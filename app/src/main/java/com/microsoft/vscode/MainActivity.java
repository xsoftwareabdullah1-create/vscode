package com.microsoft.vscode;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER = 4102;
    private static final String EDITOR_URL = "https://vscode.dev/";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(30,30,30));
        getWindow().setNavigationBarColor(Color.rgb(30,30,30));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(30,30,30));

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10), dp(3), dp(10), dp(3));
        bar.setBackgroundColor(Color.rgb(37,37,38));
        TextView brand = new TextView(this);
        brand.setText("◈  Visual Studio Code");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(16);
        brand.setSingleLine(true);
        brand.setPadding(dp(8),0,dp(8),0);
        bar.addView(brand,new LinearLayout.LayoutParams(0,dp(42),1));
        Button reload = new Button(this);
        reload.setText("↻");
        reload.setTextSize(18);
        reload.setAllCaps(false);
        reload.setContentDescription("Reload editor");
        bar.addView(reload,new LinearLayout.LayoutParams(dp(52),dp(42)));
        root.addView(bar,new LinearLayout.LayoutParams(-1,-2));

        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(30,30,30));
        web.setFocusable(true);
        web.setFocusableInTouchMode(true);
        web.setVerticalScrollBarEnabled(true);
        web.setHorizontalScrollBarEnabled(true);
        web.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setUserAgentString(s.getUserAgentString()+" VSCodeMobile/1.1");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    startActivityForResult(intent, FILE_PICKER);
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this,"File picker could not open",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
        web.setDownloadListener(new DownloadListener() {
            @Override public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
                    if (cookies != null) request.addRequestHeader("Cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setMimeType(mimeType);
                    request.setTitle("VS Code download");
                    request.setDescription("Downloading file");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "vscode-download-" + System.currentTimeMillis());
                    ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
                    Toast.makeText(MainActivity.this,"Download started. Check Downloads.",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,"Download failed: "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        bottom.setPadding(dp(10),dp(3),dp(10),dp(3));
        bottom.setBackgroundColor(Color.rgb(37,37,38));
        Button run = new Button(this);
        run.setText("▶ Run");
        run.setAllCaps(false);
        run.setTextSize(14);
        bottom.addView(run,new LinearLayout.LayoutParams(dp(100),dp(42)));
        root.addView(bottom,new LinearLayout.LayoutParams(-1,-2));

        reload.setOnClickListener(v -> web.reload());
        run.setOnClickListener(v -> {
            if (web != null) {
                web.evaluateJavascript("(function(){document.dispatchEvent(new KeyboardEvent('keydown',{key:'d',ctrlKey:true,shiftKey:true,bubbles:true}));return true;})()", null);
                Toast.makeText(this,"Editor shortcut sent. Actual code execution needs a supported runtime/terminal.",Toast.LENGTH_LONG).show();
            }
        });

        setContentView(root);
        if (state == null) web.loadUrl(EDITOR_URL); else web.restoreState(state);
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == FILE_PICKER && fileCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode,data);
            fileCallback.onReceiveValue(results);
            fileCallback = null;
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (web != null) web.saveState(out);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
