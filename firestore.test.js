const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_betzone_1";
const BOB_UID = "bob_betzone_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid, now) {
  return {
    userId: uid,
    displayName: "Aarav Striker",
    avatarKey: "cobalt_falcon",
    phoneNumber: "+919876543210",
    countryCode: "+91",
    virtualPoints: 2500,
    totalPredictions: 0,
    correctPredictions: 0,
    streakDays: 1,
    favoriteSports: ["Cricket", "Football"],
    favoriteTeams: ["Mumbai Titans", "Real Madrid"],
    badges: ["ROOKIE_PREDICTOR"],
    language: "English",
    darkMode: true,
    notificationsEnabled: true,
    privacyPublicLeaderboard: true,
    termsAccepted: true,
    createdAt: now,
    updatedAt: now,
  };
}

function validPrediction(uid, now) {
  return {
    userId: uid,
    matchId: "ipl_mi_csk_01",
    matchTitle: "Mumbai Titans vs Chennai Kings",
    sport: "Cricket",
    tournament: "IPL",
    marketType: "Match Winner",
    selectedPick: "Mumbai Titans",
    stakePoints: 250,
    multiplierX100: 185,
    potentialRewardPoints: 462,
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  };
}

function validLeaderboard(uid, now, publicVisible = true) {
  return {
    userId: uid,
    displayName: "Aarav Striker",
    avatarKey: "cobalt_falcon",
    virtualPoints: 2500,
    totalPredictions: 10,
    correctPredictions: 7,
    accuracyPct: 70,
    badges: ["ROOKIE_PREDICTOR"],
    periodBucket: "ALL",
    publicVisible,
    createdAt: now,
    updatedAt: now,
  };
}

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

test("Unauthenticated users cannot read or write any BetZone X collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, now)));
  await assertFails(unauthDb.collection("leaderboard_entries").where("publicVisible", "==", true).get());
});

test("User profile enforces strict PII isolation, shadow field rejection, and partial updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, created)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      darkMode: false,
      language: "Hindi",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user PII read/write blocked
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ virtualPoints: 999999, updatedAt: new Date(Date.now() - 1000) }));

  // Shadow field & @DocumentId id field & future timestamp rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Predictions enforce ownership, terminal state locking, and forbid @DocumentId id key", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").doc("pred_1").set(validPrediction(ALICE_UID, created))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").get()
  );

  // Settle ACTIVE -> WON succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").doc("pred_1").update({
      status: "WON",
      resultSummary: "Mumbai Titans won by 18 runs",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Terminal state lock: WON cannot be re-settled or modified
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").doc("pred_1").update({
      status: "LOST",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cannot include @DocumentId `id` field or cross-user read
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("predictions").doc("pred_2").set({
      id: "pred_2",
      ...validPrediction(ALICE_UID, created),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("predictions").get());
});

test("Reward claims and notifications enforce ownership and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("rewards_claims").doc("daily_2026_11_24").set({
      userId: ALICE_UID,
      rewardType: "DAILY_LOGIN",
      rewardKey: "daily_2026_11_24",
      title: "Day 1 Check-in Bonus",
      pointsAwarded: 150,
      createdAt: created,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("rewards_claims").doc("daily_2026_11_24").update({
      pointsAwarded: 5000,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").set({
      userId: ALICE_UID,
      title: "Match Live",
      body: "IPL clash is now live.",
      category: "MATCH_LIVE",
      matchId: "ipl_mi_csk_01",
      unread: true,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").update({
      unread: false,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Leaderboard enforces filtered publicVisible query and prevents cross-user tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("leaderboard_entries").doc(ALICE_UID).set(validLeaderboard(ALICE_UID, created, true))
  );
  await assertSucceeds(
    bobDb.collection("leaderboard_entries").doc(BOB_UID).set(validLeaderboard(BOB_UID, created, false))
  );

  // Filtered query matching Kotlin repository succeeds
  await assertSucceeds(
    bobDb.collection("leaderboard_entries").where("publicVisible", "==", true).get()
  );

  // Unfiltered list or private user get rejected
  await assertFails(bobDb.collection("leaderboard_entries").get());
  await assertFails(aliceDb.collection("leaderboard_entries").doc(BOB_UID).get());
  await assertFails(
    bobDb.collection("leaderboard_entries").doc(ALICE_UID).update({
      virtualPoints: 10,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
