# Tool-76: SentinelIQ AI Evidence Vault (Microservice)

## 📌 Project Overview
The SentinelIQ AI Microservice is a high-performance Flask application that provides real-time, structured intelligence for the Evidence Vault. It leverages the **LLaMA-3.3-70b** model via the Groq API to analyze evidence, generate risk recommendations, and draft comprehensive investigative reports.

## 🚀 Features
- **Structured Analysis**: Dedicated endpoints for evidence description, recommendations, and reporting.
- **Performance**: SHA256-based response caching ensuring < 2s latency.
- **Resilience**: Context-aware fallback templates prevent 500 errors during API outages.
- **Security**: Hardened with CSP, HSTS, and X-Frame-Options headers to mitigate OWASP ZAP findings.

## 🏗️ Technical Architecture
- **Language**: Python 3.11
- **Framework**: Flask 3.x
- **AI Model**: LLaMA-3.3-70b (via Groq Cloud)
- **Rate Limiting**: Flask-Limiter (Fixed at 30 req/min)
- **Caching**: SHA256-based in-memory response caching (Day 7/9 Optimization)[cite: 1]
- **Security**: OWASP ZAP-compliant headers (CSP, HSTS, X-Frame-Options)[cite: 1]

## 🛠️ Installation & Setup
1. **Directory**: `cd ai-service`
2. **Virtual Env**: `python -m venv venv`
3. **Activate**: `.\venv\Scripts\activate` (Windows) or `source venv/bin/activate` (Mac/Linux)
4. **Dependencies**: `pip install -r requirements.txt`
5. **Environment Variables**: Create a `.env` file based on `.env.example`:
   ```env
   GROQ_API_KEY=your_key_here
   GROQ_MODEL=llama-3.3-70b-versatile