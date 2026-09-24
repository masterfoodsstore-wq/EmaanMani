const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * Trigger: On User Signup (Firebase Auth)
 * Automatically seeds the user profile in Cloud Firestore with the initial 20 RS Welcome Bonus.
 */
exports.onUserCreated = functions.auth.user().onCreate(async (user) => {
  const uid = user.uid;
  const email = user.email || "";
  const displayName = user.displayName || email.split("@")[0] || "Player_" + uid.substring(0, 5);

  const initialBonus = 20; // 20 RS welcome bonus

  const userDocRef = db.collection("users").doc(uid);
  const now = Date.now();

  try {
    await userDocRef.set({
      uid: uid,
      id: Math.floor(1000 + Math.random() * 90000), // Numeric player identifier
      username: displayName,
      email: email,
      avatar: "🦁",
      balance: initialBonus,
      status: "ACTIVE", // ACTIVE | SUSPENDED | BLOCKED
      isAdmin: false,
      role: "user",
      createdAt: now,
      lastLoginAt: now,
      welcomeBonusClaimed: true
    }, { merge: true });

    // Record welcome bonus transaction
    await db.collection("transactions").add({
      trxId: "BONUS_" + uid.substring(0, 6) + "_" + now,
      userId: uid,
      userName: displayName,
      type: "BONUS",
      amount: initialBonus,
      balanceBefore: 0,
      balanceAfter: initialBonus,
      paymentMethod: "WelcomeBonus",
      status: "APPROVED",
      note: "Initial 20 RS Registration Gift",
      createdAt: now
    });

    console.log(`Successfully created Firestore profile for user ${uid}`);
  } catch (error) {
    console.error(`Error initializing user document for ${uid}:`, error);
  }
});

/**
 * Callable: Promote User to Admin
 * Verifies caller authorization and grants admin custom claim and updates Firestore.
 */
exports.makeAdmin = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const callerUid = context.auth.uid;
  const callerEmail = context.auth.token.email || "";

  // Check if caller is already an admin or master admin
  const isMaster = callerEmail === "admin@zoo3d.game" || callerEmail === "admin@royalx.com";
  const callerUserDoc = await db.collection("users").doc(callerUid).get();
  const isCallerAdmin = callerUserDoc.exists && callerUserDoc.data().isAdmin === true;

  if (!isMaster && !isCallerAdmin) {
    throw new functions.https.HttpsError("permission-denied", "Only administrators can grant admin privileges");
  }

  const targetUid = data.targetUid;
  if (!targetUid) {
    throw new functions.https.HttpsError("invalid-argument", "Target UID must be provided");
  }

  await admin.auth().setCustomUserClaims(targetUid, { admin: true });
  await db.collection("users").doc(targetUid).update({
    isAdmin: true,
    role: "admin"
  });

  return { success: true, message: `User ${targetUid} granted admin role.` };
});

/**
 * Callable: Admin Approve Deposit
 * Atomically credits user balance and marks transaction as APPROVED.
 */
exports.adminApproveDeposit = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const { transactionId, adminNote } = data;
  if (!transactionId) {
    throw new functions.https.HttpsError("invalid-argument", "Transaction ID is required");
  }

  const trxRef = db.collection("transactions").doc(transactionId);

  return await db.runTransaction(async (transaction) => {
    const trxSnap = await transaction.get(trxRef);
    if (!trxSnap.exists) {
      throw new functions.https.HttpsError("not-found", "Transaction not found");
    }

    const trxData = trxSnap.data();
    if (trxData.status !== "PENDING") {
      throw new functions.https.HttpsError("failed-precondition", `Transaction already ${trxData.status}`);
    }

    const userRef = db.collection("users").doc(trxData.userId);
    const userSnap = await transaction.get(userRef);
    if (!userSnap.exists) {
      throw new functions.https.HttpsError("not-found", "User account not found");
    }

    const userData = userSnap.data();
    const currentBalance = Number(userData.balance) || 0;
    const depositAmount = Number(trxData.amount) || 0;
    const newBalance = currentBalance + depositAmount;

    // Atomically credit balance and update transaction
    transaction.update(userRef, {
      balance: newBalance,
      lastUpdated: Date.now()
    });

    transaction.update(trxRef, {
      status: "APPROVED",
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      approvedAt: Date.now(),
      approvedBy: context.auth.token.email || context.auth.uid,
      adminNote: adminNote || "Approved by Admin"
    });

    return {
      success: true,
      message: `Deposit of ${depositAmount} RS approved. New balance: ${newBalance} RS.`
    };
  });
});

/**
 * Callable: Admin Reject Deposit
 */
exports.adminRejectDeposit = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const { transactionId, reason } = data;
  if (!transactionId) {
    throw new functions.https.HttpsError("invalid-argument", "Transaction ID is required");
  }

  const trxRef = db.collection("transactions").doc(transactionId);
  const trxSnap = await trxRef.get();
  if (!trxSnap.exists) {
    throw new functions.https.HttpsError("not-found", "Transaction not found");
  }

  await trxRef.update({
    status: "REJECTED",
    adminNote: reason || "Rejected by Administrator",
    rejectedAt: Date.now(),
    rejectedBy: context.auth.token.email || context.auth.uid
  });

  return { success: true, message: "Deposit rejected." };
});

