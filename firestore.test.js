const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: "buildolive4@gmail.com",
    email_verified: true,
  });
}

test("Unauthenticated requests are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("hosts").get());
  await assertFails(unauthDb.collection("wallets").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("calls").where("participantUids", "array-contains", ALICE_UID).get());
});

test("User profile enforces PII isolation, role escalation guard, and partial update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Regular user cannot self-assign ADMIN role
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      role: "ADMIN",
      accountStatus: "ACTIVE",
      termsAccepted: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Valid CALLER profile creation (no @DocumentId `id` key)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      phoneNumber: "9876543210",
      countryCode: "+91",
      language: "English",
      role: "CALLER",
      accountStatus: "ACTIVE",
      termsAccepted: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Partial profile update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice V",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot read or list Alice's private PII profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());
});

test("Email spoofing with email_verified=false does not grant admin access", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: "buildolive4@gmail.com",
    email_verified: false,
  }).firestore();

  const past = new Date(Date.now() - 5000);
  await assertFails(
    spoofDb.collection("host_frames").doc("frame_star").set({
      name: "STAR",
      tier: "STAR",
      accentHex: "#FFD36E",
      secondaryHex: "#8B5CFF",
      animationStyle: "PARTICLES",
      active: true,
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(spoofDb.collection("admin_audit_logs").get());
});

test("Host frames, call pricing, and salaries are strictly admin-controlled", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const admDb = adminContext().firestore();
  const past = new Date(Date.now() - 5000);

  // Non-admin cannot create or alter host frames or pricing
  await assertFails(
    aliceDb.collection("host_frames").doc("frame_gold").set({
      name: "GOLD",
      tier: "GOLD",
      accentHex: "#FFD36E",
      secondaryHex: "#FF4FA3",
      animationStyle: "SHIMMER",
      active: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertFails(
    aliceDb.collection("app_settings").doc("pricing").set({
      audioRatePerMin: 20,
      videoRatePerMin: 60,
      minBillingSeconds: 60,
      billingIntervalSeconds: 60,
      platformCommissionPct: 30,
      hostSharePct: 70,
      updatedAt: past,
    })
  );

  // Verified admin succeeds
  await assertSucceeds(
    admDb.collection("host_frames").doc("frame_gold").set({
      name: "GOLD",
      tier: "GOLD",
      accentHex: "#FFD36E",
      secondaryHex: "#FF4FA3",
      animationStyle: "SHIMMER",
      active: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    admDb.collection("app_settings").doc("pricing").set({
      audioRatePerMin: 20,
      videoRatePerMin: 60,
      minBillingSeconds: 60,
      billingIntervalSeconds: 60,
      platformCommissionPct: 30,
      hostSharePct: 70,
      updatedAt: past,
    })
  );

  // Host created by admin; host owner cannot upgrade own frameTier or status
  await assertSucceeds(
    admDb.collection("hosts").doc("host_1").set({
      ownerUid: BOB_UID,
      hostCode: "VBR-H101",
      name: "Aria Nova",
      bio: "Crystal voice host",
      avatarKey: "aurora",
      frameTier: "SILVER",
      status: "APPROVED",
      onlineStatus: "ONLINE",
      audioAvailable: true,
      videoAvailable: true,
      featuredHost: true,
      popularHost: true,
      newHost: false,
      premiumTier: true,
      audioRatePerMin: 20,
      videoRatePerMin: 60,
      rating: 4.9,
      totalCalls: 12,
      totalMinutes: 84,
      createdAt: past,
      updatedAt: past,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("hosts").doc("host_1").update({
      onlineStatus: "BUSY",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    bobDb.collection("hosts").doc("host_1").update({
      frameTier: "STAR",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Host cannot create or modify salary payouts
  await assertFails(
    bobDb.collection("host_salaries").doc("sal_1").set({
      hostId: "host_1",
      hostOwnerUid: BOB_UID,
      hostName: "Aria Nova",
      periodLabel: "Nov 2026",
      approvedAmountInr: 12000,
      pendingAmountInr: 0,
      paidAmountInr: 12000,
      payoutMethod: "UPI",
      status: "PAID",
      createdAt: past,
      updatedAt: past,
    })
  );
});

test("Wallet, payments, calls, chats, and consented recordings enforce ownership and query alignment", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Wallet initial creation must start at 0 for non-admin
  await assertFails(
    aliceDb.collection("wallets").doc(ALICE_UID).set({
      userId: ALICE_UID,
      balanceCoins: 99999,
      totalRechargedInr: 0,
      totalSpentCoins: 0,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    aliceDb.collection("wallets").doc(ALICE_UID).set({
      userId: ALICE_UID,
      balanceCoins: 0,
      totalRechargedInr: 0,
      totalSpentCoins: 0,
      updatedAt: past,
    })
  );

  // Unfiltered query on wallet_transactions fails; filtered query succeeds
  await assertFails(aliceDb.collection("wallet_transactions").get());
  await assertSucceeds(
    aliceDb.collection("wallet_transactions").where("userId", "==", ALICE_UID).get()
  );

  // Call session & participant-scoped list query
  await assertSucceeds(
    aliceDb.collection("calls").doc("call_1").set({
      callerId: ALICE_UID,
      callerName: "Alice",
      hostId: "host_1",
      hostOwnerUid: BOB_UID,
      hostName: "Aria Nova",
      callType: "AUDIO",
      status: "ACTIVE",
      ratePerMin: 20,
      durationSeconds: 0,
      billedCoins: 0,
      hostEarnedCoins: 0,
      platformCommissionCoins: 0,
      networkQuality: "EXCELLENT",
      callerConsentRecording: true,
      hostConsentRecording: false,
      recordingActive: false,
      participantUids: [ALICE_UID, BOB_UID],
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    bobDb.collection("calls").where("participantUids", "array-contains", BOB_UID).get()
  );
  await assertFails(bobDb.collection("calls").get());

  // Consented recording requires BOTH callerConsented and hostConsented == true
  await assertFails(
    aliceDb.collection("consented_recordings").doc("rec_bad").set({
      callId: "call_1",
      callerId: ALICE_UID,
      hostId: "host_1",
      hostOwnerUid: BOB_UID,
      callerConsented: true,
      hostConsented: false,
      durationSeconds: 60,
      retentionDays: 30,
      accessLogCount: 0,
      status: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    aliceDb.collection("consented_recordings").doc("rec_ok").set({
      callId: "call_1",
      callerId: ALICE_UID,
      hostId: "host_1",
      hostOwnerUid: BOB_UID,
      callerConsented: true,
      hostConsented: true,
      durationSeconds: 60,
      retentionDays: 30,
      accessLogCount: 0,
      status: "ACTIVE",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Chat thread + denormalized participantUids message subcollection
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").set({
      participantUids: [ALICE_UID, BOB_UID],
      callerId: ALICE_UID,
      callerName: "Alice",
      hostId: "host_1",
      hostOwnerUid: BOB_UID,
      hostName: "Aria Nova",
      lastMessage: "Hello!",
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc("chat_1").collection("messages").doc("msg_1").set({
      participantUids: [ALICE_UID, BOB_UID],
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Hello!",
      messageType: "TEXT",
      deliveryStatus: "SENT",
      createdAt: past,
    })
  );
  await assertSucceeds(
    bobDb.collection("chats").doc("chat_1").collection("messages")
      .where("participantUids", "array-contains", BOB_UID)
      .get()
  );
});
