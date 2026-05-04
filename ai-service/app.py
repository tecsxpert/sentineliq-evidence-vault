import os
import datetime
import hashlib
import time
from flask import Flask, request, jsonify
from groq import Groq
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# --- DAY 11: OFFLINE AI REDUNDANCY ---
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM

# Load environment variables
load_dotenv()

app = Flask(__name__)

# Day 7: Tracking server start time
start_time = time.time()

# Day 9: Internal cache
ai_cache = {}

# Day 11: Initialize Offline FLAN-T5 Model at Startup
print("Loading instruction-tuned offline model (FLAN-T5)... Please wait.")
tokenizer = AutoTokenizer.from_pretrained("google/flan-t5-small")
local_fallback_model = AutoModelForSeq2SeqLM.from_pretrained("google/flan-t5-small")
print("Offline model ready!")

# --- DAY 8: SECURITY HEADERS (Global Middleware) ---
@app.after_request
def add_security_headers(response):
    """Mitigates OWASP ZAP findings."""
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
    """Day 11: High-Quality dynamic fallback using local AI."""
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
        print(f"Local Transformer Error: {e}")
        
    if task_type == "recommend":
        return {
            "recommendations": [
                {"action_type": "Quarantine", "description": "Isolate the asset. Offline AI cannot verify safety.", "priority": "High"}
            ],
            "is_fallback": True
        }
    return {"is_fallback": True}

@app.route('/health', methods=['GET'])
def health_check():
    """Day 7: Uptime and system metrics"""
    uptime = time.time() - start_time
    return jsonify({
        "status": "healthy", 
        "uptime_seconds": int(uptime),
        "cache_entries": len(ai_cache),
        "offline_model": "loaded"
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

        # Day 9: Cache HIT
        if prompt_hash in ai_cache:
            return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        # Cloud AI Request
        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            response_format={"type": "json_object"}
        )

        # Day 9: Cache MISS
        ai_cache[prompt_hash] = completion.choices[0].message.content
        return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}

    except Exception as e:
        print(f"Cloud AI Error: {e} -> Rerouting to local transformer...")
        resp = jsonify(get_fallback_data("describe", data['input_data']))
        return resp, 200

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

        if prompt_hash in ai_cache:
            return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7,
            response_format={"type": "json_object"}
        )

        ai_cache[prompt_hash] = completion.choices[0].message.content
        return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}

    except Exception as e:
        print(f"Cloud AI Error: {e} -> Rerouting to local transformer...")
        resp = jsonify(get_fallback_data("recommend", data['input_data']))
        return resp, 200

if __name__ == '__main__':
    app.run(port=5000)