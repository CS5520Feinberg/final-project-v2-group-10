# Firebase Seeding Script - Setup and Execution Guide

This README outlines the necessary steps for setting up and executing the Firebase seeding script. The script is designed to populate your Firebase project with sample data for testing purposes.


## Prerequisites

- Node.js: Ensure you have Node.js installed on your local machine. If not, download and install it from here.

- Firebase Admin SDK: You'll need to have the Firebase Admin SDK installed. If not installed, you can add it via npm:

```npm install firebase-admin```

-Firebase Service Account Credentials: You should have Firebase Admin access to the Firebase project and be able to download the service account credentials.


## Steps to Run the Script

#### Download Service Account Credentials:

- Navigate to the Firebase Console.
- Choose your project.
- Click on the gear icon ⚙️ next to Project Overview and select Project settings.
- Navigate to the Service accounts tab.
- Click on Generate new private key.
- A JSON file will be downloaded. Save this safely, as it provides full admin access to your Firebase project.
- save the file as ```credentials.json``` and make sure it's not being committed 

#### Place the Service Account Credentials in the Project:

- Move the downloaded JSON file into your project directory.
- Update the script's serviceAccount line to point to the path of your JSON file:
```const serviceAccount = require('<PATH_TO_YOUR_DOWNLOADED_SERVICE_ACCOUNT_CREDENTIALS>');```


#### Update Database URL and Storage Bucket:


- In the script, replace <DATABASE_URL> with your Firebase Realtime Database URL. This usually looks like: https://YOUR-PROJECT-ID.firebaseio.com.
- Replace <STORAGE_BUCKET> with your Firebase Storage bucket URL. It's typically formatted as YOUR-PROJECT-ID.appspot.com.



``` 
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://YOUR-PROJECT-ID.firebaseio.com",
    storageBucket: "YOUR-PROJECT-ID.appspot.com"
}); 
```

#### Run the Script

- Navigate to the seed directory
- Run command: ```npm run seed```




