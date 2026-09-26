# EmaanMani Standalone Admin Portal

Standalone, production-ready HTML5/CSS3/JavaScript Admin Portal for **EmaanMani**, designed to be hosted on **GitHub Pages** or any static web hosting provider.

---

## 🚀 Features

- **Display & Brand Name:** `EmaanMani` displayed consistently across the login page, dashboard header, profile banner, and reports.
- **Independent Architecture:** 100% standalone static files (`index.html`, `style.css`, `script.js`). No build steps or server dependencies required.
- **Multi-Device Responsive:** Optimized for Google Chrome across Desktop, Laptop, Tablet, and Mobile Android browsers.
- **FiversCan Real-Time Monitor & Balance Synchronizer:**
  - Real-time telemetry monitoring for NexusGGR / FiversCan API aggregator.
  - Interactive balance synchronizer ensuring the **Lobby Balance** and **In-Game Slot Credit** (e.g. Pragmatic Play, PG Soft, Evolution) are locked to the exact same value.

---

## 🔐 Authorized Admin Accounts

The portal verifies credentials against three administrator tiers:

| Tier | Username | Password | Access Role |
| :--- | :--- | :--- | :--- |
| **A. Administrator Portal** | `admin@administrator.com` | `ADMIN@786` | Full Master Administration |
| **B. Enterprise Admin Gateway** | `admin@enterprise.com` | `Admin@786enter` | Enterprise Infrastructure & Telemetry |
| **C. Admin Console** | `admin@console.com` | `Admin@786` | Real-time Operations & Cashier Sync |

*Note: Entering any other credentials will trigger the error: `"Invalid username or password."`.*

---

## 🌐 How to Deploy to GitHub Pages

1. **Create a GitHub Repository:**
   - Go to [GitHub.com](https://github.com) and create a new repository (e.g., `emaanmani-admin`).
2. **Upload Files:**
   - Upload the 4 files in this folder directly to the root of your repository:
     - `index.html`
     - `style.css`
     - `script.js`
     - `README.md`
3. **Enable GitHub Pages:**
   - Go to **Settings** → **Pages** in your repository.
   - Under **Build and deployment** > **Source**, choose **Deploy from a branch**.
   - Select branch `main` (or `master`) and folder `/(root)`.
   - Click **Save**.
4. **Access the Portal:**
   - Within 1–2 minutes, your website will be live at:
     `https://<your-username>.github.io/<repository-name>/`
