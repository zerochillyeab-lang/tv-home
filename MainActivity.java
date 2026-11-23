package com.yeab.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private static final String HOME_URL = "file:///android_asset/index.html";
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.setBackgroundColor(0xFF05060F);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handle(request.getUrl().toString());
            }
        });

        web.loadUrl(HOME_URL);
        web.requestFocus();
    }

    /** Opens real TV apps when a tile points at a known service. */
    private boolean handle(String url) {
        if (url.startsWith("file:")) {
            return false;
        }
        if (url.startsWith("intent:")) {
            try {
                startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME));
            } catch (Exception ignored) {
            }
            return true;
        }
        try {
            String pkg = packageFor(Uri.parse(url).getHost());
            if (pkg != null) {
                PackageManager pm = getPackageManager();
                Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
                if (i == null) {
                    i = pm.getLaunchIntentForPackage(pkg);
                }
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false; // app not installed: show the website instead
    }

    private String packageFor(String host) {
        if (host == null) {
            return null;
        }
        if (host.contains("youtube.com")) return "com.google.android.youtube.tv";
        if (host.contains("netflix.com")) return "com.netflix.ninja";
        if (host.contains("primevideo.com")) return "com.amazon.amazonvideo.livingroom";
        if (host.contains("disneyplus.com")) return "com.disney.disneyplus";
        if (host.contains("spotify.com")) return "com.spotify.tv.android";
        return null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (e.getAction() == KeyEvent.ACTION_UP) {
                String u = web.getUrl();
                if (u != null && !u.startsWith("file:") && web.canGoBack()) {
                    web.goBack();
                } else {
                    web.evaluateJavascript("window.tvBack&&window.tvBack()", null);
                }
            }
            return true; // a home screen never closes on Back
        }
        return super.dispatchKeyEvent(e);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String u = web.getUrl();
        if (u != null && !u.startsWith("file:")) {
            web.loadUrl(HOME_URL);
        } else {
            web.evaluateJavascript("window.tvHome&&window.tvHome()", null);
        }
    }
}
