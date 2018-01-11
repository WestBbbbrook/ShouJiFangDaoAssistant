package com.example.shoujifangdaoassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MainActivity extends Activity implements View.OnClickListener{
    /**
     * Called when the activity is first created.
     */
	String convId;
    String localId;
    String bindId;
    private Button bindButton,findButton;
    private boolean isBind=false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtons();
        changeButtonContent();
    }

    @Override//对话框弹出在
    protected void onResume() {
        super.onResume();
        changeButtonContent();
    }
    private void changeButtonContent() {
        getBindStatus();
        if(isBind)
            bindButton.setText("解除绑定");
        else
            bindButton.setText("绑定");
    }
    private void getBindStatus() {
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
         bindId=pref.getString("bindId","");
        isBind=!bindId.isEmpty();//bindId是空的话isband设置为false
    }
    
    public void setButtons() {
        if(bindButton!=null)
            return;//如果绑空 就返回
        
        bindButton= (Button) findViewById(R.id.button_bind);
        findButton= (Button) findViewById(R.id.button_find);
        bindButton.setOnClickListener(this);
        findButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_bind:
                if(!isBind)//如果没绑定进入绑定activity
                    startActivity(new Intent(this,BindActivity.class));
                else{//否则弹出对话框 取消绑定
                    new AlertDialog.Builder(MainActivity.this).setTitle("提示")
                            .setMessage("确定要取消绑定吗")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unBind();
                                    refresh();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
                break;
            case R.id.button_find:
            	startActivity(new Intent(this,PreFindActivity.class));
            	break;
        }
    }
    

    private void unBind() {
        stopService(new Intent(this,ConnectBaas.class));
        
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
         convId=pref.getString("convId", "");
         localId=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();//获取设备码作为自己用户名
         bindId=pref.getString("bindId","");//bindId是对方用户名
         
        AVIMClient ac=AVIMClient.getInstance(localId);
        ac.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    final AVIMConversation conv = avimClient.getConversation(convId);
                    conv.kickMembers(Arrays.asList(bindId, localId), new AVIMConversationCallback() {//移除双方，删除对话
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                            	//对话号和对方号码设置为空
                                SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("convId", "");
                                editor.putString("bindId", "");
                                editor.commit();
                                
                                changeButtonContent();//切换按钮字样
                                Toast.makeText(MainActivity.this, "解除绑定完成！", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this,"解除绑定失败，请检查网络后重试",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void refresh() {
        startActivity(new Intent(this,getClass()));
    }
}
