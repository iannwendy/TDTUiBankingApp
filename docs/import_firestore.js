const admin = require('firebase-admin');
const fs = require('fs');

// Initialize Firebase Admin
const serviceAccount = require('./serviceAccountKey.json'); // Bạn cần tải file này từ Firebase Console

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: 'ibanking-ffbb9'
});

const db = admin.firestore();

async function importData() {
  try {
    const data = JSON.parse(fs.readFileSync('./test_account_nguyen_bao_minh.json', 'utf8'));
    
    // Import users
    if (data.users) {
      for (const [docId, userData] of Object.entries(data.users)) {
        await db.collection('users').doc(docId).set(userData);
        console.log(`✓ Imported user: ${docId}`);
      }
    }
    
    // Import accounts
    if (data.accounts) {
      for (const [docId, accountData] of Object.entries(data.accounts)) {
        await db.collection('accounts').doc(docId).set(accountData);
        console.log(`✓ Imported account: ${docId}`);
      }
    }
    
    console.log('\n✅ Import completed successfully!');
    process.exit(0);
  } catch (error) {
    console.error('❌ Error importing data:', error);
    process.exit(1);
  }
}

importData();

