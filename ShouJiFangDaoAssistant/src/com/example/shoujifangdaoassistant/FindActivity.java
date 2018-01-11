package com.example.shoujifangdaoassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.os.PersistableBundle;
import android.sax.TextElementListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.avos.avoscloud.im.v2.*;
import com.avos.avoscloud.im.v2.callback.*;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xjk on 2016/3/4.
 */
public class FindActivity extends Activity implements View.OnClickListener{
    public class CustomMessageHandler extends AVIMMessageHandler {
        @Override
        public void onMessage(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
            String text=textView.getText().toString();
            text+="\n"+((AVIMTextMessage)message).getText();
            textView.setText(text);
        }

        public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        photoButton= (Button) findViewById(R.id.button_photo);
        positionButton= (Button) findViewById(R.id.button_position);
        textView= (TextView) findViewById(R.id.textView);
        
        Intent intent=getIntent();
        String convId=intent.getStringExtra("convId");
        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
        tryToConnectLossPhone(convId);
        
        setButtons();
    }
    private void setButtons() {
        positionButton.setOnClickListener(this);
        photoButton.setOnClickListener(this);
    }
    private Button photoButton,positionButton,soundButton;
    public TextView textView;
    private AVIMConversation conversation=null;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_position:
                if(conversation==null)
                    return;
                AVIMTextMessage msg=new AVIMTextMessage();
                msg.setText("#position");
                conversation.sendMessage(msg, new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                        if (e == null) {
                            Toast.makeText(FindActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

        }
    }
    private void tryToConnectLossPhone(final String conversationId) {
        String localId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        AVIMClient ac = AVIMClient.getInstance(localId);
        ac.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if(e!=null) {
                    Toast.makeText(FindActivity.this,"无法连接到服务器",
                            Toast.LENGTH_LONG).show();
                }else {
                    AVIMConversationQuery conversationQuery=avimClient.getQuery();
                    conversationQuery.whereEqualTo("objectId",conversationId);
                    conversationQuery.findInBackground(new AVIMConversationQueryCallback() {
                        @Override
                        public void done(List<AVIMConversation> convs, AVIMException e) {
                            if(e==null) {
                                Toast.makeText(FindActivity.this,"连接服务器成功",
                                        Toast.LENGTH_SHORT).show();
                                AVIMConversation conv=convs.get(0);
                                conversation=conv;
                                conv.getMemberCount(new AVIMConversationMemberCountCallback() {
                                    @Override
                                    public void done(Integer count, AVIMException e) {
                                        if(e==null) {
                                            if(count<2) {
                                                Toast.makeText(FindActivity.this,"对方处于离线状态，你可以尝试发送离线消息",
                                                        Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(FindActivity.this,"对方在线，你现在可以发送命令并及时收到回复",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}
