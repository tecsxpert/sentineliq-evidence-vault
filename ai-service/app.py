import os
import datetime
from flask import Flask, request, jsonify
from groq import Groq
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

# Load environment variables from .env [cite: 64]
load_dotenv()

app = Flask(__name__)

# Day 3 Requirement: Rate Limiting - blocks IPs exceeding 30 req/min 
limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"
)

# Initialize Groq client with API key 
client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def get_fallback_data(task_type):
    """Provides a safe JSON response if the AI fails to prevent HTTP 500."""
    now = datetime.datetime.now().isoformat()
    if task_type == "describe":
        return {
            "description": "Analysis is currently being processed or the AI service is temporarily unavailable.",
            "severity_score": 0,
            "generated_at": now,
            "is_fallback": True
        }
    return {
        "recommendations": [
            {"action_type": "Manual Review", "description": "Please review this item manually.", "priority": "High"}
        ],
        "is_fallback": True
    }

@app.route('/health', methods=['GET'])
def health_check():
    """Health endpoint for Java service to monitor AI status[cite: 12, 69]."""
    return jsonify({"status": "healthy", "timestamp": datetime.datetime.now().isoformat()}), 200

@app.route('/describe', methods=['POST'])
@limiter.limit("30 per minute")
def describe_evidence():
    """AI Developer 1: Validates input, loads prompt, calls Groq, returns structured JSON[cite: 64]."""
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        # Load the template refined on Day 2 [cite: 64]
        with open("prompts/describe_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])

        # Call Groq LLaMA-3.3-70b 
        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3, # Factual setting 
            response_format={"type": "json_object"}
        )

        response_data = completion.choices[0].message.content
        return response_data, 200, {'Content-Type': 'application/json'}

    except Exception as e:
        print(f"AI Service Error: {e}")
        # Return fallback template on Groq error to avoid 500 status 
        return jsonify(get_fallback_data("describe")), 200

@app.route('/recommend', methods=['POST'])
@limiter.limit("30 per minute")
def recommend_actions():
    """AI Developer 1: Provides 3 recommendations as a JSON array[cite: 64]."""
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("prompts/recommend_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.7, # Creative setting for recommendations 
            response_format={"type": "json_object"}
        )

        return completion.choices[0].message.content, 200, {'Content-Type': 'application/json'}
    except Exception as e:
        print(f"AI Service Error: {e}")
        return jsonify(get_fallback_data("recommend")), 200

@app.route('/generate-report', methods=['POST'])
@limiter.limit("30 per minute")
def generate_report():
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        with open("prompts/report_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])

        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.5, # Balanced for reporting 
            response_format={"type": "json_object"}
        )

        # Return the AI response
        return completion.choices[0].message.content, 200, {'Content-Type': 'application/json'}
    except Exception as e:
        print(f"Report Error: {e}") # Check your terminal to see the actual error message
        # Return a report-specific fallback
        return jsonify({
            "title": "Report Unavailable",
            "summary": "AI service encountered an error.",
            "overview": "Detailed report could not be generated.",
            "key_items": [],
            "recommendations": [],
            "is_fallback": True
        }), 200
    
if __name__ == '__main__':
    # AI Microservice runs on Port 5000 [cite: 12]
    app.run(port=5000)