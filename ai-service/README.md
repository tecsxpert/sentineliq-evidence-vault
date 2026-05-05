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
- **Production Server:** Waitress WSGI
- **Framework**: Flask 3.x
- **AI Model**: LLaMA-3.3-70b (via Groq Cloud)
- **Rate Limiting**: Flask-Limiter (Fixed at 30 req/min)
- **Offline Fallback AI:** google/flan-t5-small (loaded locally into RAM)
- **Caching:** Redis (15-minute TTL via SHA256 hashing)
- **Vector DB:** ChromaDB (for Domain Knowledge RAG)
- **Security**: OWASP ZAP-compliant headers (CSP, HSTS, X-Frame-Options)

## **⚙️ Prerequisites & Setup**

1. Ensure Python 3.11+ is installed.  
2. Navigate to the ai-service/ directory.  
3. Install dependencies:  
   pip install \-r requirements.txt

4. Configure your environment variables.

## **🔐 Environment Variables (.env)**

Create a .env file in the ai-service/ directory with the following variables. **Never commit this file to GitHub.**

GROQ\_API\_KEY=gsk\_your\_api\_key\_here  
GROQ\_MODEL=llama3-70b-8192  
REDIS\_HOST=localhost  
REDIS\_PORT=6379

## **🏃‍♂️ Run Instructions**

To start the production server (Waitress) on port 5000:

python app.py

*Note: The server will take a few seconds to load the offline Hugging Face model into memory before accepting requests. All logs are saved to ai\_service.log.*

## **📡 API Reference**

### **1\. Health Check**

* **Endpoint:** GET /health  
* **Description:** Returns uptime, active model, and Redis connection status.  
* **Response:**  
  {  
      "status": "healthy",  
      "uptime\_seconds": 120,  
      "groq\_model": "llama3-70b-8192",  
      "redis\_cache": "connected",  
      "offline\_fallback": "active"  
  }

### **2\. Describe Evidence**

* **Endpoint:** POST /describe  
* **Rate Limit:** 30 requests / minute  
* **Request Body:**  
  {  
      "input\_data": "Found a suspicious USB drive on the server rack."  
  }

* **Success Response (200 OK):**  
  {  
      "description": "The USB drive presents a severe risk of malicious payload execution or unauthorized data exfiltration.",  
      "severity\_score": 5,  
      "generated\_at": "2026-05-04T12:00:00.000Z",  
      "is\_fallback": false  
  }

### **3\. Recommend Actions**

* **Endpoint:** POST /recommend  
* **Rate Limit:** 30 requests / minute  
* **Request Body:**  
  {  
      "input\_data": "Found a suspicious USB drive on the server rack."  
  }

* **Success Response (200 OK):**  
  {  
      "recommendations": \[  
          {  
              "action\_type": "Quarantine",  
              "description": "Isolate the USB drive in a static-proof bag immediately.",  
              "priority": "High"  
          }  
      \],  
      "is\_fallback": false  
  }

### **4\. Generate Report (Maintained by AI Dev 2\)**

* **Endpoint:** POST /generate-report  
* **Description:** Aggregates data into a structured JSON report.  
* **Request Body:**  
  {  
      "input\_data": "Summary of Week 1 incidents."  
  }

* **Success Response (200 OK):**  
  {  
      "title": "Week 1 Incident Report",  
      "summary": "Brief overview of the events...",  
      "key\_items": \["Item 1", "Item 2"\],  
      "recommendations": \["Recommendation 1"\]  
  }  
