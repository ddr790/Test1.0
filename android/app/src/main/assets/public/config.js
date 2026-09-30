// Firebase Web configuration for the ATLAS app.
// These values are Firebase's public web-app configuration and are safe to ship
// to the browser. Authentication security is enforced by Firebase rules/providers.
window.ATLAS_CONFIG={firebase:{apiKey:"AIzaSyBvPeD3bK456OOVD5mdUnmGZNkp0wBUOzU",authDomain:"atlas-9f28f.firebaseapp.com",projectId:"atlas-9f28f",storageBucket:"atlas-9f28f.firebasestorage.app",messagingSenderId:"69441691797",appId:"1:69441691797:web:ddc0dba7dbdfb977b7fb9c",measurementId:"G-20P74J8S77"}};

// Image generation configuration (user-provided key).
window.ATLAS_IMAGE_CONFIG={apiKey:"",model:"gemini-3.1-flash-image"};

// Google Calendar API configuration. OAuth is still required for private calendars.
window.ATLAS_CALENDAR_CONFIG={apiKey:"AIzaSyB5XnPblw5SY2ERLSwk10jJB5yWB6-OABs"};

// LINE integration: keep the Channel Secret on a server; never ship it in the APK.
window.ATLAS_LINE_CONFIG={channelId:"2011805927",shareEnabled:true};

// Optional agentic automation backend. Leave empty to use local ATLAS actions.
window.ATLAS_AGENT_CONFIG={endpoint:""};
