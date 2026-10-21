package in.kisansahayak.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends Activity {
    private static final int PORT = 18765;
    private static final String HOME = "http://127.0.0.1:" + PORT + "/";
    private static boolean serverStarted = false;
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(false);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("127.0.0.1".equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true; // external sites, tel:, mailto: open outside the app
            }
        });

        web.loadDataWithBaseURL(null,
            "<html><body style='margin:0;background:#15803d;color:#fff;font-family:sans-serif;"
            + "display:flex;align-items:center;justify-content:center;height:100vh;text-align:center'>"
            + "<div><h2>Kisan Sahayak</h2><p>किसान सहायक</p><p style='opacity:.8'>Starting…</p></div></body></html>",
            "text/html", "utf-8", null);

        new Thread(this::boot).start();
    }

    private void boot() {
        try {
            File webapp = new File(getFilesDir(), "webapp");
            File data = new File(getFilesDir(), "data");
            syncWebApp(webapp);

            if (!serverStarted) {
                serverStarted = true;
                if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
                final String w = webapp.getAbsolutePath(), d = data.getAbsolutePath();
                Thread server = new Thread(() ->
                    Python.getInstance().getModule("launcher").callAttr("run", w, d, PORT), "flask");
                server.setDaemon(true);
                server.start();
            }

            long deadline = System.currentTimeMillis() + 60000;
            while (System.currentTimeMillis() < deadline && !portOpen()) Thread.sleep(250);
            runOnUiThread(() -> web.loadUrl(HOME));
        } catch (Exception e) {
            final String msg = String.valueOf(e);
            runOnUiThread(() -> web.loadDataWithBaseURL(null,
                "<h3>Could not start</h3><pre>" + msg.replace("<", "&lt;") + "</pre>", "text/html", "utf-8", null));
        }
    }

    private boolean portOpen() {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress("127.0.0.1", PORT), 200);
            return true;
        } catch (Exception e) { return false; }
    }

    /** Copy the bundled Flask app (templates/static/code) to private storage when the app is installed or updated. */
    private void syncWebApp(File dest) throws Exception {
        PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        SharedPreferences sp = getSharedPreferences("ks", MODE_PRIVATE);
        long stamp = pi.lastUpdateTime;
        if (dest.exists() && sp.getLong("webapp_stamp", -1) == stamp) return;
        deleteRecursive(dest);
        copyAsset("webapp", dest);
        sp.edit().putLong("webapp_stamp", stamp).apply();
    }

    private void copyAsset(String assetPath, File target) throws Exception {
        String[] children = getAssets().list(assetPath);
        if (children != null && children.length > 0) {
            target.mkdirs();
            for (String c : children) copyAsset(assetPath + "/" + c, new File(target, c));
        } else {
            target.getParentFile().mkdirs();
            try (InputStream in = getAssets().open(assetPath); OutputStream out = new FileOutputStream(target)) {
                byte[] buf = new byte[8192]; int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }
        }
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) { File[] k = f.listFiles(); if (k != null) for (File c : k) deleteRecursive(c); }
        f.delete();
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
