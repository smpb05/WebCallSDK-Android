package kz.sm.webcallsdk;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class WebViewProvider {
    private static WebViewProvider instance;
    private Activity activity;
    private WebView webView;
    private EventListener eventListener;

    private String url;
    private final String urlAudio = "https://mvc.t2m.kz/demos/event-test.html";
    private final String urlVideo = "https://mvc.t2m.kz/demos/echotest.html";
    private boolean permissionsGranted = false;
    private boolean isVideoCall = false;


    public static WebViewProvider getProvider() {
        if (instance==null)
            instance = new WebViewProvider();

        return instance;
    }

    public void setWebView(Activity activity, WebView webView, boolean isVideoCall) {
        this.activity = activity;
        this.webView = webView;
        this.isVideoCall = isVideoCall;

        if (isVideoCall)
            url = urlVideo;
        else
            url = urlAudio;

        checkPermission();
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public boolean loadPage() {
        checkPermission();

        if (!permissionsGranted) {
            Log.e("WebViewProvider", "permissions are not allowed");
            return false;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(eventListener), "AndroidInterface");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false); // Разрешить автоматическое воспроизведение мультимедиа
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // Разрешить JavaScript открывать окна без разрешения
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                try {
                    request.grant(request.getResources());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        webView.loadUrl(url);
        return true;
    }

    public void setDeviceData() {
        String js = String.format("setDeviceData('{device: \"%s\", android: \"%s\"}');",
                Build.MODEL, Build.VERSION.RELEASE);
        Log.i("WebViewProvider", "setDeviceData executed");
        evaluate(js);
    }

    public void setUser(String phone) {
        String js = "setUserData('" + phone + "')";
        Log.i("WebViewProvider", "setUser executed");
        evaluate(js);
    }

    private void evaluate(String js) {
        if (activity==null) {
            Log.e("WebViewProvider", "has no activity, use setWebView");
            return;
        }
        activity.runOnUiThread(() ->
            webView.evaluateJavascript(js, result ->
                Log.i("WebViewProvider", "evaluateJavaScript result " + result)));
    }

    private ArrayList<String> getPermissions() {
        ArrayList<String> permission = new ArrayList<>();
        permission.add(Manifest.permission.CAMERA);
        permission.add(Manifest.permission.RECORD_AUDIO);
        return permission;
    }

    private void checkPermission() {
        boolean audioGranted = false;
        boolean videoGranted = false;
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            audioGranted = true;

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            videoGranted = true;

        Log.i("WebViewProvider", "microphone permission granted: " + audioGranted);
        Log.i("WebViewProvider", "camera permission granted: " + videoGranted);

        if ((!audioGranted || !videoGranted) && isVideoCall
                || !audioGranted)
            ActivityCompat.requestPermissions(activity, getPermissions().toArray(new String[0]), 1);
        else
            permissionsGranted = true;
    }
}
