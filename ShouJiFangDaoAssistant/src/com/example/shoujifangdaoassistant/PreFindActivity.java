package com.example.shoujifangdaoassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xjk on 2016/3/7.
 */
public class PreFindActivity extends Activity {

    private List<AVIMConversation> realConversation;
    private ListView listView;
    String localId;
	AVIMClient ac;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_find);
        
        listView= (ListView) findViewById(R.id.listView);
        Toast.makeText(this,"find...",Toast.LENGTH_SHORT).show();
        getList();
    }
    private void getList() {
        localId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
         ac=AVIMClient.getInstance(localId);
        ac.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient avimClient, AVIMException e) {
                if (e != null) {
                    Toast.makeText(PreFindActivity.this, "无法连接到服务器，请检查你的网络设置", Toast.LENGTH_SHORT).show();
                } else {
                    AVIMConversationQuery query = avimClient.getQuery();
                    query.limit(5);
                    query.findInBackground(new AVIMConversationQueryCallback() {
                        @Override
                        public void done(List<AVIMConversation> list, AVIMException e) {
                            ac.close(new AVIMClientCallback() {
                                @Override
                                public void done(AVIMClient avimClient, AVIMException e) {
                                }
                            });
                            if (e == null) {
                                Toast.makeText(PreFindActivity.this, "查找完毕", Toast.LENGTH_SHORT).show();
                                filterConversation(list);
                                displayValidList();
                            }else{
                                Toast.makeText(PreFindActivity.this, "从服务器获取数据失败", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PreFindActivity.this,MainActivity.class));
                                finish();
                            }
                        }
                    });

                }
            }
        });
    }
    private void filterConversation(List<AVIMConversation> allConvs) {
        List<AVIMConversation> realConvs=new ArrayList<AVIMConversation>();
        for(AVIMConversation conversation:allConvs) {
            if(!conversation.getCreator().equals(localId)) {
                realConvs.add(conversation);
            }
        }
        realConversation=realConvs;
    }
    private void displayValidList(){
        List<String> data=new ArrayList<String>();
        for(AVIMConversation conversation:realConversation) {
            data.add(conversation.getCreator());
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1,data );
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(PreFindActivity.this,FindActivity.class);
                intent.putExtra("convId",realConversation.get(position).getConversationId());
                startActivity(intent);
                finish();
            }
        });
    }
}
