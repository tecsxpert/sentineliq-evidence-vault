# demo_runner.py
import requests
import json
import time

API_URL = "http://localhost:5000/describe"

# 30 distinct demo records for the final presentation
demo_records = [
    "Found a suspicious USB drive on the server rack.",
    "Water-damaged laptop found near the water exit.",
    "Shattered smartphone recovered from the suspect's vehicle.",
    "Server logs show 500 failed login attempts at 3 AM.",
    "Confidential documents were left unattended near the printer.",
    "An unauthorized keycard was found dropped in the lobby.",
    "Unrecognized Raspberry Pi plugged into the main network switch.",
    "Employee reported a phishing email with a PDF attachment.",
    "Hard drive found with severe burn marks in the trash bin.",
    "Security camera obscured with black tape outside the IT room.",
    "Open laptop left unlocked in the public cafeteria.",
    "External hard drive missing from the secure vault.",
    "Post-it note with admin passwords found on a monitor.",
    "Network traffic spike detected originating from the guest Wi-Fi.",
    "Ransomware note displayed on the HR department's main terminal.",
    "Severed ethernet cables found in the secondary server room.",
    "Employee badge swiped at two different locations simultaneously.",
    "Unknown Bluetooth device broadcasting near the CEO's office.",
    "Burner phone found taped under a conference room table.",
    "Firewall logs show outgoing data to a known malicious IP.",
    "Unidentified drone crashed on the roof near the HVAC intakes.",
    "USB keylogger found plugged into the CFO's keyboard.",
    "Multiple biometric scanner rejections at the main vault.",
    "Suspicious powder found inside an opened hardware shipment.",
    "Database backup files found on a personal cloud storage account.",
    "Smartwatch with recording capabilities left in a classified meeting.",
    "Physical lock on the telecom closet shows signs of picking.",
    "Hidden Wi-Fi network 'Free_Coffee' detected mimicking company SSID.",
    "Discarded uniform found near the restricted loading dock.",
    "Large outbound email sent with encrypted ZIP file attached."
]

demo_results = []

print(f"🚀 Starting Batch Processing of {len(demo_records)} records...")

for index, record in enumerate(demo_records):
    print(f"Processing {index + 1}/{len(demo_records)}: {record[:30]}...")
    
    try:
        start_time = time.time()
        response = requests.post(API_URL, json={"input_data": record})
        latency = round(time.time() - start_time, 2)
        
        if response.status_code == 200:
            result = response.json()
            demo_results.append({
                "id": index + 1,
                "input": record,
                "latency_seconds": latency,
                "ai_analysis": result
            })
        else:
            print(f"  ❌ Error: Status {response.status_code}")
            
    except Exception as e:
        print(f"  ❌ Connection Failed: {e}")
        
    # Brief pause to respect rate limits
    time.sleep(1)

# Save to a demo-ready JSON file
with open("demo_output.json", "w") as f:
    json.dump(demo_results, f, indent=4)

print("\n✅ Batch Processing Complete! All outputs saved to 'demo_output.json'.")