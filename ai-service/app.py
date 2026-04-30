import os
import datetime
import hashlib
import time
from flask import Flask, request, jsonify
from groq import Groq
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# Load environment variables
load_dotenv()

app = Flask(__name__)

# Day 7/9: Tracking server start time for health metrics
start_time = time.time()

# Day 7/9: Internal cache to meet the < 2s response target
ai_cache = {}

# --- DAY 8: SECURITY HEADERS (Global Middleware) ---
@app.after_request
def add_security_headers(response):
    """Day 8: Mitigates OWASP ZAP findings by enforcing secure browser behaviors."""
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

# Initialize Groq client
client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def get_fallback_data(task_type):
    """Day 5/9: Provides safe JSON if the AI fails to prevent HTTP 500."""
    now = datetime.datetime.now().isoformat()
    if task_type == "describe":
        return {
            "description": "Analysis is currently unavailable. Using fallback logic.",
            "severity_score": 0,
            "generated_at": now,
            "is_fallback": True
        }
    elif task_type == "recommend":
        return {
            "recommendations": [
                {"action_type": "Manual Review", "description": "AI service offline. Review manually.", "priority": "High"}
            ],
            "is_fallback": True
        }
    return {
        "title": "Report Unavailable",
        "summary": "AI service encountered an error.",
        "overview": "Detailed report could not be generated.",
        "key_items": [],
        "recommendations": [],
        "is_fallback": True
    }

@app.route('/health', methods=['GET'])
def health_check():
    """Day 7/9: Health endpoint with uptime and cache stats[cite: 1]."""
    uptime = time.time() - start_time
    return jsonify({
        "status": "healthy", 
        "uptime_seconds": int(uptime),
        "cache_entries": len(ai_cache),
        "timestamp": datetime.datetime.now().isoformat()
    }), 200

@app.route('/describe', methods=['POST'])
@limiter.limit("30 per minute")
def describe_evidence():
    """Day 9: Optimized with SHA256 Caching[cite: 1]."""
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("ai-service/prompts/describe_template.txt", "r") as f:
            template = f.read()
        prompt = template.replace("{input_data}", data['input_data'])
        
        # Caching logic
        prompt_hash = hashlib.sha256(prompt.encode()).hexdigest()
        if prompt_hash in ai_cache:
            return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            response_format={"type": "json_object"}
        )

        ai_cache[prompt_hash] = completion.choices[0].message.content
        return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}
    except Exception:
        return jsonify(get_fallback_data("describe")), 200

@app.route('/recommend', methods=['POST'])
@limiter.limit("30 per minute")
def recommend_actions():
    """Day 9: Optimized recommendations with 2s speed target[cite: 1]."""
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("ai-service/prompts/recommend_template.txt", "r") as f:
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
    except Exception:
        return jsonify(get_fallback_data("recommend")), 200

@app.route('/generate-report', methods=['POST'])
@limiter.limit("30 per minute")
def generate_report():
    """Day 9: Optimized report generation with zero 500 error guarantee[cite: 1]."""
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("ai-service/prompts/report_template.txt", "r") as f:
            template = f.read()
        prompt = template.replace("{input_data}", data['input_data'])
        
        prompt_hash = hashlib.sha256(prompt.encode()).hexdigest()
        if prompt_hash in ai_cache:
            return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'HIT'}

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.5,
            response_format={"type": "json_object"}
        )

        ai_cache[prompt_hash] = completion.choices[0].message.content
        return ai_cache[prompt_hash], 200, {'Content-Type': 'application/json', 'X-Cache': 'MISS'}
    except Exception:
        return jsonify(get_fallback_data("report")), 200

if __name__ == '__main__':
    # AI Microservice runs on Port 5000[cite: 1]
    app.run(port=5000)