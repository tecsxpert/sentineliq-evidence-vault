import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()
client = Groq(api_key=os.getenv("GROQ_API_KEY"))

def test_prompt(input_text):
    # Load the template you just created
    # Using forward slashes is safer and works on Windows
    with open("prompts/describe_template.txt", "r") as f:
        template = f.read()
    
    prompt = template.replace("{input_data}", input_text)
    
    try:
        chat_completion = client.chat.completions.create(
            messages=[{"role": "user", "content": prompt}],
            model=os.getenv("GROQ_MODEL"),
            temperature=0.3, # Use 0.3 for factual consistency [cite: 94]
            response_format={"type": "json_object"}
        )
        print(f"Input: {input_text[:30]}...")
        print(f"Output: {chat_completion.choices[0].message.content}\n")
    except Exception as e:
        print(f"Error testing input: {e}")

# 5 Real-world test inputs
test_cases = [
    "Discarded USB drive found in the lobby with 'Company Secrets' label.",
    "CCTV footage showing an unidentified individual entering the server room at 2 AM.",
    "Encrypted PDF file recovered from the CEO's deleted items folder.",
    "Physical logbook with missing pages from the secure warehouse entrance.",
    "Anonymous tip regarding unauthorized software installation on workstation-04."
]

if __name__ == "__main__":
    for case in test_cases:
        test_prompt(case)