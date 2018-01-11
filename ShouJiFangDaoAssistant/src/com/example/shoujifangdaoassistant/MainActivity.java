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

    @Override//�Ի��򵯳���
    protected void onResume() {
        super.onResume();
        changeButtonContent();
    }
    private void changeButtonContent() {
        getBindStatus();
        if(isBind)
            bindButton.setText("�����");
        else
            bindButton.setText("��");
    }
    private void getBindStatus() {
        SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
         bindId=pref.getString("bindId","");
        isBind=!bindId.isEmpty();//bindId�ǿյĻ�isband����Ϊfalse
    }
    
    public void setButtons() {
        if(bindButton!=null)
            return;//������ �ͷ���
        
        bindButton= (Button) findViewById(R.id.button_bind);
        findButton= (Button) findViewById(R.id.button_find);
        bindButton.setOnClickListener(this);
        findButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_bind:
                if(!isBind)//���û�󶨽����activity
                    startActivity(new Intent(this,BindActivity.class));
                else{//���򵯳��Ի��� ȡ����
                    new AlertDialog.Builder(MainActivity.this).setTitle("��ʾ")
                            .setMessage("ȷ��Ҫȡ������")
                            .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    unBind();
                                    refresh();
                                }
                            }).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
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
         localId=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();//��ȡ�豸����Ϊ�Լ��û���
         bindId=pref.getString("bindId","");//bindId�ǶԷ��û���
         
        AVIMClient ac=AVIMClient.getInstance(localId);
        ac.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e == null) {
                    final AVIMConversation conv = avimClient.getConversation(convId);
                    conv.kickMembers(Arrays.asList(bindId, localId), new AVIMConversationCallback() {//�Ƴ�˫����ɾ���Ի�
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                            	//�Ի��źͶԷ���������Ϊ��
                                SharedPreferences pref=getSharedPreferences("data",MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("convId", "");
                                editor.putString("bindId", "");
                                editor.commit();
                                
                                changeButtonContent();//�л���ť����
                                Toast.makeText(MainActivity.this, "�������ɣ�", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(MainActivity.this,"�����ʧ�ܣ��������������",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void refresh() {
        startActivity(new Intent(this,getClass()));
    }
}
