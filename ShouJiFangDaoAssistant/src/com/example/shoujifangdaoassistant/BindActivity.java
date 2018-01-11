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
      //��imei��Ϊ�Լ�Ψһ�ʺţ�����Ҫ���룬���Ըĳ������
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
                                                edit.commit();//�Ѱ󶨺���ͶԻ����������
                                                Toast.makeText(BindActivity.this,"OK,your IMEI is "+localId,Toast.LENGTH_SHORT).show();
                                                //��̨��������ʵʱ����������Ϣ
                                                startService(new Intent(BindActivity.this, ConnectBaas.class));
                                                //����������
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