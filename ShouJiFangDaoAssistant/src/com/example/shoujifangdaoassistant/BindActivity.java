package com.example.shoujifangdaoassistant;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;

import java.util.Arrays;

/**
 * Created by xjk on 2016/3/3.
 */
public class BindActivity extends Activity {

    String bindId;
    String token;
    private Button bindButton;
    private EditText bindIdEditText,tokenEditText;
    String localId;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bind);
      //用imei作为自己唯一帐号，不需要输入，可以改成输入的
        localId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        bindIdEditText= (EditText) findViewById(R.id.editText_bindId);
        tokenEditText= (EditText) findViewById(R.id.editText_token);
        setButton();
    }
    public void setButton(){
        bindButton= (Button) findViewById(R.id.button_bind);
        bindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 bindId=bindIdEditText.getText().toString();
                 token=tokenEditText.getText().toString();
                AVIMClient ac = AVIMClient.getInstance(localId);
                ac.open(new AVIMClientCallback() {
                    @Override
                    public void done(AVIMClient client, AVIMException e) {
                        if (e == null) {
                            client.createConversation(Arrays.asList(bindId),token, null,
                                    new AVIMConversationCreatedCallback() {
                                        @Override
                                        public void done(AVIMConversation conv, AVIMException e) {
                                            if (e == null) {
                                                Toast.makeText(BindActivity.this,"bind OK!",Toast.LENGTH_LONG).show();
                                                SharedPreferences.Editor edit=getSharedPreferences("data",MODE_PRIVATE).edit();
                                                edit.putString("bindId",bindId);
                                                edit.putString("convId",conv.getConversationId());
                                                edit.commit();//把绑定号码和对话号码存起来
                                                Toast.makeText(BindActivity.this,"OK,your IMEI is "+localId,Toast.LENGTH_SHORT).show();
                                                //后台启动服务实时监听控制信息
                                                startService(new Intent(BindActivity.this, ConnectBaas.class));
                                                //返回主界面
                                                startActivity(new Intent(BindActivity.this, MainActivity.class));
                                                BindActivity.this.finish();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });
    }
}