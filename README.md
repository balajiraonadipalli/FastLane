# 🚨 FastLane - Smart Emergency Toll Alert System

FastLane is an innovative software-only solution built to **automatically alert and control toll barriers for emergency vehicles** like ambulances, fire trucks, and police vehicles—eliminating manual intervention and reducing delays during critical situations.

## 🚀 Overview

In many regions, emergency vehicles must wait at tolls for barriers to be manually lifted, especially in lanes not equipped for FASTag. FastLane bridges this gap using a **mobile app** for emergency drivers and a **real-time alert dashboard** for toll operators.

## 🧩 Key Features

- 📍 **Live Location Tracking**: Sends real-time GPS data of the emergency vehicle to nearby tolls.
- 🛑 **Automated Toll Alerts**: Toll dashboard receives instant alerts with vehicle ETA and location.
- 🧠 **Smart Barrier Control**: Reduces manual dependency by notifying staff in advance.
- 🔒 **Secure Access Logs**: Tracks which barriers were lifted and when.

## 🛠️ Tech Stack

| Component          | Technology Used          |
|--------------------|--------------------------|
| Mobile App         | Jetpack Compose (Android)|
| Dashboard          | ReactJS + Tailwind CSS   |
| Backend/API        | Node.js + Express        |
| Database           | Firebase Firestore       |
| Maps & Location    | Google Maps API          |
| Deployment         | Firebase Hosting         |

## 📸 UI Samples

| Emergency App | Admin Dashboard |
|---------------|------------------|
| ![App Screenshot](link) | ![Dashboard Screenshot](link) |

## 🧪 How It Works

1. Emergency vehicle taps "Initiate Emergency" on the mobile app.
2. GPS coordinates are sent to backend and matched to nearby tollgates.
3. Dashboard at those tolls flashes alert and shows ETA.
4. Staff lift barricades in time to ensure seamless vehicle passage.

## 💡 Real-World Impact

- Reduces **manual coordination delays** for emergency vehicles.
- Ensures **safe and swift passage** through toll barriers.
- Scalable solution for **national highway emergency protocols**.
