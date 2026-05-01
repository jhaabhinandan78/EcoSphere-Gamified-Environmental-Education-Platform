# EcoSphere — Environmental Education Android App

A multi-tenant environmental education platform for schools, featuring role-based portals for Students, Teachers, and Lead Teachers with real-time gamification and content management.

## Tech Stack

- **Android** — Kotlin, Material Design 3, ViewBinding
- **Backend** — Firebase Firestore, Firebase Auth, Firebase Storage, FCM
- **Web Portal** — HTML, CSS, JavaScript, Firebase Web SDK

---

## Features

### Student Portal
- Gamified learning with modules, quizzes, and eco-challenges
- EcoPoints system with 10-level dynamic progression
- Real-time school-scoped leaderboard
- Challenge submission with photo proof upload
- Announcements and push notifications from teachers
- Progress tracker with module and challenge completion stats

### Teacher Portal
- Create and manage learning modules, quizzes, and challenges
- Review and approve/reject student challenge submissions
- Send school-scoped announcements and push notifications
- Batch (class) management for organizing students
- Live activity feed and top performers view

### Lead Teacher Portal
- All teacher features plus:
- Approve or reject pending teacher registrations
- Platform settings management
- User role management

### School Registration Web Portal
- Onboard new schools with auto-generated unique school codes
- Manage school metadata independently of the mobile app

---

## Setup

### 1. Firebase Setup
- Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
- Enable **Authentication** (Email/Password), **Firestore**, **Storage**, and **Cloud Messaging**

### 2. Android App
- Copy `app/google-services.json.example` → `app/google-services.json`
- Replace all placeholder values with your Firebase project credentials
- Open in Android Studio and run

### 3. Web Portal
- Copy `school-registration-portal/firebase-config.js.example` → `school-registration-portal/firebase-config.js`
- Replace all placeholder values with your Firebase web app credentials
- Open `index.html` in a browser or deploy to Firebase Hosting

---

## Project Structure

```
├── app/
│   ├── src/main/java/com/example/capstone/
│   │   ├── models/          # Data models
│   │   ├── repository/      # Firebase data layer
│   │   ├── utils/           # Utilities (LevelCalculator, etc.)
│   │   └── *.kt             # Activities and Fragments
│   └── google-services.json.example
├── school-registration-portal/
│   ├── index.html
│   ├── app.js
│   ├── styles.css
│   └── firebase-config.js.example
└── README.md
```

---

## Security Notes

- `google-services.json` and `firebase-config.js` are excluded from version control
- Never commit real API keys — use the `.example` files as templates
- Configure Firestore security rules before deploying to production

---

## License

This project was developed as a capstone project for academic purposes.
