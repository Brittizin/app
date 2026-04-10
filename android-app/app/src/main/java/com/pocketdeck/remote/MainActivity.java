package com.pocketdeck.remote;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "pocket_deck_prefs";
    private static final String KEY_REMOTE_URL = "remote_url";
    private static final String DEFAULT_REMOTE_URL = "http://192.168.3.9:3939/?mode=remote";

    private WebView webView;
    private View errorCard;
    private TextView errorText;
    private Button retryButton;
    private Button configButton;
    private Button topConfigButton;
    private Button prevPageButton;
    private Button nextPageButton;
    private TextView titleText;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = findViewById(R.id.webView);
        errorCard = findViewById(R.id.errorCard);
        errorText = findViewById(R.id.errorText);
        retryButton = findViewById(R.id.retryButton);
        configButton = findViewById(R.id.configButton);
        topConfigButton = findViewById(R.id.topConfigButton);
        prevPageButton = findViewById(R.id.prevPageButton);
        nextPageButton = findViewById(R.id.nextPageButton);
        titleText = findViewById(R.id.titleText);

        titleText.setText(getString(R.string.app_name));

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new RemoteClient());

        retryButton.setOnClickListener(v -> loadRemote());
        configButton.setOnClickListener(v -> openConfigDialog());
        topConfigButton.setOnClickListener(v -> openConfigDialog());
        prevPageButton.setOnClickListener(v -> triggerPageChange("remotePrevPageBtn"));
        nextPageButton.setOnClickListener(v -> triggerPageChange("remoteNextPageBtn"));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

        loadRemote();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    private void applyImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    private void triggerPageChange(String buttonId) {
        webView.evaluateJavascript(
            "(() => { const btn = document.querySelector('#" + buttonId + "'); if (btn) { btn.click(); return true; } return false; })();",
            null
        );
    }

    private void loadRemote() {
        showError(null);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(getRemoteUrl());
    }

    private String getRemoteUrl() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return prefs.getString(KEY_REMOTE_URL, DEFAULT_REMOTE_URL);
    }

    private void saveRemoteUrl(String url) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(KEY_REMOTE_URL, url).apply();
    }

    private void showError(String message) {
        if (message == null) {
            errorCard.setVisibility(View.GONE);
            return;
        }

        errorCard.setVisibility(View.VISIBLE);
        errorText.setText(message);
        webView.setVisibility(View.INVISIBLE);
    }

    private void openConfigDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(getRemoteUrl());
        input.setHint("http://192.168.3.9:3939/?mode=remote");

        new AlertDialog.Builder(this)
            .setTitle("Endereco do DeckWave")
            .setMessage("Use o IP do computador na mesma rede Wi-Fi.")
            .setView(input)
            .setPositiveButton("Salvar", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.isEmpty()) {
                    if (!value.contains("?")) {
                        value = value + "?mode=remote";
                    }
                    saveRemoteUrl(value);
                    loadRemote();
                }
            })
            .setNeutralButton("Abrir no navegador", (dialog, which) -> {
                String value = input.getText().toString().trim();
                if (!value.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(value)));
                }
            })
            .setNegativeButton("Cancelar", (DialogInterface.OnClickListener) null)
            .show();
    }

    private final class RemoteClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            showError(null);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (request.isForMainFrame()) {
                showError("Nao foi possivel conectar ao DeckWave.\nConfira o IP do PC e a rede Wi-Fi.");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String host = uri.getHost();
            String currentHost = Uri.parse(getRemoteUrl()).getHost();
            return host != null && !host.equalsIgnoreCase(currentHost);
        }
    }
}
