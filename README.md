# 🌍 EcoSphere — Gamified Environmental Education Platform

<p align="center">
  <strong>Learn. Act. Sustain.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/UI-Material Design 3-6750A4?style=for-the-badge"/>
</p>

---

## 📌 About the Project

**EcoSphere** is a multi-tenant environmental education platform built for schools. It turns passive eco-learning into an active, measurable, and competitive experience through gamification, structured learning modules, real-world eco-challenges, and live leaderboards.

The platform consists of:
- **Android Mobile App** — The core learning and management platform used daily by students and teachers
- **School Registration Web Portal** — Allows school administrators to onboard their institution and generate a unique school code

---

## 🎯 Problem & Solution

**Problem:** Schools lack a dedicated digital platform to deliver structured eco-education, track student progress, and motivate students to take real-world environmental action. Traditional methods are passive and ineffective.

**Solution:** EcoSphere brings gamification to environmental learning — students earn EcoPoints, level up, compete on leaderboards, and submit real eco-challenges with photo proof. Teachers manage all content and monitor student engagement from a dedicated portal.

---

## 🏗️ How It Works

A school registers through the web portal and receives a **unique 14-character school code**. Teachers and students use this code to join the school on the mobile app. The first teacher to register becomes the **Lead Teacher** — who approves other teachers and manages platform settings. Teachers then create learning modules, quizzes, and challenges. Students learn, submit, earn points, and compete.

Every piece of data is tagged with a `schoolId` — ensuring **complete data isolation between schools** enforced at both the app layer and Firestore security rules.

---

## 👥 User Roles

| Role | Description |
|---|---|
| **Student** | Learns modules, takes quizzes, submits eco-challenges, earns EcoPoints, competes on leaderboard |
| **Normal Teacher** | Creates content, manages batches, reviews challenge submissions, sends announcements |
| **Lead Teacher** | All teacher features + approves/rejects teacher registrations + manages platform settings |

---

## ✨ Key Features

### Student Portal
- Structured **learning modules** with quiz gate — must pass quiz to complete a module
- **Eco-challenge submissions** with photo proof, reviewed and approved by teachers
- **Real-time school leaderboard** — live ranking of all school students via Firestore snapshot listeners
- **10-level EcoPoints progression** — Eco Seedling 🌱 to Eco Master 🏆
- **Daily login streaks**, progress tracker, announcements, push notifications
- **Ecosystem Strategy Game** — 8 eco-decision scenarios, earn up to 10 EcoPoints

### Teacher Portal
- **Dashboard analytics** — total students, active today, total EcoPoints, average completion %
- Full **CRUD for modules, quizzes, and challenges** — all scoped to the teacher's school
- **Challenge review workflow** — approve/reject submissions with feedback, auto-awards points
- **School-scoped announcements** and **FCM push notifications** to all school students
- **Batch management**, top performers view, live activity feed
- **Teacher approval system** *(Lead Teacher)* — real-time pending registrations with approve/reject

### Web Portal
- School registration with full details
- **Auto-generates a guaranteed 14-character unique school code**
- Directly saves to Firebase Firestore
- Download registration details as a text file

---

## 🏆 Gamification — Level System

| Level | Title | EcoPoints |
|---|---|---|
| 1 | Eco Seedling 🌱 | 0 |
| 2 | Eco Sprout 🌿 | 100 |
| 3 | Eco Explorer 🌳 | 250 |
| 4 | Eco Guardian 🌍 | 500 |
| 5 | Eco Champion ⭐ | 800 |
| 6 | Eco Expert 🔥 | 1,100 |
| 7 | Eco Warrior 🛡️ | 1,500 |
| 8 | Eco Sage 🌟 | 2,000 |
| 9 | Eco Legend 💎 | 2,700 |
| 10 | Eco Master 🏆 | 3,600 |

Points are earned **only** from module completion and challenge approval — no extra or duplicate points. The level system uses an exponential curve so early levels are achievable quickly while higher levels reward sustained effort.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android SDK 24–34 |
| UI Framework | Material Design 3, ViewBinding |
| Database | Firebase Firestore |
| Authentication | Firebase Auth |
| File Storage | Firebase Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Background Jobs | WorkManager |
| Image Loading | Glide |
| Async Operations | Kotlin Coroutines |
| Architecture | MVVM, Repository Pattern |
| Web Portal | HTML, CSS, JavaScript + Firebase Web SDK |

---

## 📐 Architecture Highlights

- **Multi-tenancy** — Multiple schools on a single platform with complete data isolation via `schoolId` filtering on every Firestore query
- **Role-based access** — 3 distinct portals (Student, Teacher, Lead Teacher) with UI and feature visibility controlled by user role at runtime
- **Firestore Security Rules** — Server-side enforcement of school isolation — even a direct API call cannot access another school's data
- **Real-time leaderboard** — Firestore `addSnapshotListener` pushes live updates to all connected students
- **Cascade delete** — Deleting a module/challenge automatically reverses student EcoPoints, removes completion records and quiz attempts
- **Transaction-safe points** — Firestore transactions prevent duplicate EcoPoints from being awarded

---

## 👨‍💻 My Contribution

This is a capstone group project. My role was the **complete Android application development**:
- Designed and implemented all 3 role-based portals from scratch
- Architected the multi-tenant system with school-scoped data isolation
- Built the gamification engine — EcoPoints, 10-level progression, real-time leaderboard
- Developed the quiz engine with configurable pass thresholds and daily attempt limits
- Implemented challenge submission and teacher review workflow with automatic point reversal on delete
- Integrated Firebase Auth, Firestore, Storage, and FCM end-to-end
- Wrote and deployed production Firestore security rules
- Connected web portal onboarding with mobile app registration flow

The school registration web portal was built by my teammates.

---

## 📊 Project Scale

- 66+ Kotlin files · 75+ XML layouts · 3 role-based portals
- 15+ Firestore collections with security rules
- Unlimited school support on a single platform

---

<p align="center">Made with ❤️ for a greener future 🌱</p>
