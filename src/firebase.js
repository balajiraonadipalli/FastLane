// src/firebase.js
import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getAnalytics } from "firebase/analytics";

const firebaseConfig = {
  apiKey: "AIzaSyBUNjONdpkOrOSVDWbfnwM0MlyK-cmsaWs",
  authDomain: "fastlane-64285.firebaseapp.com",
  databaseURL: "https://fastlane-64285-default-rtdb.firebaseio.com",
  projectId: "fastlane-64285",
  storageBucket: "fastlane-64285.firebasestorage.app",
  messagingSenderId: "184541049387",
  appId: "1:184541049387:web:3264d96a395e3fa72af06e",
  measurementId: "G-NYTCW5Q2ZR"
};

// Initialize Firebase
const firebaseApp = initializeApp(firebaseConfig);

// Initialize services
const auth = getAuth(firebaseApp);
const analytics = getAnalytics(firebaseApp);

// Export services
export { auth, analytics, firebaseApp };