# Build the installable Android app (APK)

The APK contains the Flask server + database inside it. It runs fully on the phone (no hosting needed).
Weather, live mandi rates and external links need internet; everything else works offline.

## Easiest: build in the cloud (no Android Studio, free)
1. Create a free GitHub account → New repository → upload ALL files of this folder
   (including the hidden `.github` folder).
2. Open the **Actions** tab → "Build Android APK" → wait ~8–10 minutes
   (if it doesn't start, click **Run workflow**).
3. Open the finished run → **Artifacts → KisanSahayak-apk** → download → unzip → `app-debug.apk`.

## Install on a phone
Send `app-debug.apk` to the phone (WhatsApp, Google Drive, USB, email), tap it,
allow "Install unknown apps" when asked, then tap Install. Open **Kisan Sahayak** from the app drawer.

## Or build on your PC
Install Android Studio, then:
```
./sync_webapp.sh          # copy Flask app into the Android project
open the `android/` folder in Android Studio → Build → Build APK(s)
```

## Notes
- After changing any Flask code/templates/static, run `./sync_webapp.sh` again, then rebuild.
- Data (users, posts, bookmarks) is stored privately on each phone — it is NOT shared between phones.
  A shared community across users requires a hosted server (see PHONE_APP_GUIDE.md).
- Admin login is disabled in the APK (no ADMIN_USERNAME/ADMIN_PASSWORD set). The demo account
  ramesh_kumar / farmer123 works.
- This is a debug-signed APK, fine for sharing directly. For Google Play you need a release-signed AAB.
