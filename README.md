# 🌍 EcoSphere — Gamified Environmental Education Platform

<p align="center">
  <strong>Learn. Act. Sustain.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
  <img src="https://img.shields.io/badge/UI-Material Design 3-6750A4?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Architecture-MVVM-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Status-Completed-success?style=for-the-badge"/>
</p>

---

## 📌 What is EcoSphere?

**EcoSphere** is a production-ready, multi-tenant environmental education platform built for schools. It transforms passive eco-learning into an active, competitive, and measurable experience through gamification, structured learning, real-world eco-challenges, and live leaderboards.

The platform has two components:
- **📱 Android Mobile App** — The core learning and management platform used daily by students and teachers
- **🌐 School Registration Web Portal** — Onboarding tool for school administrators deployed on Netlify

---

## 🎯 Problem Statement

Schools have no dedicated digital platform to:
- Deliver structured environmental education in an engaging way
- Assign, track, and reward real-world eco-actions from students
- Manage content, students, and progress independently per school
- Motivate students beyond passive reading and lectures

---

## 💡 Solution

EcoSphere bridges the gap between environmental awareness and student action. Students earn **EcoPoints** by completing modules, passing quizzes, and submitting eco-challenges with photo proof. A **10-level progression system**, daily streaks, and a **real-time leaderboard** drive continuous engagement.

The platform supports **multiple schools simultaneously** — each completely isolated from one another through a multi-tenant architecture enforced at both the application and database security level.

---

## 🏗️ How It Works

```
School Admin ──► Web Portal ──► Registers School ──► Gets Unique 14-char Code
                                                               │
              Lead Teacher ──► Joins App ──► Auto Approved ──►│
                                                               │
         Normal Teachers ──► Join App ──► Await Approval ─────┤
                                                               │
              Students ──► Join App ──► Select Batch ──► Start Learning
```

---

## 👥 User Roles

| Role | Capabilities |
|---|---|
| 🎓 **Student** | Learn modules, take quizzes, submit eco-challenges, earn EcoPoints, compete on leaderboard |
| 👨‍🏫 **Normal Teacher** | Full content management, review challenge submissions, manage batches, send announcements |
| 👑 **Lead Teacher** | Everything a teacher can do + approve/reject teacher registrations + platform settings |

---

## ✨ Features

### 🎓 Student Portal
| Feature | Description |
|---|---|
| Learning Modules | Structured content created by teachers, ordered and school-specific |
| Quiz Engine | Must pass configurable quiz threshold to unlock module completion |
| Eco-Challenges | Submit real-world eco-actions with photo proof, reviewed by teachers |
| EcoPoints System | Earn exact points set by teacher — no inflation, no duplicates |
| 10-Level Progression | Eco Seedling 🌱 → Eco Sprout → ... → Eco Master 🏆 |
| Real-time Leaderboard | Live school-scoped ranking via Firestore snapshot listeners |
| Daily Login Streak | Encourages consistent daily engagement |
| Progress Tracker | Module %, challenge %, quiz pass rate, overall completion |
| Announcements | School-specific messages from teachers, with unread badge |
| Push Notifications | FCM real-time alerts from teachers to all school students |
| Ecosystem Game | 8-scenario eco-decision strategy game — earn up to 10 points |
| Eco AI Assistant | Built-in chatbot for environmental queries |

### 👨‍🏫 Teacher Portal
| Feature | Description |
|---|---|
| Dashboard Analytics | Total students, active today, total school EcoPoints, avg completion % |
| Module Management | Create, edit, delete modules — fully scoped to the teacher's school |
| Quiz Management | Add, edit, delete questions per module with validation |
| Challenge Management | Create challenges with point values, toggle active/inactive |
| Review Challenges | Approve/reject with feedback — auto-awards/reverses EcoPoints |
| Send Announcements | School-scoped, reflects instantly on student portal |
| Push Notifications | FCM broadcast to all school students |
| Batch Management | Create and manage class batches, students register into them |
| Top Performers | View top 10 students ranked by EcoPoints |
| Live Activity Feed | Real-time student action feed, filtered by school |
| Teacher Approval | *(Lead Teacher)* Approve/reject pending registrations in real time |
| Platform Settings | *(Lead Teacher)* Quiz pass %, attempt limits, notification timing |

### 🌐 Web Portal
| Feature | Description |
|---|---|
| School Registration | Full school details form with validation |
| Unique Code Generation | Guaranteed 14-character alphanumeric school code |
| Firebase Integration | Saves directly to Firestore, accessible by the mobile app |
| Download Details | Export registration details as a text file |

---

## 🏆 Gamification — Level System

| Level | Title | EcoPoints Required |
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

> Level thresholds use an exponential curve — early levels are achievable quickly to keep new students motivated, while higher levels reward long-term sustained engagement.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android SDK 24–34 |
| UI Framework | Material Design 3, ViewBinding |
| Database | Firebase Firestore (real-time NoSQL) |
| Authentication | Firebase Auth (Email/Password) |
| File Storage | Firebase Storage |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Background Jobs | WorkManager (daily reminders) |
| Image Loading | Glide |
| Async Operations | Kotlin Coroutines |
| Architecture | MVVM, Repository Pattern |
| Web Portal | HTML, CSS, JavaScript + Firebase Web SDK |

---

## 📐 Technical Architecture Highlights

- **Multi-tenancy** — Every Firestore document tagged with `schoolId`. All queries filter by school. Students and teachers are physically incapable of accessing another school's data
- **Firestore Security Rules** — Server-side enforcement — even a direct API call bypassing the app cannot read another school's data
- **Real-time leaderboard** — `addSnapshotListener` pushes live updates to all connected students the moment any EcoPoint changes
- **Cascade delete** — Deleting a module or challenge automatically reverses all awarded student EcoPoints, removes completion records, and cleans quiz attempts
- **Transaction-safe points** — Firestore transactions prevent any possibility of duplicate EcoPoints
- **Role-based UI** — Lead Teacher features are hidden from Normal Teachers at runtime based on the `isLeadTeacher` flag from Firestore
- **Source.SERVER queries** — Critical progress screens bypass Firebase cache and always fetch fresh data

---

## 👨‍💻 My Contribution

This is a capstone group project. I was responsible for the **complete Android application**:

- Designed and built all 3 role-based portals from scratch (Student, Teacher, Lead Teacher)
- Architected the multi-tenant system with school-scoped data isolation across 15+ Firestore collections
- Built the gamification engine — EcoPoints, exponential level system, real-time leaderboard
- Developed the quiz engine with configurable pass thresholds, daily attempt limits, and anti-cheat transactions
- Implemented the full challenge submission → review → approval workflow with automatic point reversal on delete
- Integrated Firebase Auth, Firestore, Storage, and FCM end-to-end
- Wrote and deployed production-grade Firestore security rules
- Connected the web portal onboarding flow with the mobile app registration system

> The school registration web portal was built by my teammates.

---

## 📊 Project Scale

```
66+ Kotlin source files
75+ XML layout files
15+ Firestore collections with security rules
3 role-based portals
Unlimited schools supported on a single platform
```

---

<p align="center">Made with ❤️ for a greener future 🌱</p>
