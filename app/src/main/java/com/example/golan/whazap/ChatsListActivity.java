package com.example.golan.whazap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.example.golan.whazap.adapters.ChatsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by golan on 21/06/2017.
 */

public class ChatsListActivity extends AppCompatActivity {
    ListView channelsList;
    List<String> list, channelsNames;
    String channelClicked, myName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);
        list = new ArrayList<>();
        channelsNames = new ArrayList<>();
        channelsList = (ListView) findViewById(R.id.channelsList);
        channelsList.setAdapter(new ChatsAdapter(this, list));
        myName = Backendless.UserService.CurrentUser().getProperty("name").toString();
        listenToMyChannel(myName);
        getChats();
    }

    public void getChats() {
        Backendless.Persistence.of("Chats").find(new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                int listLength = response.size();
                for (int i = 0; i < listLength; i++) {
                    String s = response.get(i).get("name").toString();
                    if (s.contains(myName)) {
                        //channelsNames.add(s);
                        //subscribeForChat(s);
                        ((ChatsAdapter) channelsList.getAdapter()).addChat(s);
                    }
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ChatsListActivity.this, "64 ChatsListActivity", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        channelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                channelClicked = new ChatsAdapter(ChatsListActivity.this, list).getItem(pos);
                String[] parts = channelClicked.split("&", 2);
                Intent i = new Intent(ChatsListActivity.this, ChatActivity.class);
                i.putExtra("partner name", myName.equals(parts[0]) ? parts[1] : parts[0]);
                i.putExtra("channel of the chat", channelClicked);
                startActivity(i);
            }
        });
    }

    private void listenToMyChannel(final String inviteToChatChannel) {

        Backendless.Messaging.registerDevice(LogInActivity.GCMSenderID, inviteToChatChannel, new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Backendless.Messaging.subscribe(inviteToChatChannel, new AsyncCallback<List<Message>>() {
                    @Override
                    public void handleResponse(List<Message> messages) {
                        Toast.makeText(ChatsListActivity.this, messages.get(0).getData().toString(), Toast.LENGTH_LONG).show();
                        String msginfo = messages.get(0).getData().toString();
                        ((ChatsAdapter) channelsList.getAdapter()).addChat(new String(msginfo));
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ChatsListActivity.this, "don`t have any message", Toast.LENGTH_LONG).show();
                    }
                }, new AsyncCallback<Subscription>() {
                    @Override
                    public void handleResponse(Subscription response) {
                        Toast.makeText(ChatsListActivity.this, "listening to my channel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ChatsListActivity.this, "does not listen to channel", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ChatsListActivity.this, "does not listen to channel", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addChat(View v) {
        Intent i = new Intent(this, UserListActivity.class);
        startActivity(i);
    }
}


