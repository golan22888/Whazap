package com.example.golan.whazap;

import android.content.Intent;
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


public class UsersListActivity extends AppCompatActivity {
    String myName, myID, userClicked, userClickedID, chna;
    ListView listView;
    static final int CAMERA = 1, WRITE = 2;
    LinkedList<String> usersList = new LinkedList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.myUsers);
        getContacts();
        myName = Backendless.UserService.CurrentUser().getProperty("name").toString();
        myID = Backendless.UserService.CurrentUser().getObjectId();
        PermissionManager.check(this, android.Manifest.permission.CAMERA, CAMERA);
        PermissionManager.check(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE);
        listenToMyChannel(myName);
    }

  /*  @Override
    protected void onStart() {
        super.onStart();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                if (!(v instanceof AppCompatTextView)) {
                    userClicked = new UsersListAdapter(UsersListActivity.this, usersList).getItem(i);

                    Backendless.Data.of(BackendlessUser.class).find(new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<BackendlessUser> users) {
                            Iterator<BackendlessUser> userIterator = users.getCurrentPage().iterator();
                            while (userIterator.hasNext()) {
                                BackendlessUser user = userIterator.next();
                                if ((("" + user.getProperty("name")).equals(userClicked))) {
                                    //userClickedID = user.getObjectId();
                                    createChat();
                                    inviteToChat(userClicked, myName + "&" + userClicked);
                                }
                            }
                        }

                        public void handleFault(BackendlessFault e) {
                            Toast.makeText(UsersListActivity.this, "72 UserListActivity", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Intent mc = new Intent(UsersListActivity.this, ChatsListActivity.class);
                    // startActivity(mc);

                }
            }
        });
    }*/

    public void getContacts() {

        Backendless.Data.of(BackendlessUser.class).find(new AsyncCallback<List<BackendlessUser>>() {
            @Override
            public void handleResponse(List<BackendlessUser> response) {
                Iterator<BackendlessUser> userIterator = response.iterator();
                while (userIterator.hasNext()) {
                    BackendlessUser user = userIterator.next();
                    if (!(("" + user.getProperty("name")).equals(myName))) {
                        usersList.add("" + user.getProperty("name"));
                    }
                    listView.setAdapter(new UsersListAdapter(UsersListActivity.this, usersList));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
                            if (!(v instanceof AppCompatTextView)) {
                                userClicked = usersList.get(i);
                                createChat();
                                inviteToChat(userClicked, myName + "&" + userClicked);
                            }
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });}


    private void createChat() {
        Map chat = new HashMap();
        chat.put("ownerId", myID);//logged userClicked is the owner
        chat.put("name", myName + "&" + userClicked);//same as channel name - by chosen architecture
        Backendless.Data.of("Chats").save(chat, new AsyncCallback<Map>() {
            public void handleResponse(Map map) {
                String chatId = map.get("objectId").toString();//Id of created chat
                String chatName = map.get("name").toString();//name of created chat
                //Assume that xxx - is logged user
                addUserToChat(chatId, myID, chatName);//add self
            }

            public void handleFault(BackendlessFault e) {
                e.getCode();
            }
        });
    }

    private void addUserToChat(final String chatId, final String userId, final String chatName) {
        Map chatter = new HashMap();
        chatter.put("chatId", chatId);
        chatter.put("userId", userId);//Id of logged user
        Backendless.Data.of("Chatters").save(chatter, new AsyncCallback<Map>() {
            public void handleResponse(Map map) {
                //subscribeForChat(chatName);
                Toast.makeText(UsersListActivity.this, "chat created", Toast.LENGTH_SHORT).show();
            }

            public void handleFault(BackendlessFault e) {
                Toast.makeText(UsersListActivity.this, "does`nt work", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void listenToMyChannel(final String channelName) {
        Backendless.Messaging.registerDevice(LogInActivity.GCMSenderID, channelName, new AsyncCallback<Void>() {
            public void handleResponse(Void aVoid) {
                Toast.makeText(UsersListActivity.this, "successfully registered", Toast.LENGTH_SHORT).show();//successfully register
                Backendless.Messaging.subscribe(channelName, new AsyncCallback<List<Message>>() {
                    @Override
                    public void handleResponse(List<Message> messages) {
                        String msginfo = messages.get(0).getData().toString();
                        String[] parts = msginfo.split(",");
                        String inviterName = parts[0];
                        final String channelToListerTo = parts[1];
                        //Toast.makeText(UsersListActivity.this,inviterName+" invites you to his chat: "+channelToListerTo,Toast.LENGTH_LONG).show();
                        Intent i = new Intent(UsersListActivity.this, ChatActivity.class);
                        i.putExtra("partner name", inviterName);
                        i.putExtra("channel of the chat",channelToListerTo);
                        startActivity(i);
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Toast.makeText(UsersListActivity.this, "don`t have any message", Toast.LENGTH_LONG).show();
                    }
                }, new AsyncCallback<Subscription>() {
                    @Override
                    public void handleResponse(Subscription subscription) {
                        Toast.makeText(UsersListActivity.this, "listening to my channel", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Toast.makeText(UsersListActivity.this, "does not listen to channel", Toast.LENGTH_LONG).show();

                    }
                });

                /*Backendless.Messaging.subscribe(channelName ,new AsyncCallback<List<Message>> () {
                    public void handleResponse(List<Message> messages) {
                        Toast.makeText(UsersListActivity.this,"todoboom",Toast.LENGTH_LONG).show();
                        //message received
                        Message msg = messages.get(0);
                        String content = msg.getData().toString();//message content
                        //8.6.2017 - check what kind of content it is
                        //because - image is sent as URL string
                        //we should validate - if it is validImageUrl
                        if(validImageUrl(content)){
                            //use as image url - load image
                        }else{
                            //just show the string
                        }
                        msg.getHeaders().get("sendUserId");//gives me user id
                    }
                    public void handleFault(BackendlessFault e) {
                        //failed

                });*/
            }

            public void handleFault(BackendlessFault e) {
                //failed
            }
        });

    }

    private boolean validImageUrl(String url) {
        String baseUrl = "https://developer.backendless.com";
        //example without REGEX
        //return ((url.startsWith(baseUrl) && url.contains(appId)) && url.endsWith(".jpg") || url.endsWith(".png"));
        //example with REGEX
        String validChars = "[a-zA-Z.0-9_/\\-]{1,300}";//lower case OR upper case letters OR . OR digits (0-9) OR / OR - OR _
        return url.matches("^" + baseUrl + validChars + LogInActivity.appID + validChars + "jpg|png$");
    }

    private void inviteToChat(String hostName, String requestedChannel) {
        PublishOptions pOpts = new PublishOptions();
        //Assume that xxx - is logged user
        pOpts.putHeader("theChannel", hostName + "," + requestedChannel);//who sending the message
        //Channel == chatId
        Backendless.Messaging.publish(userClicked, hostName + "," + requestedChannel, pOpts, new AsyncCallback<MessageStatus>() {
            public void handleResponse(MessageStatus messageStatus) {
                Toast.makeText(UsersListActivity.this, "The request has been sent", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(UsersListActivity.this, ChatActivity.class);
                i.putExtra("partner name", userClicked);
                i.putExtra("channel of the chat", myName + "&" + userClicked);
                startActivity(i);
            }

            public void handleFault(BackendlessFault e) {
                Toast.makeText(UsersListActivity.this, "Failed to send the request", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