/**
 * Callable: Request Withdrawal
 * Checks user balance, locks the requested funds atomically, and queues withdrawal.
 */
exports.requestWithdrawal = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const uid = context.auth.uid;
  const amount = Number(data.amount);
  const paymentMethod = data.paymentMethod || "EasyPaisa";
  const accountNumber = data.accountNumber || "";
  const accountTitle = data.accountTitle || "";

  if (!amount || amount <= 0) {
    throw new functions.https.HttpsError("invalid-argument", "Invalid withdrawal amount");
  }
  if (!accountNumber || !accountTitle) {
    throw new functions.https.HttpsError("invalid-argument", "Account title and number are required");
  }

  const userRef = db.collection("users").doc(uid);

  return await db.runTransaction(async (transaction) => {
    const userSnap = await transaction.get(userRef);
    if (!userSnap.exists) {
      throw new functions.https.HttpsError("not-found", "User account not found");
    }

    const userData = userSnap.data();
    if (userData.status === "BLOCKED" || userData.status === "SUSPENDED") {
      throw new functions.https.HttpsError("permission-denied", "Account is suspended or blocked");
    }

    const currentBalance = Number(userData.balance) || 0;
    if (currentBalance < amount) {
      throw new functions.https.HttpsError("failed-precondition", "Insufficient balance");
    }

    const newBalance = currentBalance - amount;

    // Deduct balance atomically
    transaction.update(userRef, {
      balance: newBalance,
      lastUpdated: Date.now()
    });

    // Create withdrawal transaction
    const newTrxRef = db.collection("transactions").doc();
    transaction.set(newTrxRef, {
      trxId: "WTH_" + Date.now(),
      userId: uid,
      userName: userData.username || "Player",
      type: "WITHDRAWAL",
      amount: amount,
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: paymentMethod,
      senderNumber: accountNumber,
      transactionRef: accountTitle,
      status: "PENDING",
      createdAt: Date.now()
    });

    return {
      success: true,
      message: `Withdrawal request for ${amount} RS submitted. New balance: ${newBalance} RS.`
    };
  });
});

/**
 * Callable: Admin Update User Account Status (ACTIVE, SUSPENDED, BLOCKED)
 */
exports.adminUpdateUserStatus = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const { targetUid, newStatus, reason } = data;
  if (!targetUid || !["ACTIVE", "SUSPENDED", "BLOCKED"].includes(newStatus)) {
    throw new functions.https.HttpsError("invalid-argument", "Valid target UID and status required");
  }

  await db.collection("users").doc(targetUid).update({
    status: newStatus,
    statusReason: reason || "",
    statusUpdatedAt: Date.now()
  });

  await db.collection("admin_audit_logs").add({
    action: `USER_STATUS_CHANGE_${newStatus}`,
    targetUid: targetUid,
    adminEmail: context.auth.token.email || context.auth.uid,
    details: reason || `Changed account status to ${newStatus}`,
    timestamp: Date.now()
  });

  return { success: true, message: `Account status set to ${newStatus}` };
});

/**
 * Callable: Admin Adjust Balance
 * Allows administrative corrections with strict audit logging.
 */
exports.adminAdjustBalance = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Authentication required");
  }

  const { targetUid, adjustmentAmount, reason } = data;
  const delta = Number(adjustmentAmount);
  if (!targetUid || isNaN(delta)) {
    throw new functions.https.HttpsError("invalid-argument", "Target UID and valid numeric amount required");
  }

  const userRef = db.collection("users").doc(targetUid);

  return await db.runTransaction(async (transaction) => {
    const userSnap = await transaction.get(userRef);
    if (!userSnap.exists) {
      throw new functions.https.HttpsError("not-found", "User not found");
    }

    const userData = userSnap.data();
    const currentBalance = Number(userData.balance) || 0;
    const newBalance = Math.max(0, currentBalance + delta);

    transaction.update(userRef, {
      balance: newBalance,
      lastUpdated: Date.now()
    });

    const trxRef = db.collection("transactions").doc();
    transaction.set(trxRef, {
      trxId: "ADJ_" + Date.now(),
      userId: targetUid,
      userName: userData.username || "Player",
      type: "ADJUSTMENT",
      amount: delta,
      balanceBefore: currentBalance,
      balanceAfter: newBalance,
      paymentMethod: "AdminAdjustment",
      status: "APPROVED",
      adminNote: reason || "Manual adjustment by Admin",
      createdAt: Date.now()
    });

    return {
      success: true,
      message: `Balance adjusted by ${delta > 0 ? "+" : ""}${delta} RS. New balance: ${newBalance} RS.`
    };
  });
});
