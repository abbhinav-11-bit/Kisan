"""Starts the Shram Setu Flask app inside the Android app (127.0.0.1 only)."""
import os, sys, secrets, types


def run(webapp_dir, data_dir, port):
    os.makedirs(data_dir, exist_ok=True)
    sys.path.insert(0, webapp_dir)
    os.chdir(webapp_dir)

    # python-dotenv isn't needed on the phone; stub it so app.py imports cleanly
    stub = types.ModuleType("dotenv")
    stub.load_dotenv = lambda *a, **k: False
    sys.modules["dotenv"] = stub

    # Database + a persistent secret key live in the app's private storage
    os.environ["KISAN_DB_PATH"] = os.path.join(data_dir, "database.db")
    key_file = os.path.join(data_dir, "secret.key")
    if not os.path.exists(key_file):
        with open(key_file, "w") as f:
            f.write(secrets.token_hex(32))
    with open(key_file) as f:
        os.environ["SECRET_KEY"] = f.read().strip()

    import app as kisan
    from werkzeug.serving import make_server
    server = make_server("127.0.0.1", port, kisan.app, threaded=True)
    server.serve_forever()
