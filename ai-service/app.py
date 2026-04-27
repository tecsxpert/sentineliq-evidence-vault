import os
import datetime
from flask import Flask, request, jsonify
from groq import Groq
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

load_dotenv()
app = Flask(__name__)

# Day 3 Requirement: Rate Limiting [cite: 16, 64]
limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"],
    storage_uri="memory://"
)

client = Groq(api_key=os.getenv("GROQ_API_KEY"))

@app.route('/describe', methods=['POST'])
@limiter.limit("30 per minute") # cite: 64
def describe_evidence():
    data = request.json
    if not data or 'input_data' not in data:
        return jsonify({"error": "Missing input_data"}), 400

    try:
        # Load the template you refined on Day 2 
        with open("prompts/describe_template.txt", "r") as f:
            template = f.read()
        
        prompt = template.replace("{input_data}", data['input_data'])

        # Call Groq [cite: 16]
        completion = client.chat.completions.create(
            model=os.getenv("GROQ_MODEL"),
            messages=[{"role": "user", "content": prompt}],
            temperature=0.3,
            response_format={"type": "json_object"}
        )

        # Return structured JSON with generated_at timestamp 
        response_data = completion.choices[0].message.content
        return response_data, 200, {'Content-Type': 'application/json'}

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)