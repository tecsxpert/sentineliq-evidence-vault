# seed_db.py
import chromadb

print("Initializing ChromaDB...")
client = chromadb.PersistentClient(path="./chroma_db")

# Create a collection for SentinelIQ knowledge
collection = client.get_or_create_collection(name="sentineliq_knowledge")

# 10 Domain Knowledge Documents
documents = [
    "Protocol 1: Any USB drive found in unauthorized areas must be isolated and scanned for malware.",
    "Protocol 2: Water-damaged electronics should not be powered on to prevent short circuits.",
    "Protocol 3: Server logs showing multiple failed login attempts indicate a potential brute-force attack.",
    "Guideline A: Confidential documents left unattended on desks constitute a severe physical security breach.",
    "Guideline B: Shattered or physically destroyed smartphones often indicate an attempt to destroy digital evidence.",
    "Policy 101: Unauthorized keycards found near server rooms require an immediate facility-wide access audit.",
    "Hardware Standard: All recovered hard drives must be immediately placed in static-proof Faraday bags.",
    "Network Standard: Unrecognized devices plugged into ethernet ports trigger an automatic Level 4 lockdown.",
    "Security Memo: Phishing emails containing encrypted PDF attachments are currently the primary attack vector.",
    "Evidence Handling: Always photograph the scene and device state before touching or removing any electronic evidence."
]

# Generate IDs for the documents
ids = [f"rule_{i}" for i in range(1, 11)]

print("Seeding 10 domain documents into the vector database...")
collection.add(documents=documents, ids=ids)

print("✅ ChromaDB Seeded Successfully! Database is ready for RAG.")