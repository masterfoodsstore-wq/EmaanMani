/**
 * Royal X Live Operations & Admin Console (Firebase Web Client)
 * Connects directly to Firebase Authentication, Cloud Firestore, and Cloud Functions.
 */

// Replace this config with your actual Firebase Project config from the Firebase Console
const firebaseConfig = window.FIREBASE_CONFIG || {
  apiKey: "AIzaSy_ROYAL_X_WEB_ADMIN_KEY",
  authDomain: "royal-x-casino.firebaseapp.com",
  projectId: "royal-x-casino",
  storageBucket: "royal-x-casino.appspot.com",
  messagingSenderId: "123456789012",
  appId: "1:123456789012:web:abcdef123456"
};

// Initialize Firebase
if (!firebase.apps.length) {
  try {
    firebase.initializeApp(firebaseConfig);
  } catch (err) {
    console.warn("Firebase initialization notice:", err.message);
  }
}

const auth = firebase.auth();
const db = firebase.firestore();
const functions = firebase.functions();

// Global State
let currentUser = null;
let liveGameConfig = null;
let allUsers = [];
let allTransactions = [];
let allAnnouncements = [];

// DOM Elements
const authModal = document.getElementById("auth-modal");
const dashboardContainer = document.getElementById("dashboard-container");
const loginForm = document.getElementById("login-form");
const authAlert = document.getElementById("auth-alert");
const connectionStatus = document.getElementById("connection-status");
const activeTabTitle = document.getElementById("active-tab-title");

// Navigation tabs
const navItems = document.querySelectorAll(".nav-item");
const tabPanes = document.querySelectorAll(".tab-pane");

function switchTab(tabName) {
  navItems.forEach(item => {
    item.classList.toggle("active", item.dataset.tab === tabName);
  });
  tabPanes.forEach(pane => {
    pane.classList.toggle("active", pane.id === `tab-${tabName}`);
  });
  const titles = {
    overview: "Platform Overview",
    users: "Player Registry & Account Control",
    games: "Game Servers & Remote Config",
    transactions: "Financial Ledger & Cashier Approvals",
    announcements: "Live Broadcast Announcements",
    settings: "System & Firebase Configuration"
  };
  activeTabTitle.textContent = titles[tabName] || "Platform Overview";
}

navItems.forEach(item => {
  item.addEventListener("click", () => switchTab(item.dataset.tab));
});

// Authentication Flow
auth.onAuthStateChanged(async (user) => {
  if (user) {
    currentUser = user;
    document.getElementById("current-admin-email").textContent = user.email || "Admin";
    authModal.classList.add("hidden");
    dashboardContainer.classList.remove("hidden");
    connectionStatus.textContent = "Live Connected (Firestore Real-time)";
    initRealtimeListeners();
  } else {
    currentUser = null;
    authModal.classList.remove("hidden");
    dashboardContainer.classList.add("hidden");
    connectionStatus.textContent = "Awaiting Authentication";
  }
});

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const email = document.getElementById("login-email").value.trim();
  const password = document.getElementById("login-password").value;
  const btn = document.getElementById("btn-login");

  btn.disabled = true;
  authAlert.classList.add("hidden");

  try {
    await auth.signInWithEmailAndPassword(email, password);
  } catch (err) {
    authAlert.textContent = err.message || "Failed to authenticate";
    authAlert.className = "alert alert-error";
    authAlert.classList.remove("hidden");
  } finally {
    btn.disabled = false;
  }
});

document.getElementById("btn-logout").addEventListener("click", () => {
  auth.signOut();
});

