package kz.sm.webcallsdk;

import android.webkit.JavascriptInterface;

public class WebAppInterface {
    private EventListener event;

    public WebAppInterface(EventListener event) {
        this.event = event;
    }

    @JavascriptInterface
    public void postMessage(String message) {
        switch (message.trim()) {
            case "onCallFinished":
                if (event!=null)
                    event.onCallFinished();
                break;
            case "getDeviceData":
                WebViewProvider.getProvider().setDeviceData();
                break;
            default:
                break;
        }
    }
}
