import os
import datetime
import hashlib
import time
import logging
import redis # DAY 7: Redis caching
from flask import Flask, request, jsonify
from groq import Groq
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
from waitress import serve

# Configure Audit Logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - [%(levelname)s] - %(message)s',
    handlers=[
        logging.FileHandler("ai_service.log"),
        logging.StreamHandler()
    ]
)

load_dotenv()
app = Flask(__name__)
start_time = time.time()

# --- DAY 7: REDIS CACHE INITIALIZATION ---
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))

try:
    # decode_responses=True automatically converts bytes to strings
    redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, db=0, decode_responses=True)
    redis_client.ping()
    logging.info(f"Successfully connected to Redis Cache at {REDIS_HOST}:{REDIS_PORT}")
except Exception as e:
    logging.warning(f"Redis connection failed. Running without cache. Error: {e}")
    redis_client = None

# Day 11: Offline FLAN-T5 Model
logging.info("Loading instruction-tuned offline model (FLAN-T5)... Please wait.")
tokenizer = AutoTokenizer.from_pretrained("google/flan-t5-small")
local_fallback_model = AutoModelForSeq2SeqLM.from_pretrained("google/flan-t5-small")
logging.info("Offline model ready!")

# Day 8: Security Headers
@app.after_request
def add_security_headers(response):
    response.headers['X-Content-Type-Options'] = 'nosniff'
    response.headers['X-Frame-Options'] = 'DENY'
    response.headers['Strict-Transport-Security'] = 'max-age=31536000; includeSubDomains'
    response.headers['Content-Security-Policy'] = "default-src 'self'"
    response.headers['X-XSS-Protection'] = '1; mode=block'
    if 'Cache-Control' not in response.headers:
        response.headers['Cache-Control'] = 'no-store, no-cache, must-revalidate, max-age=0'
    return response

# Day 3: Rate Limiting
limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"
)

client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def get_fallback_data(task_type, input_text="No input provided"):
    now = datetime.datetime.now().isoformat()
    try:
        if task_type == "describe":
            local_prompt = f"Briefly explain the security risk of this evidence: {input_text}"
            inputs = tokenizer(local_prompt, return_tensors="pt")
            outputs = local_fallback_model.generate(**inputs, max_length=50)
            clean_output = tokenizer.decode(outputs[0], skip_special_tokens=True).capitalize()
                
            return {
                "description": f"[OFFLINE MODE] {clean_output}.",
                "severity_score": 6, 
                "generated_at": now,
                "is_fallback": True
            }
    except Exception as e:
        logging.error(f"Local Transformer Error: {e}")
        
    if task_type == "recommend":
        return {
            "recommendations": [{"action_type": "Quarantine", "description": "Isolate the asset.", "priority": "High"}],
            "is_fallback": True
        }
    return {"is_fallback": True}

# --- DAY 7: UPDATED HEALTH ENDPOINT ---
@app.route('/health', methods=['GET'])
def health_check():
    """Returns model info, uptime, and cache status."""
    uptime = time.time() - start_time
    redis_status = "connected" if redis_client and redis_client.ping() else "disconnected"
    
    return jsonify({
        "status": "healthy", 
        "uptime_seconds": int(uptime),
        "groq_model": os.getenv("GROQ_MODEL", "llama3-70b-8192"),
        "redis_cache": redis_status,
        "offline_fallback": "active"
    }), 200

@app.route('/describe', methods=['POST'])
@limiter.limit("30 per minute")
def describe_evidence():
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("prompts/describe_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])
        prompt_hash = hashlib.sha256(prompt.encode()).hexdigest()

        # --- DAY 7: REDIS CACHE HIT CHECK ---
        if redis_client:
            cached_data = redis_client.get(prompt_hash)
            if cached_data:
                logging.info("Redis Cache HIT for /describe")
                return cached_data, 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        # Cloud AI Request
        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            response_format={"type": "json_object"}
        )
        response_text = completion.choices[0].message.content

        # --- DAY 7: REDIS CACHE MISS SAVE (15 Min TTL) ---
        if redis_client:
            redis_client.setex(name=prompt_hash, time=900, value=response_text)
            logging.info("Redis Cache MISS - Data saved to Redis with 15m TTL")

        return response_text, 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}

    except Exception as e:
        logging.warning(f"Cloud AI Error: {e} -> Rerouting to local transformer...")
        resp = jsonify(get_fallback_data("describe", data['input_data']))
        return resp, 200

# Apply the exact same caching logic to the recommend route
@app.route('/recommend', methods=['POST'])
@limiter.limit("30 per minute")
def recommend_actions():
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("prompts/recommend_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])
        prompt_hash = hashlib.sha256(prompt.encode()).hexdigest()

        if redis_client:
            cached_data = redis_client.get(prompt_hash)
            if cached_data:
                logging.info("Redis Cache HIT for /recommend")
                return cached_data, 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7,
            response_format={"type": "json_object"}
        )
        response_text = completion.choices[0].message.content

        if redis_client:
            redis_client.setex(name=prompt_hash, time=900, value=response_text)
            logging.info("Redis Cache MISS - Data saved to Redis with 15m TTL")

        return response_text, 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}

    except Exception as e:
        logging.warning(f"Cloud AI Error: {e} -> Rerouting to local transformer...")
        resp = jsonify(get_fallback_data("recommend", data['input_data']))
        return resp, 200

if __name__ == '__main__':
    logging.info("Starting SentinelIQ AI Microservice on Production Server (Port 5000)...")
    serve(app, host="0.0.0.0", port=5000)