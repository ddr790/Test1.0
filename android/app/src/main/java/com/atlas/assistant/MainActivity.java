package com.atlas.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int REQ_MEDIA = 1001;
    private static final int REQ_NOTIFICATIONS = 1002;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AtlasBridge(this), "ATLASNative");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean ok = true;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ok = false;
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) ok = false;
                    }
                    if (ok) request.grant(request.getResources()); else request.deny();
                });
            }
        });
        setContentView(webView);
        requestRuntimePermissions();
        createNotificationChannel();
        webView.loadUrl("file:///android_asset/public/index.html");
    }

    private void requestRuntimePermissions() {
        java.util.ArrayList<String> p = new java.util.ArrayList<>();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.CAMERA);
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.RECORD_AUDIO);
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!p.isEmpty()) ActivityCompat.requestPermissions(this, p.toArray(new String[0]), REQ_MEDIA);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("atlas_reminders", "ATLAS Lembretes", NotificationManager.IMPORTANCE_HIGH);
            c.setDescription("Lembretes e compromissos do ATLAS");
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    public static class AtlasBridge {
        private final MainActivity a;
        AtlasBridge(MainActivity a){this.a=a;}
        @JavascriptInterface public void share(String text){
            Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT,text);
            a.startActivity(Intent.createChooser(i,"Compartilhar com"));
        }
        @JavascriptInterface public void openUrl(String url){try{a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception ignored){}}
        @JavascriptInterface public void dial(String number){try{a.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(number))));}catch(Exception ignored){}}
        @JavascriptInterface public void message(String number,String body){try{Intent i=new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+Uri.encode(number))); i.putExtra("sms_body",body); a.startActivity(i);}catch(Exception ignored){try{a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"+Uri.encode(number)+"?body="+Uri.encode(body))));}catch(Exception ignored2){}}}
        @JavascriptInterface public void addCalendar(String title,String dateTime,String notes,long durationMinutes){
            try {
                java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US);
                long s=f.parse(dateTime).getTime(), e=s+Math.max(1,durationMinutes)*60000L;
                Intent i=new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE,title).putExtra(CalendarContract.Events.DESCRIPTION,notes)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,s).putExtra(CalendarContract.EXTRA_EVENT_END_TIME,e);
                a.startActivity(i);
            } catch(Exception ignored) {}
        }
        @JavascriptInterface public void scheduleReminder(String id,String title,String body,long triggerAt){
            try {
                AlarmManager am=(AlarmManager)a.getSystemService(Context.ALARM_SERVICE);
                Intent i=new Intent(a, ReminderReceiver.class).putExtra("title",title).putExtra("body",body);
                int code=Math.abs(id.hashCode()); PendingIntent pi=PendingIntent.getBroadcast(a,code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
                if (Build.VERSION.SDK_INT >= 23) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pi); else am.set(AlarmManager.RTC_WAKEUP,triggerAt,pi);
            } catch(Exception ignored) {}
        }
        @JavascriptInterface public void cancelReminder(String id){
            try { AlarmManager am=(AlarmManager)a.getSystemService(Context.ALARM_SERVICE); Intent i=new Intent(a, ReminderReceiver.class); int code=Math.abs(id.hashCode()); PendingIntent pi=PendingIntent.getBroadcast(a,code,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE); if(pi!=null){am.cancel(pi);pi.cancel();} } catch(Exception ignored) {}
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
