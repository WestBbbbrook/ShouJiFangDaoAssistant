package com.example.shoujifangdaoassistant;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

/**
 * Created by xjk on 2016/3/3.
 */
public class Main extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "SvNjRKqrgTK0LGUPy6jouBBi-gzGzoHsz", "09rdpXCnXd8yKTaUXJilhR53");
        //startService(new Intent(this,ConnectBaas.class));

    }
}
