# 🚨 FastLane – Smart Emergency Route Management

FastLane is a real-time location tracking and route-clearing system designed to help emergency vehicles like ambulances move quickly by notifying nearby vehicles and toll gates to clear the path.

## 🚀 Features

- 📍 Real-time location tracking of emergency vehicles
- 🧭 Google Maps integration with live markers
- 🔔 Proximity alerts to nearby users (within 10-20 meters)
- 🛣️ Route clearance notifications (for vehicles/toll operators)
- 🔒 Firebase Firestore backend for seamless data updates

## 🛠️ Tech Stack

- **Frontend**: React.js
- **Maps**: Google Maps JavaScript API
- **Backend**: Firebase Firestore (NoSQL)
- **Other Tools**: Geolocation API, Geometry library (Google Maps), Vite

## 📸 Screenshots

![Live Tracking Map](./assets/map-tracking.png)
*Live vehicle tracking on map with distance-based alerts*

## ⚙️ How It Works

1. **Emergency Vehicle** updates its current location in Firestore.
2. **User Devices** listen for changes and calculate the distance using Google Maps Geometry API.
3. If within a defined radius (e.g., 20 meters), a notification sound and visual alert is triggered.
4. Routes are dynamically displayed and updated as the vehicle moves.

## 🔧 Setup Instructions

1. Clone the repo
   ```bash
   git clone https://github.com/your-username/fastlane.git
   cd fastlane
