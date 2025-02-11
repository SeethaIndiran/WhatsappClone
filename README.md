# 📱 WhatsApp Clone - Android Chat App
## 🔥 Overview
   This is a fully functional WhatsApp Clone built using Kotlin and modern Android development tools. The app replicates most of the core functionalities of WhatsApp, including real-time chat, audio/video calling, media sharing, status updates, and notifications. It follows MVVM architecture with Dagger Hilt for dependency injection, ensuring clean code and scalability.
   The app utilizes Firebase Firestore for real-time data synchronization, Firebase Authentication for secure user login, and WebRTC for seamless audio/video calling. Additionally, users can send text messages, images, videos, and audio files, along with real-time message status (sent, delivered, read)

## 🎯 Features

### ✅ Authentication
   * Firebase Authentication for secure signup and logins.
   * Google Sign-In integration (optional)
   * User profile setup with profile picture and status

### 💬 Real-time Chat
   * One-to-one instant messaging
   * Send & receive images, videos, and audio files
   * Real-time message status: Sent, Delivered, Read
   * Typing indicator support
   * Message reactions and emojis
   * Message deletion (delete for everyone & delete for self)
   * Multiple message selection for bulk actions
   * Forward messages to other users

### 📞 Audio & Video Calling
   * WebRTC-powered high-quality audio/video calls
   * Firebase Cloud Messaging (FCM) for call notifications
   * Call history and logs
   * In-call UI with mute, speaker, and camera toggle

###  📸 Status Feature (Stories)
   * Upload images, videos, and text status
   * View other users' statuses
   * Status disappears automatically after 24 hours
   * Status privacy settings (who can see your status)
   * Status views tracking

### 🗂 Media Sharing
   * Send and receive images, videos, and audio
   * Gallery preview before sending
   * In-app image viewer and video player

### 🔔 Push Notifications
   * Firebase Cloud Messaging (FCM) integration
   * Get real-time notifications for new messages, calls, and status updates

### 📂 File Management
   * Download media files to device storage
   * Access media files from chat history
   * Clear chat history and manage storage

### 📝 User Profile & Settings
   * Edit profile picture, username, and status
   * Privacy settings (Last seen, Profile photo visibility, Read receipts)
   * Block & unblock users

### 🛠 Other Features
   * Search functionality for chats and contacts
   * Dark Mode support
   * Online/offline status visibility
   * Auto-delete messages after a set time

## 🏗 Tech Stack
### 📌 Frontend & UI
    * Kotlin (Primary language)
    * XML-based UI
    * ViewModel, LiveData, MutableStateFlow
    * ConstraintLayout & Material Design

### 📌 Backend & Database
    *  Firebase Firestore (Real-time database)
    * Firebase Authentication (User login & sign-up)
    * Firebase Storage (For media file uploads)
    * Firebase Cloud Messaging (FCM) (Push notifications)

### 📌 Networking & APIs
    * Retrofit (For network requests)
    * WebRTC (For real-time audio/video calls)
    * Glide (For image loading)

### 📌 Dependency Injection & Architecture
    * MVVM Architecture (Clean & scalable)
    * Dagger Hilt (Dependency Injection)

### 📌 Other Libraries & Tools
    * Lottie Animations (For smooth UI animations)
    * ExoPlayer (For video playback)
    * Coroutine & Flow (For asynchronous programming)
    * Room Database (For local chat history storage)
    * Encryption (End-to-end encryption for chat security)

## 🔧 Prerequisites
    
    
    
     
