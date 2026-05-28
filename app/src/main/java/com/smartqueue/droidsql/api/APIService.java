package com.smartqueue.droidsql.api;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.smartqueue.droidsql.MainActivity;
import com.smartqueue.droidsql.model.DatabaseManager;

import java.net.NetworkInterface;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Foreground service to keep the DroidSQL REST API server running in the background
 * even when the main app process is closed or cleared by the user.
 */
public class APIService extends Service {
    private static final String CHANNEL_ID = "DroidSQL_API_Server_Channel";
    private static final int NOTIFICATION_ID = 1001;

    public static final String ACTION_START = "com.smartqueue.droidsql.api.action.START";
    public static final String ACTION_STOP = "com.smartqueue.droidsql.api.action.STOP";

    private static APIServer apiServerInstance;

    public static synchronized APIServer getApiServerInstance() {
        return apiServerInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopServer();
            stopSelf();
            return START_NOT_STICKY;
        }

        startServer();
        return START_STICKY;
    }

    private synchronized void startServer() {
        if (apiServerInstance == null) {
            DatabaseManager dbManager = DatabaseManager.getInstance(this);
            android.content.SharedPreferences prefs = getSharedPreferences("APISecurityPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("api_auth_token", "");
            apiServerInstance = new APIServer(dbManager, token);
            apiServerInstance.start();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Intent stopIntent = new Intent(this, APIService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String ip = getLocalIpAddress();
        int port = apiServerInstance.getPort();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("DroidSQL REST API Server")
                .setContentText("Running on http://" + ip + ":" + port)
                .setSmallIcon(android.R.drawable.ic_menu_share)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Server", stopPendingIntent)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private synchronized void stopServer() {
        if (apiServerInstance != null) {
            apiServerInstance.stop();
            apiServerInstance = null;
        }
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DroidSQL REST API Server",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows notification when REST API Server is running in the background");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private String getLocalIpAddress() {
        try {
            android.net.wifi.WifiManager wm = (android.net.wifi.WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                int ipAddress = wm.getConnectionInfo().getIpAddress();
                if (ipAddress != 0) {
                    return String.format(Locale.US, "%d.%d.%d.%d",
                            (ipAddress & 0xff),
                            (ipAddress >> 8 & 0xff),
                            (ipAddress >> 16 & 0xff),
                            (ipAddress >> 24 & 0xff));
                }
            }
        } catch (Exception ignored) {}

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = inetAddress.getHostAddress();
                        if (ip != null && ip.indexOf(':') < 0) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}
