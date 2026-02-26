const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

// This function wakes up every time a new document is added to your "notifications" collection!
exports.sendPartyAlert = onDocumentCreated("notifications/{notificationId}", async (event) => {
    // Grab the data your Android app just saved
    const notificationData = event.data.data();

    if (!notificationData) {
        return null; // Safety check
    }

    // Build the payload for the push notification
    const payload = {
        notification: {
            title: notificationData.title || "New Party Alert!",
            body: notificationData.message || "Open WeParty to see details.",
        },
        topic: "party_alerts" // This blasts it to everyone who flipped your switch!
    };

    try {
        // Send the push notification
        const response = await admin.messaging().send(payload);
        console.log("Successfully sent push notification:", response);
        return null;
    } catch (error) {
        console.error("Error sending push notification:", error);
        return null;
    }
});