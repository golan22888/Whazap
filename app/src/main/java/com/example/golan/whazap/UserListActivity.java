package com.example.golan.whazap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.example.golan.whazap.adapters.UsersListAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class UserListActivity extends AppCompatActivity {
    String myName, myID, userClicked,channelName,oppositeChannelName;
    ListView listView;
    static final int CAMERA = 1, WRITE = 2;
    LinkedList<String> usersList = new LinkedList<>();
    SharedPreferences prefsChannels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        listView = (ListView) findViewById(R.id.myUsers);
        getContacts();
        myName = Backendless.UserService.CurrentUser().getProperty("name").toString();
        myID = Backendless.UserService.CurrentUser().getObjectId();
        PermissionManager.check(this, android.Manifest.permission.CAMERA, CAMERA);
        PermissionManager.check(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE);
        listenToMyChannel(myName);
        prefsChannels = getSharedPreferences("channels i have", 0);

    }


    public void getContacts() {

        Backendless.Persistence.of(BackendlessUser.class).find(new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                Iterator<BackendlessUser> userIterator = response.iterator();
                while (userIterator.hasNext()) {
                    BackendlessUser user = userIterator.next();
                    if (!(("" + user.getProperty("name")).equals(myName))) {
                        usersList.add("" + user.getProperty("name"));
                    }
                }
                listView.setAdapter(new UsersListAdapter(UserListActivity.this, usersList));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                        if (!(v instanceof AppCompatTextView)) {
                            userClicked = usersList.get(i);
                            createChat();
                            channelName = myName + "&" + userClicked;
                            oppositeChannelName = userClicked + "&" + myName;
                            inviteToChat();

                        }
                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(UserListActivity.this,"Userlistactivity line 106",Toast.LENGTH_LONG).show();
            }
        });}

    private void createChat() {
        Map chat = new HashMap();
        chat.put("ownerId", myID);
        chat.put("name", myName + "&" + userClicked);
        Backendless.Data.of("Chats").save(chat, new AsyncCallback<Map>() {
            public void handleResponse(Map map) {
                String chatId = map.get("objectId").toString();
                addUserToChat(chatId, myID);
            }

            public void handleFault(BackendlessFault e) {
                e.getCode();
            }
        });
    }

    private void addUserToChat(final String chatId, final String userId) {
        Map chatter = new HashMap();
        chatter.put("chatId", chatId);
        chatter.put("userId", userId);//Id of logged user
        Backendless.Data.of("Chatters").save(chatter, new AsyncCallback<Map>() {
            public void handleResponse(Map map) {
                Toast.makeText(UserListActivity.this, "chat created", Toast.LENGTH_SHORT).show();
            }

            public void handleFault(BackendlessFault e) {
                Toast.makeText(UserListActivity.this, "does`nt work", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void listenToMyChannel(final String inviteToChatChannel) {
        Backendless.Messaging.registerDevice(LogInActivity.GCMSenderID, inviteToChatChannel, new AsyncCallback<Void>() {
            public void handleResponse(Void aVoid) {
                Toast.makeText(UserListActivity.this, "successfully registered", Toast.LENGTH_SHORT).show();//successfully register
                    Backendless.Messaging.subscribe(inviteToChatChannel, new AsyncCallback<List<Message>>() {
                        @Override
                        public void handleResponse(List<Message> messages) {
                            String msginfo = messages.get(0).getData().toString();
                            String[] parts = msginfo.split(",");
                            final String channelToListerTo = parts[1];
                            Intent i = new Intent(UserListActivity.this, ChatsListActivity.class);
                            prefsChannels.edit().putString(channelToListerTo,channelToListerTo).apply();
                            startActivity(i);
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            Toast.makeText(UserListActivity.this, "don`t have any message", Toast.LENGTH_LONG).show();
                        }
                    }, new AsyncCallback<Subscription>() {
                        @Override
                        public void handleResponse(Subscription subscription) {
                            Toast.makeText(UserListActivity.this, "listening to my channel", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            Toast.makeText(UserListActivity.this, "does not listen to channel", Toast.LENGTH_LONG).show();
                        }
                    });
            }

            public void handleFault(BackendlessFault e) {
                //failed
            }
        });

    }

    private void inviteToChat() {
        if(!(prefsChannels.contains(channelName)||prefsChannels.contains(oppositeChannelName))) {
            PublishOptions pOpts = new PublishOptions();
            pOpts.putHeader("theChannel", channelName);
            Backendless.Messaging.publish(userClicked, channelName, pOpts, new AsyncCallback<MessageStatus>() {
                public void handleResponse(MessageStatus messageStatus) {
                    Toast.makeText(UserListActivity.this, "The request has been sent", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(UserListActivity.this, ChatsListActivity.class);
                    prefsChannels.edit().putString(channelName,channelName).apply();
                    startActivity(i);
                }
                public void handleFault(BackendlessFault e) {
                    Toast.makeText(UserListActivity.this, "UserListActivity 193", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(UserListActivity.this,"you already have chat with that user", Toast.LENGTH_LONG).show();
        }
    }

}

