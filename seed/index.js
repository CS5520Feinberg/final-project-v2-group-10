const admin = require('firebase-admin');
const fs = require('fs');
const serviceAccount = require('<PATH_TO_SERVICE_ACCOUNT_CREDENTIALS>');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "<DATABASE_URL>",
    storageBucket: "<STORAGE_BUCKET>"

});

const db = admin.firestore();
const rtdb = admin.database();

const postTypes = ["offerProductPosts", "needProductPosts", "offerServicePosts", "needServicePosts"];
const placeholderImagePath = './placeholderImage.jpg';
const avatarImagePath = './avatarImage.jpg';

async function clearData() {
    // Clear Firestore Data
    for (const type of postTypes) {
        const snapshot = await db.collection('posts').doc(type).collection('posts').get();
        snapshot.forEach(doc => {
            doc.ref.delete();
        });
    }

    // Clear Realtime Database Data
    await rtdb.ref('users').remove();
    await rtdb.ref('posts').remove();


    // Clear Firebase Storage Data
    const bucket = admin.storage().bucket();

    // Deleting post images
    for (const type of postTypes) {
        const [files] = await bucket.getFiles({ prefix: `images/${type}/` });
        for (const file of files) {
            await file.delete();
        }
    }

    // Deleting avatars
    const [avatarFiles] = await bucket.getFiles({ prefix: 'images/avatar/' });
    for (const file of avatarFiles) {
        await file.delete();
    }

    // Clear Firebase Authentication Users
    const listUsersResult = await admin.auth().listUsers(1000); // Adjust the number as per your needs
    listUsersResult.users.forEach(async (userRecord) => {
        await admin.auth().deleteUser(userRecord.uid);
    });
}


// Create's 10 users, and 4 posts (of each type) for each user
async function seedData() {
    const bucket = admin.storage().bucket();

    for (let i = 1; i <= 10; i++) {

        // Create user in Firebase Authentication
        const userCredential = await admin.auth().createUser({
            email: `user${i}@example.com`,
            emailVerified: false,
            password: 'password',
        });

        const userId = userCredential.uid; // This is the generated UID from Firebase Authentication

        // Seeding user data in Realtime DB
        const user = new User(`user${i}@example.com`);
        user.name = `User ${i}`;
        await rtdb.ref(`users/${userId}`).set(user);

        // Upload avatar to Firebase Storage
        await bucket.upload(avatarImagePath, {
            destination: `images/avatar/${userId}.jpg`
        });

        for (const type of postTypes) {
            for (let j = 0; j <= 3; j++) {
                const postId = `${type}${j}`;

                // Seeding post data in Firestore
                const post = new Post(j, userId, `Title ${postId}`, `This is a random post text for ${postId}`, (i * 10 + j), postId, 1);
                await db.collection('posts').doc(type).collection('posts').doc(postId).set(post.toMap());

                // Seeding post data in Realtime DB
                await rtdb.ref(`posts/${type}/${postId}`).set(post);

                // Upload post images to Firebase Storage
                await bucket.upload(placeholderImagePath, {
                    destination: `images/${type}/${postId}/original.jpg`
                });
                await bucket.upload(placeholderImagePath, {
                    destination: `images/${type}/${postId}/small.jpg`
                });
            }
        }
    }
}


// Execute
(async function () {
    await clearData();
    await seedData();
    console.log('Seeding completed!');
})();



// Entity classes (copied from the Java classes we have)
class Post {
    constructor(type, userId, title, text, price, postId, cnt) {
        this.type = type; // ["OfferProduct", "NeedProduct","OfferService", "NeedService"]
        this.userId = userId;
        this.images = [];
        this.smallImages = [];
        this.title = title;
        this.text = text;
        this.price = price;
        this.replies = {};
        this.isActive = true;
        this.postId = postId;
        this.imgCnt = cnt;
    }

    toMap() {
        let map = {
            type: this.type,
            userId: this.userId,
            title: this.title,
            text: this.text,
            price: this.price,
            isActive: this.isActive,
            timestamp: new Date(), // You might want to adjust this to use Firebase Server Timestamps
            postId: this.postId,
            imgCnt: this.imgCnt,
        };
        
        if (this.images.length > 0) {
            map.images = this.images;
        }
        
        if (this.smallImages.length > 0) {
            map.smallImages = this.smallImages;
        }
        
        if (Object.keys(this.replies).length > 0) {
            map.replies = this.replies;
        }
        
        return map;
    }
}

class User {
    constructor(email, name = "", campus = "") {
        this.avatar = ""; // user icon
        this.campus = campus;
        this.name = name;
        this.email = email;
        this.offerServicePosts = {};
        this.wantServicePosts = {};
        this.offerProductPosts = {};
        this.wantProductPosts = {};
        this.chats = {};
    }

    getAvatar() {
        return this.avatar;
    }

    setAvatar(avatar) {
        this.avatar = avatar;
    }

    getCampus() {
        return this.campus;
    }

    setCampus(campus) {
        this.campus = campus;
    }

    getName() {
        return this.name;
    }

    setName(name) {
        this.name = name;
    }

    getEmail() {
        return this.email;
    }

    setEmail(email) {
        this.email = email;
    }
}