// Real-Time Firestore Listeners
function initRealtimeListeners() {
  // 1. Listen to Users Collection
  db.collection("users").onSnapshot((snapshot) => {
    allUsers = [];
    let totalBalance = 0;

    snapshot.forEach(doc => {
      const u = { uid: doc.id, ...doc.data() };
      allUsers.push(u);
      totalBalance += Number(u.balance) || 0;
    });

    document.getElementById("stat-total-users").textContent = allUsers.length;
    document.getElementById("stat-total-balance").textContent = `₨ ${totalBalance.toLocaleString()}`;

    renderUsersTable();
  }, (err) => {
    console.error("Error listening to users:", err);
  });

  // 2. Listen to Transactions Collection
  db.collection("transactions").orderBy("createdAt", "desc").limit(50).onSnapshot((snapshot) => {
    allTransactions = [];
    let pendingCount = 0;
    let pendingDep = 0;
    let pendingWith = 0;

    snapshot.forEach(doc => {
      const t = { id: doc.id, ...doc.data() };
      allTransactions.push(t);
      if (t.status === "PENDING") {
        pendingCount++;
        if (t.type === "DEPOSIT") pendingDep++;
        if (t.type === "WITHDRAWAL") pendingWith++;
      }
    });

    document.getElementById("stat-pending-trx").textContent = pendingCount;
    document.getElementById("pending-badge").textContent = pendingCount;
    document.getElementById("stat-pending-breakdown").textContent = `${pendingDep} Deposits • ${pendingWith} Withdrawals`;

    renderOverviewTransactions();
    renderTransactionsTable();
  }, (err) => {
    console.error("Error listening to transactions:", err);
  });

  // 3. Listen to Live Game Config
  db.collection("app_config").doc("live_game_config").onSnapshot((doc) => {
    if (doc.exists) {
      liveGameConfig = doc.data();
      populateGameConfigForm(liveGameConfig);
    } else {
      // Seed default config in Firestore if missing
      seedDefaultGameConfig();
    }
  }, (err) => {
    console.error("Error listening to game config:", err);
  });

  // 4. Listen to Announcements
  db.collection("announcements").orderBy("createdAt", "desc").limit(20).onSnapshot((snapshot) => {
    allAnnouncements = [];
    snapshot.forEach(doc => {
      allAnnouncements.push({ id: doc.id, ...doc.data() });
    });
    renderAnnouncementsTable();
  });
}

