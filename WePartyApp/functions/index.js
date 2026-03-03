const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK
admin.initializeApp();

// Notice we changed the trigger to "events" so we can read the guest list
exports.sendTargetedPartyAlert = onDocumentCreated("events/{eventId}", async (event) => {
    // Get the data from the newly created event document
    const partyData = event.data.data();

    // 1. Get the Host and the Guest List
    const hostId = partyData.hostId;
    const guests = partyData.invitedGuests || [];

    // 2. Combine them into one "Allowed" list of User IDs
    const allowedUsers = [];
    if (hostId) allowedUsers.push(hostId);
    allowedUsers.push(...guests);

    // If no one is invited and no host is listed, stop the function here.
    if (allowedUsers.length === 0) {
        console.log("No users attached to this event. Aborting notification.");
        return null;
    }

    const tokens = [];

    // 3. Loop through the allowed users and fetch their FCM Tokens from the 'users' collection
    for (const userId of allowedUsers) {
        try {
            const userDoc = await admin.firestore().collection("users").doc(userId).get();
            if (userDoc.exists) {
                const userData = userDoc.data();
                // If the user has a token saved, add it to our delivery list
                if (userData.fcmToken) {
                    tokens.push(userData.fcmToken);
                }
            }
        } catch (error) {
            console.error(`Error fetching data for user ${userId}:`, error);
        }
    }

    // If we didn't find any valid device tokens, stop here.
    if (tokens.length === 0) {
        console.log("No valid device tokens found for the users in this event.");
        return null;
    }

    // 4. Build the Notification Payload
    const payload = {
        notification: {
            title: "New Party Alert!",
            body: `${partyData.name} is happening! Tap to see details.`
        },
        tokens: tokens // <-- This targets only the specific devices in the array.
    };

    // 5. Send the targeted Multicast message
    try {
        const response = await admin.messaging().sendEachForMulticast(payload);
        console.log(`Successfully sent ${response.successCount} notifications.`);
        if (response.failureCount > 0) {
            console.log(`Failed to send ${response.failureCount} notifications.`);
        }
    } catch (error) {
        console.error("Error sending targeted notifications:", error);
    }
});