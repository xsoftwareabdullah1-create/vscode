package com.microsoft.vscode;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
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
    private static final String EDITOR_URL = "https://vscode.dev/";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(30,30,30));
        getWindow().setNavigationBarColor(Color.rgb(30,30,30));
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(30,30,30));
        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(10,4,10,4); bar.setBackgroundColor(Color.rgb(37,37,38));
        TextView brand = new TextView(this); brand.setText("◈  Visual Studio Code"); brand.setTextColor(Color.WHITE); brand.setTextSize(16); brand.setPadding(8,0,16,0); bar.addView(brand,new LinearLayout.LayoutParams(0,48,1));
        Button run = new Button(this); run.setText("▶ Run"); run.setAllCaps(false); bar.addView(run); run.setOnClickListener(v -> {
            if (web == null) return;
            web.evaluateJavascript("(function(){try{if(window.require){var c=window.require('vs/base/common/commands');}document.dispatchEvent(new KeyboardEvent('keydown',{key:'d',ctrlKey:true,shiftKey:true,bubbles:true}));return 'Run and Debug shortcut sent';}catch(e){return 'Use the editor Run menu';}})()", result -> Toast.makeText(this,"Run: open Run and Debug in the editor if needed",Toast.LENGTH_SHORT).show());
        });
        Button reload = new Button(this); reload.setText("↻"); reload.setContentDescription("Reload editor"); bar.addView(reload); reload.setOnClickListener(v -> web.reload());
        root.addView(bar,new LinearLayout.LayoutParams(-1,-2));
        web = new WebView(this); web.setBackgroundColor(Color.rgb(30,30,30)); WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setLoadsImagesAutomatically(true); s.setSupportZoom(true); s.setBuiltInZoomControls(true); s.setDisplayZoomControls(false); s.setMediaPlaybackRequiresUserGesture(false); s.setUserAgentString(s.getUserAgentString()+" VSCodeMobile/1.0"); web.setWebViewClient(new WebViewClient()); web.setWebChromeClient(new WebChromeClient());
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root); if(state==null) web.loadUrl(EDITOR_URL); else web.restoreState(state);
    }
    @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);if(web!=null)web.saveState(out);}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
