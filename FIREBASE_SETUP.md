# HanapAral – Firebase Setup Guide (Step by Step)

Follow these steps to configure Firebase for the HanapAral app.

---

## Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Create a project**
3. Enter project name: **HanapAral**
4. (Optional) Enable Google Analytics, then continue
5. Click **Create project**

---

## Step 2: Add Android App to Firebase

1. In the project overview, click **Add app** → **Android**
2. Enter **Android package name**: `com.example.hanaparal`
3. (Optional) Enter app nickname: **HanapAral**
4. Click **Register app**
5. **Download `google-services.json`** and place it in:
   ```
   HanapAral/app/
   ```
   (same folder as `app/build.gradle.kts`)

---

## Step 3: Enable Google Sign-In (Authentication)

1. In Firebase Console → **Build** → **Authentication**
2. Click **Get started**
3. Go to **Sign-in method** tab
4. Click **Google** → Enable it
5. Set **Project support email** and save

---

## Step 4: Create Firestore Database

1. Go to **Build** → **Firestore Database**
2. Click **Create database**
3. Start in **production mode**
4. Choose a location (e.g. `us-central1`) → **Enable**
5. Add Firestore security rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /students/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /studyGroups/{groupId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null;
    }
    match /studyGroups/{groupId}/members/{memberId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## Step 5: Enable Cloud Messaging (FCM)

1. Go to **Build** → **Cloud Messaging**
2. No extra setup needed in console for basic FCM
3. For sending notifications:
   - Use **Project Settings** → **Cloud Messaging** tab
   - Copy **Server key** (for legacy HTTP API) or use **Firebase Admin SDK** in a backend
4. For this app, FCM is configured in the client; you can send test notifications from Firebase Console or a backend

---

## Step 6: Add Remote Config

1. Go to **Build** → **Remote Config**
2. Click **Create configuration**
3. Add these parameters:

| Parameter Key           | Default Value | Description                      |
|-------------------------|---------------|----------------------------------|
| `group_creation_enabled`| `true`        | Toggle group creation module     |
| `announcement_header`   | `Welcome!`    | Global announcement header       |
| `max_members_per_group` | `20`          | Maximum members per study group  |

4. Click **Publish changes**

---

## Step 7: Get SHA-1 for Google Sign-In (Android)

1. In project folder, run:
   ```bash
   cd c:\Users\admin\AndroidStudioProjects\HanapAral
   gradlew signingReport
   ```
2. Copy the **SHA-1** under `Variant: debug`
3. In Firebase Console → **Project Settings** → **Your apps**
4. Select your Android app → **Add fingerprint** → paste SHA-1 → Save

---

## Step 8: Add Web Client ID for Google Sign-In

1. In Firebase Console → **Project Settings** → **Your apps** → select your Android app
2. Scroll to **Your apps** → under the app, find **SHA certificate fingerprints**
3. Also in **Project Settings** → **General** tab → under **Your apps**, click **Web** (or add a Web app if none)
4. Copy the **Web client ID** (looks like `123456789-xxx.apps.googleusercontent.com`)
5. Add to `app/src/main/res/values/strings.xml`:
   ```xml
   <string name="default_web_client_id" translatable="false">YOUR_WEB_CLIENT_ID_HERE</string>
   ```
   *(Note: If you add a Web app to Firebase, `google-services.json` may already include this; the plugin can auto-generate it. Try building first - if you get "default_web_client_id" error, add the string manually.)*

---

## Step 9: Verify Setup

- `google-services.json` is in `app/` folder
- Authentication → Google is enabled
- Firestore is created and rules are set
- Remote Config parameters are created
- SHA-1 fingerprint is added for the Android app

---

## Step 10: Optional - Send FCM Notifications

The app is configured to receive push notifications. To send them:

1. **Test notification**: Firebase Console → **Cloud Messaging** → **Create your first campaign**
2. **Automated triggers** (requires backend): Use Firebase Admin SDK (Node.js, Cloud Functions) to send notifications when:
   - **New member joins**: Trigger when a document is created in `studyGroups/{id}/members/`
   - **Group announcements**: Trigger when an announcement document is created
   - **Study reminders**: Schedule via Cloud Scheduler + Cloud Functions
