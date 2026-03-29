const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

// Initialize the Firebase Admin SDK
admin.initializeApp();

// Function 1: The Global Bridge - Triggers for all app notifications
exports.onNotificationCreated = onDocumentCreated("notifications/{notifId}", async (event) => {
    const data = event.data.data();
    const allowedUsers = data.allowedUsers || [];

    // If there's no one to notify, stop here.
    if (allowedUsers.length === 0) {
        console.log("No users attached to this notification document. Aborting.");
        return null;
    }

    const tokens = [];

    // 1. Loop through the allowed users and fetch their FCM Tokens
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
            console.error(`Error fetching token for user ${userId}:`, error);
        }
    }

    // If we didn't find any valid device tokens, stop here.
    if (tokens.length === 0) {
        console.log("No valid device tokens found for recipients.");
        return null;
    }

    // 2. Build the Notification Payload
    const payload = {
        notification: {
            title: data.title || "WeParty Update",
            body: data.message || "Tap to see what's happening!"
        },
        tokens: tokens
    };

    // 3. Send the targeted Multicast message to physical devices
    try {
        const response = await admin.messaging().sendEachForMulticast(payload);
        console.log(`Successfully delivered ${response.successCount} push notifications.`);
    } catch (error) {
        console.error("Critical error in Global Bridge delivery:", error);
    }
});
// FUNCTION 2: 24-Hour Reminder - Runs at the top of every hour
exports.send24HourReminders = onSchedule({
    schedule: "0 * * * *",
    timeZone: "UTC" // Manual TZ calculation used below
}, async (event) => {

    console.log("Waking up to check for 12:00 PM local time zones...");

    const messages = [];

    try {
        // 1. Grab every user from the database
        const usersSnapshot = await admin.firestore().collection('users').get();

        // 2. Loop through every single user
        for (const userDoc of usersSnapshot.docs) {
            const userData = userDoc.data();

            // If they don't have a token, skip them
            if (!userData.fcmToken) continue;

            const userTz = userData.timeZone || "America/New_York";
            let userDate;
            let currentHour;

            // Wrap timezone generation in a try/catch to prevent fatal crashes
            try {
                userDate = new Date(new Date().toLocaleString("en-US", {timeZone: userTz}));
                currentHour = userDate.getHours();
            } catch (tzError) {
                console.warn(`Invalid timezone [${userTz}] for user ${userDoc.id}. Skipping.`);
                continue;
            }

            // 3. Only proceed if the clock just struck 12:00 PM for them
            if (currentHour === 12) {

                // Figure out the exact date for their tomorrow (YYYY-MM-DD)
                userDate.setDate(userDate.getDate() + 1);
                const y = userDate.getFullYear();
                const m = String(userDate.getMonth() + 1).padStart(2, '0');
                const d = String(userDate.getDate()).padStart(2, '0');
                const theirTomorrowString = `${y}-${m}-${d}`;

                // Only query events happening on their tomorrow
                const eventsSnapshot = await admin.firestore().collection('events')
                    .where('date', '==', theirTomorrowString)
                    .get();

                // 4. Look through the matched events
                for (const eventDoc of eventsSnapshot.docs) {
                    const eventData = eventDoc.data();

                    // Are they the host or are they on the attending list?
                    const isHost = eventData.hostId === userDoc.id;
                    const isAttending = eventData.attending && eventData.attending[userDoc.id];

                    if (isHost || isAttending) {
                        messages.push({
                            token: userData.fcmToken,
                            notification: {
                                title: `Reminder: ${eventData.name || "Your event"} is Tomorrow!`,
                                body: `Get ready! Your party starts tomorrow at ${eventData.time || "TBD"}.`
                            },
                            data: { eventId: eventDoc.id }
                        });
                    }
                }
            }
        }

        // 5. Send the batch of messages for this specific hour
        if (messages.length > 0) {
            try {
                const response = await admin.messaging().sendEach(messages);
                console.log(`Sent ${response.successCount} local-time reminders. Failed: ${response.failureCount}`);
            } catch (sendError) {
                console.error("Critical error while sending batch messages:", sendError);
            }
        } else {
            console.log("Nobody is experiencing 12:00 PM with a party tomorrow. Going back to sleep.");
        }

    } catch (globalError) {
        console.error("Critical failure executing the 24-hour reminder sweep:", globalError);
    }

    return null;
});