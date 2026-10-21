#!/usr/bin/env bash
# Copies the Flask app into the Android project's assets (run before every APK build)
set -e
cd "$(dirname "$0")"
D=android/app/src/main/assets/webapp
rm -rf "$D" && mkdir -p "$D"
cp app.py database.py init_db.py "$D/"
cp -r templates static "$D/"
find "$D" -name "__pycache__" -prune -exec rm -rf {} +
echo "Synced web app -> $D"
