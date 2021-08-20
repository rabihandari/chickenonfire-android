package com.zeappa.chickenonfire.tools;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.zeappa.chickenonfire.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class KnetWebView extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.knet_webview);

        WebView webView = findViewById(R.id.web_view);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String url = extras.getString("URL");
            final String successSecretKey = extras.getString("Success Secret Key");
            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url) {

                    Intent intent = new Intent();

                    if(url.contains("paymentSuccess")){

                        assert successSecretKey != null;
                        if(url.contains(successSecretKey)){
                            intent.putExtra("Payment Status", 1);
                            setResult(RESULT_OK, intent);
                            finish();
                        }else {

                            intent.putExtra("Payment Status", 0);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    }else if (url.contains("paymentfailure")){
                        intent.putExtra("Payment Status", 0);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                }
            });
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.loadUrl(url);
        }


    }
}

