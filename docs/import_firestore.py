#!/usr/bin/env python3
"""
Script to import JSON data into Firestore
Requires: pip install firebase-admin
"""

import json
import firebase_admin
from firebase_admin import credentials, firestore

# Initialize Firebase Admin using service account key file
try:
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
    print("✓ Firebase initialized with service account")
except ValueError as e:
    if "already initialized" in str(e).lower():
        print("Firebase already initialized")
    else:
        raise
except FileNotFoundError:
    print("❌ Error: serviceAccountKey.json not found!")
    print("Please ensure the service account key file is in the docs/ directory")
    exit(1)

db = firestore.client()

def import_data(json_file='test_account_nguyen_bao_minh.json'):
    with open(json_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    # Import users
    if 'users' in data:
        for doc_id, user_data in data['users'].items():
            db.collection('users').document(doc_id).set(user_data)
            print(f"✓ Imported user: {doc_id}")
    
    # Import accounts
    if 'accounts' in data:
        for doc_id, account_data in data['accounts'].items():
            db.collection('accounts').document(doc_id).set(account_data)
            print(f"✓ Imported account: {doc_id}")
    
    print("\n✅ Import completed successfully!")

if __name__ == '__main__':
    import_data()

