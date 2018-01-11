package com.example.shoujifangdaoassistant;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.util.List;

/**
 * Created by xjk on 2016/3/4.
 */
public class ConnectBaas extends Service {

    private  double latitude,longitude;
    String bindId;
    private LocationManager locationManager;
    private String provider;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
        checkGps();
        setLocationListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        String localId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        bindId=pref.getString("bindId","");
        AVIMClient avimClient = AVIMClient.getInstance(localId);
        avimClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    public class CustomMessageHandler extends AVIMMessageHandler {
        @Override
        public void onMessage(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
            if(!message.getFrom().equals(bindId))
                return;
            if(message instanceof AVIMTextMessage){
                String content=((AVIMTextMessage)message).getText();
                AVIMTextMessage msg= new AVIMTextMessage();
                if(content.equals("#position")) {
                    msg.setText(latitude+","+longitude);
                    conversation.sendMessage(msg, new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {

                        }
                    });
                }
            }
        }

        public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }

    private void checkGps() {
        LocationManager locationManager
                = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        //boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps) {
            Intent GPSIntent = new Intent();
            GPSIntent.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
            GPSIntent.setData(Uri.parse("custom:3"));
            try {
                PendingIntent.getBroadcast(this, 0, GPSIntent, 0).send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }
    public void setLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = locationManager.GPS_PROVIDER;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            showLocation(location);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                5000, 1, locationListener);
        Log.d("GPSService", "onStartCommand3 executed");

    }

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
            //Log.d("GPSService", "onStartCommand4 executed");
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };

    public void showLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }
}

