# Firebase setup

## Quick login test checklist (do these in order)
1) Create Firebase project `TDTUMobileBanking`.
2) Add Android app  
   - Package: `com.example.tdtumobilebanking`  
   - (Optional but recommended) Add SHA-1 from `./gradlew signingReport` for future Dynamic Links/Phone auth.  
   - Download `google-services.json` → place at `app/google-services.json` (do NOT commit).
3) Enable products  
   - Authentication → Sign-in method → Email/Password ON.  
   - Firestore → start in production mode.  
   - Storage → keep default rules for now (or tighten later).
4) Create Firestore seed documents (UI will need these to route)  
   - Collection `users`, doc id = the Firebase Auth uid you will log in with. Fields:  
     ```
     fullName: "Test Customer"
     email: your_login_email@example.com
     role: "CUSTOMER"          // or "OFFICER"
     phoneNumber: "0123456789"
     kycStatus: "VERIFIED"     // to bypass KYC screen
     avatarUrl: ""             // optional
     uid: (same as doc id)
     ```  
   - (Optional) Create collection `accounts` with an account for that user:  
     ```
     accountId: "acc_test_1"   // doc id can match
     ownerId: <same uid>
     accountType: "CHECKING"
     balance: 5_000_000
     currency: "VND"
     ```
5) Create an Auth user to match the Firestore doc  
   - Authentication → Users → Add user → email/password same as above.  
   - Copy the generated uid into the Firestore `users` doc id.
6) (Maps optional) In `local.properties` add your key if using maps screen:  
   ```
   MAPS_API_KEY=YOUR_KEY
   ```
7) Sync & run  
   - Android Studio: Sync Gradle, then run the app.  
   - Log in with the email/password you created. Should land on Customer or Officer dashboard depending on `role`.

1. **Create project**
   - Create a Firebase project named `TDTUMobileBanking`.
   - Add an Android app with package `com.example.tdtumobilebanking`.
   - Download `google-services.json` and place it at `app/google-services.json` (do not commit secrets).

2. **Enable products**
   - Authentication: Email/Password enabled.
   - Firestore: start in production mode; create collections `users`, `accounts`, `transactions`, `branches` per schema in `req.txt`.
   - Storage: keep default rules, create bucket path `/kyc/{uid}/` for ID + selfie.
   - (Optional) Crashlytics/Analytics already wired via BOM.

3. **Security rules (baseline)**
   ```json
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
       match /accounts/{accountId} {
         allow read, write: if request.auth != null;
       }
       match /transactions/{txId} {
         allow read, write: if request.auth != null;
       }
       match /branches/{branchId} {
         allow read: if true;
         allow write: if request.auth != null;
       }
     }
   }
   ```
   Adjust for officer-only operations in production (e.g., role claims).

4. **Gradle**
   - Plugins already set: `com.google.gms.google-services`, Hilt, Compose.
   - Firebase BOM declared in `app/build.gradle.kts`.

5. **Google Maps**
   - Create an Android Maps SDK API key in Google Cloud and add to `local.properties`:
     ```
     MAPS_API_KEY=YOUR_KEY
     ```
   - Reference it in `AndroidManifest.xml` if enabling the map view.

6. **2FA mock**
   - The app generates a 6-digit OTP in-app (Toast/Screen) before transfer confirmation.

7. **eKYC capture**
   - Implement CameraX pipeline to capture ID + selfie and upload to `Storage /kyc/{uid}/`.
   - Persist download URLs in Firestore `users.avatarUrl` and set `kycStatus` to `PENDING` then `VERIFIED` after officer review.