// Render Users Table
function renderUsersTable() {
  const tbody = document.getElementById("users-tbody");
  const search = document.getElementById("user-search").value.toLowerCase();
  const filterStatus = document.getElementById("user-status-filter").value;

  const filtered = allUsers.filter(u => {
    const matchesSearch = !search ||
      (u.username && u.username.toLowerCase().includes(search)) ||
      (u.email && u.email.toLowerCase().includes(search)) ||
      (u.id && u.id.toString().includes(search)) ||
      (u.uid && u.uid.toLowerCase().includes(search));

    const matchesStatus = filterStatus === "ALL" || u.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No players found matching query.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(u => {
    const statusClass = u.status === "ACTIVE" ? "badge-active" : (u.status === "SUSPENDED" ? "badge-suspended" : "badge-blocked");
    const dateStr = u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "—";
    const balance = Number(u.balance) || 0;

    return `
      <tr>
        <td>
          <strong>${u.avatar || "🦁"} ${escapeHtml(u.username || "Player")}</strong>
          <br><small style="color:var(--text-muted)">ID: ${u.id || u.uid.substring(0,6)}</small>
        </td>
        <td>${escapeHtml(u.email || "—")}</td>
        <td><strong style="color:var(--gold-light)">₨ ${balance.toLocaleString()}</strong></td>
        <td><span class="badge ${statusClass}">${u.status || "ACTIVE"}</span></td>
        <td>${dateStr}</td>
        <td>
          <button class="btn-secondary" style="padding:4px 8px;font-size:0.75rem" onclick="openAdjustBalance('${u.uid}', '${escapeHtml(u.username || "Player")}', ${balance})">💰 Balance</button>
          ${u.status === "ACTIVE" 
            ? `<button class="btn-danger-outline" style="padding:4px 8px;font-size:0.75rem" onclick="setUserStatus('${u.uid}', 'SUSPENDED')">Suspend</button>`
            : `<button class="btn-secondary" style="padding:4px 8px;font-size:0.75rem;color:var(--emerald-green)" onclick="setUserStatus('${u.uid}', 'ACTIVE')">Activate</button>`
          }
        </td>
      </tr>
    `;
  }).join("");
}

document.getElementById("user-search").addEventListener("input", renderUsersTable);
document.getElementById("user-status-filter").addEventListener("change", renderUsersTable);

// User Status Control
async function setUserStatus(uid, status) {
  if (!confirm(`Are you sure you want to change this player's status to ${status}?`)) return;
  try {
    await db.collection("users").doc(uid).update({
      status: status,
      statusUpdatedAt: Date.now()
    });
  } catch (err) {
    alert("Error updating user status: " + err.message);
  }
}

// Balance Adjustment Modal
function openAdjustBalance(uid, name, balance) {
  document.getElementById("adjust-uid").value = uid;
  document.getElementById("adjust-user-name").textContent = `${name} (Current: ₨ ${balance.toLocaleString()})`;
  document.getElementById("adjust-amount").value = "";
  document.getElementById("adjust-reason").value = "";
  document.getElementById("modal-adjust-balance").classList.remove("hidden");
}

document.getElementById("form-adjust-balance").addEventListener("submit", async (e) => {
  e.preventDefault();
  const uid = document.getElementById("adjust-uid").value;
  const delta = Number(document.getElementById("adjust-amount").value);
  const reason = document.getElementById("adjust-reason").value;

  if (isNaN(delta) || delta === 0) {
    alert("Please enter a valid non-zero amount");
    return;
  }

  try {
    const userRef = db.collection("users").doc(uid);
    await db.runTransaction(async (t) => {
      const snap = await t.get(userRef);
      if (!snap.exists) throw new Error("User does not exist");
      const current = Number(snap.data().balance) || 0;
      const newBal = Math.max(0, current + delta);
      t.update(userRef, { balance: newBal });
      
      const trxRef = db.collection("transactions").doc();
      t.set(trxRef, {
        trxId: "ADJ_" + Date.now(),
        userId: uid,
        userName: snap.data().username || "Player",
        type: "ADJUSTMENT",
        amount: delta,
        balanceBefore: current,
        balanceAfter: newBal,
        paymentMethod: "AdminAdjustment",
        status: "APPROVED",
        adminNote: reason,
        createdAt: Date.now()
      });
    });

    closeModal("modal-adjust-balance");
    alert("Balance successfully updated!");
  } catch (err) {
    alert("Error adjusting balance: " + err.message);
  }
});

// Render Transactions
function renderOverviewTransactions() {
  const tbody = document.getElementById("overview-trx-tbody");
  const recent = allTransactions.slice(0, 5);

  if (recent.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No transactions recorded yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = recent.map(t => {
    const statusClass = t.status === "APPROVED" ? "badge-approved" : (t.status === "PENDING" ? "badge-pending" : "badge-rejected");
    return `
      <tr>
        <td><code>${(t.trxId || t.id).substring(0, 10)}...</code></td>
        <td>${escapeHtml(t.userName || "Player")}</td>
        <td>${t.type}</td>
        <td><strong>₨ ${Number(t.amount || 0).toLocaleString()}</strong></td>
        <td><span class="badge ${statusClass}">${t.status}</span></td>
      </tr>
    `;
  }).join("");
}

function renderTransactionsTable() {
  const tbody = document.getElementById("transactions-tbody");
  const typeFilter = document.getElementById("trx-filter-type").value;
  const statusFilter = document.getElementById("trx-filter-status").value;

  const filtered = allTransactions.filter(t => {
    const matchesType = typeFilter === "ALL" || t.type === typeFilter;
    const matchesStatus = statusFilter === "ALL" || t.status === statusFilter;
    return matchesType && matchesStatus;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-state">No transactions match filters.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(t => {
    const statusClass = t.status === "APPROVED" ? "badge-approved" : (t.status === "PENDING" ? "badge-pending" : "badge-rejected");
    const dateStr = t.createdAt ? new Date(t.createdAt).toLocaleString() : "—";
    const isPending = t.status === "PENDING";

    return `
      <tr>
        <td><code>${t.trxId || t.id}</code></td>
        <td>${escapeHtml(t.userName || "Player")}</td>
        <td><strong>${t.paymentMethod || t.type}</strong></td>
        <td><strong style="color:var(--gold-light)">₨ ${Number(t.amount || 0).toLocaleString()}</strong></td>
        <td>${escapeHtml(t.senderNumber || t.transactionRef || "—")}</td>
        <td><span class="badge ${statusClass}">${t.status}</span></td>
        <td><small>${dateStr}</small></td>
        <td>
          ${isPending ? `
            <button class="btn-secondary" style="padding:4px 8px;font-size:0.75rem;color:var(--emerald-green)" onclick="approveTransaction('${t.id}')">✓ Approve</button>
            <button class="btn-danger-outline" style="padding:4px 8px;font-size:0.75rem" onclick="rejectTransaction('${t.id}')">✕ Reject</button>
          ` : `<small style="color:var(--text-muted)">Settled</small>`}
        </td>
      </tr>
    `;
  }).join("");
}

document.getElementById("trx-filter-type").addEventListener("change", renderTransactionsTable);
document.getElementById("trx-filter-status").addEventListener("change", renderTransactionsTable);

// Approve / Reject Cashier Transactions
async function approveTransaction(trxDocId) {
  if (!confirm("Confirm approval of this transaction? Funds will be credited immediately.")) return;
  try {
    const trxRef = db.collection("transactions").doc(trxDocId);
    await db.runTransaction(async (t) => {
      const snap = await t.get(trxRef);
      if (!snap.exists) throw new Error("Transaction does not exist");
      const trx = snap.data();
      if (trx.status !== "PENDING") throw new Error("Transaction is already " + trx.status);

      const userRef = db.collection("users").doc(trx.userId);
      const userSnap = await t.get(userRef);
      if (!userSnap.exists) throw new Error("User does not exist");

      const currentBalance = Number(userSnap.data().balance) || 0;
      const amount = Number(trx.amount) || 0;
      const newBalance = currentBalance + amount;

      t.update(userRef, { balance: newBalance });
      t.update(trxRef, {
        status: "APPROVED",
        balanceBefore: currentBalance,
        balanceAfter: newBalance,
        approvedAt: Date.now(),
        approvedBy: currentUser ? currentUser.email : "Admin"
      });
    });
    alert("Transaction approved successfully!");
  } catch (err) {
    alert("Approval error: " + err.message);
  }
}

async function rejectTransaction(trxDocId) {
  const reason = prompt("Enter rejection reason:");
  if (reason === null) return;
  try {
    await db.collection("transactions").doc(trxDocId).update({
      status: "REJECTED",
      adminNote: reason || "Rejected by Administrator",
      rejectedAt: Date.now(),
      rejectedBy: currentUser ? currentUser.email : "Admin"
    });
    alert("Transaction marked as rejected.");
  } catch (err) {
    alert("Rejection error: " + err.message);
  }
}

// Live Game Remote Switchboard
function populateGameConfigForm(config) {
  document.getElementById("quick-toggle-dragon").checked = config.isDragonTigerOnline !== false;
  document.getElementById("quick-toggle-zoo").checked = config.isZooRouletteOnline !== false;
  document.getElementById("quick-toggle-slots").checked = config.isCyberSlotsOnline === true;

  document.getElementById("game-dragon-seconds").value = config.dragonCountdownSeconds || 15;
  document.getElementById("game-zoo-seconds").value = config.zooCountdownSeconds || 18;
  document.getElementById("game-min-bet").value = config.minBetLimit || 10;
  document.getElementById("game-max-bet").value = config.maxBetLimit || 50000;
  document.getElementById("game-commission").value = config.houseCommissionPercent || 3.0;
  document.getElementById("game-player-count").value = config.onlinePlayerCount || 2418;

  document.getElementById("m-title").value = config.maintenanceTitle || "SYSTEM SERVER UPDATE IN PROGRESS";
  document.getElementById("m-message").value = config.maintenanceMessage || "";

  const onlineCount = (config.isDragonTigerOnline ? 1 : 0) + (config.isZooRouletteOnline ? 1 : 0) + (config.isCyberSlotsOnline ? 1 : 0);
  document.getElementById("stat-tables-online").textContent = `${onlineCount} / 3`;
  document.getElementById("stat-maintenance-status").textContent = config.isGlobalMaintenance ? "🚨 Global Maintenance Active" : "Normal Live Tables";
  document.getElementById("btn-quick-maintenance").textContent = config.isGlobalMaintenance ? "Disable Maintenance" : "Enable Maintenance";
}

async function seedDefaultGameConfig() {
  const defaultConfig = {
    isGlobalMaintenance: false,
    maintenanceTitle: "SYSTEM SERVER UPDATE IN PROGRESS",
    maintenanceMessage: "The game server is currently applying an Over-The-Air hot-patch. Live tables will resume in a few moments without requiring APK re-download.",
    maintenanceEtaMinutes: 5,
    isDragonTigerOnline: true,
    isZooRouletteOnline: true,
    isCyberSlotsOnline: false,
    dragonCountdownSeconds: 15,
    zooCountdownSeconds: 18,
    minBetLimit: 10,
    maxBetLimit: 50000,
    houseCommissionPercent: 3.0,
    onlinePlayerCount: 2418,
    isBroadcastActive: true,
    liveBroadcastMessage: "🔥 Welcome to Royal X! EasyPaisa & JazzCash Instant Deposits Active • 24/7 Instant Cashier Payouts!",
    liveAppVersion: "v2.5.0-OTA",
    lastUpdatedTimestamp: Date.now(),
    lastAdminEditor: currentUser ? currentUser.email : "admin@royalx.com"
  };
  await db.collection("app_config").doc("live_game_config").set(defaultConfig);
}

// Quick Toggles
document.getElementById("quick-toggle-dragon").addEventListener("change", async (e) => {
  await db.collection("app_config").doc("live_game_config").update({
    isDragonTigerOnline: e.target.checked,
    lastUpdatedTimestamp: Date.now()
  });
});

document.getElementById("quick-toggle-zoo").addEventListener("change", async (e) => {
  await db.collection("app_config").doc("live_game_config").update({
    isZooRouletteOnline: e.target.checked,
    lastUpdatedTimestamp: Date.now()
  });
});

document.getElementById("quick-toggle-slots").addEventListener("change", async (e) => {
  await db.collection("app_config").doc("live_game_config").update({
    isCyberSlotsOnline: e.target.checked,
    lastUpdatedTimestamp: Date.now()
  });
});

document.getElementById("btn-quick-maintenance").addEventListener("click", async () => {
  const current = liveGameConfig ? liveGameConfig.isGlobalMaintenance : false;
  const updated = !current;
  await db.collection("app_config").doc("live_game_config").update({
    isGlobalMaintenance: updated,
    lastUpdatedTimestamp: Date.now()
  });
});

document.getElementById("btn-save-games").addEventListener("click", async () => {
  const updates = {
    dragonCountdownSeconds: Number(document.getElementById("game-dragon-seconds").value),
    zooCountdownSeconds: Number(document.getElementById("game-zoo-seconds").value),
    minBetLimit: Number(document.getElementById("game-min-bet").value),
    maxBetLimit: Number(document.getElementById("game-max-bet").value),
    houseCommissionPercent: Number(document.getElementById("game-commission").value),
    onlinePlayerCount: Number(document.getElementById("game-player-count").value),
    maintenanceTitle: document.getElementById("m-title").value,
    maintenanceMessage: document.getElementById("m-message").value,
    lastUpdatedTimestamp: Date.now(),
    lastAdminEditor: currentUser ? currentUser.email : "admin@royalx.com"
  };

  try {
    await db.collection("app_config").doc("live_game_config").update(updates);
    alert("Live Game Configuration successfully pushed OTA to all connected APKs!");
  } catch (err) {
    alert("Error updating config: " + err.message);
  }
});

// Announcements Manager
function renderAnnouncementsTable() {
  const tbody = document.getElementById("announcements-tbody");
  if (allAnnouncements.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No active announcements.</td></tr>`;
    return;
  }

  tbody.innerHTML = allAnnouncements.map(a => {
    return `
      <tr>
        <td><strong>${escapeHtml(a.title || "")}</strong></td>
        <td>${escapeHtml(a.message || "")}</td>
        <td><span class="badge badge-active">${a.type || "INFO"}</span></td>
        <td>${a.isActive !== false ? "🟢 Active" : "⚪ Disabled"}</td>
        <td>
          <button class="btn-danger-outline" style="padding:4px 8px;font-size:0.75rem" onclick="deleteAnnouncement('${a.id}')">Delete</button>
        </td>
      </tr>
    `;
  }).join("");
}

document.getElementById("btn-new-announcement").addEventListener("click", () => {
  document.getElementById("modal-announcement").classList.remove("hidden");
});

document.getElementById("form-add-announcement").addEventListener("submit", async (e) => {
  e.preventDefault();
  const title = document.getElementById("ann-title").value;
  const message = document.getElementById("ann-msg").value;
  const type = document.getElementById("ann-type").value;

  try {
    await db.collection("announcements").add({
      title: title,
      message: message,
      type: type,
      isActive: true,
      createdAt: Date.now(),
      author: currentUser ? currentUser.email : "Admin"
    });

    // Also update current live broadcast message in app_config
    await db.collection("app_config").doc("live_game_config").update({
      liveBroadcastMessage: `${title}: ${message}`,
      isBroadcastActive: true,
      lastUpdatedTimestamp: Date.now()
    });

    closeModal("modal-announcement");
    alert("Announcement published live across all mobile clients!");
  } catch (err) {
    alert("Error posting announcement: " + err.message);
  }
});

async function deleteAnnouncement(id) {
  if (!confirm("Delete this broadcast announcement?")) return;
  try {
    await db.collection("announcements").doc(id).delete();
  } catch (err) {
    alert("Error deleting announcement: " + err.message);
  }
}

// Modal Helpers
function closeModal(id) {
  document.getElementById(id).classList.add("hidden");
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
