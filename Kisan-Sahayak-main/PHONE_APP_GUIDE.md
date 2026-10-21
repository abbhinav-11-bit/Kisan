# Kisan Sahayak as a phone app

The project is now an installable **PWA** (Progressive Web App). Your Flask backend stays as it is;
the phone installs it like a normal app (home-screen icon, full screen, offline fallback page).

## Step 1 — Put the server online (HTTPS is required)
Phones only allow installing/service workers over HTTPS (localhost excepted).

**Render (easiest):**
1. Push this folder to a GitHub repo.
2. Render → New → Blueprint → pick the repo (it reads `render.yaml`).
3. Set `ADMIN_USERNAME`, `ADMIN_PASSWORD`, optionally `DATA_GOV_API_KEY`.
   `SECRET_KEY` is generated for you.

**PythonAnywhere / Railway / any VPS** also work: run `gunicorn app:app`, set the same env vars.

> SQLite note: the DB is a file. Use a host with a persistent disk (set `KISAN_DB_PATH=/data/database.db`),
> otherwise users/posts reset on every redeploy. For many users later, move to PostgreSQL.

## Step 2 — Install on the phone
- **Android (Chrome):** open your site → tap **Install** banner (or ⋮ → *Install app*).
- **iPhone (Safari):** Share → **Add to Home Screen**.

## Step 3 (optional) — Real APK / Play Store
1. Go to https://www.pwabuilder.com, enter your HTTPS URL, choose **Android** → download the package.
2. For Play Store, put the generated `assetlinks.json` content in the `ASSETLINKS_JSON` env var
   (served at `/.well-known/assetlinks.json`).
3. Play Store needs a one-time $25 developer account.

## Test on your phone before deploying
```bash
HOST=0.0.0.0 python app.py     # then open http://<your-PC-IP>:5000 on the same Wi-Fi
```
(Install prompt won't appear without HTTPS, but layout can be checked. Chrome → `chrome://flags` →
"Insecure origins treated as secure" can be used for testing.)

## What was added
- `static/manifest.webmanifest`, `static/icons/*` — app name, colors, icons, shortcuts
- `static/sw.js` (served at `/sw.js`) — caching + offline fallback
- `templates/offline.html` — offline page
- `templates/base.html` — manifest/meta tags, SW registration, install banner
- `app.py` — PWA routes, 30-day login sessions, `HOST`/`PORT` env vars
- `Procfile`, `render.yaml` — deployment
