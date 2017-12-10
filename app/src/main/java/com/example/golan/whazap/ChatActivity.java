package com.example.golan.whazap;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.Subscription;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.messaging.Message;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;
import com.example.golan.whazap.adapters.MessagesAdapter;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    SharedPreferences prefsLeavingTime, prefsChannels;
    public final int CAMERA = 1, WRITE = 2;
    EditText chatText;
    ListView chatList;
    String myName, myID, partnerName, chatChannel, hourOfDay, minuteOfHour, msg;
    ImageView img;
    File imgFile;
    Date leavingChatTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatText = (EditText) findViewById(R.id.chatText);
        chatList = (ListView) findViewById(R.id.chatList);
        chatList.setAdapter(new MessagesAdapter(this));
        partnerName = getIntent().getStringExtra("partner name");
        chatChannel = getIntent().getStringExtra("channel of the chat");
        getSupportActionBar().setTitle(partnerName);
        myName = Backendless.UserService.CurrentUser().getProperty("name").toString();
        myID = Backendless.UserService.CurrentUser().getObjectId();
        subscribeForChat(chatChannel);
        PermissionManager.check(this, Manifest.permission.CAMERA, CAMERA);
        PermissionManager.check(this, Manifest.permission.CAMERA, WRITE);
        img = (ImageView) findViewById(R.id.attachment);
        prefsLeavingTime = getSharedPreferences("the time i left the chat" + chatChannel, 0);
        prefsChannels = getSharedPreferences("channels i have", 0);
        prefsChannels.edit().putString(chatChannel, chatChannel).apply();
        cachUpMsg();
    }

    private void subscribeForChat(final String channelName) {
        Backendless.Messaging.registerDevice(LogInActivity.GCMSenderID, channelName, new AsyncCallback<Void>() {
            public void handleResponse(Void aVoid) {
                Toast.makeText(ChatActivity.this, "successfully listen to chat", Toast.LENGTH_SHORT).show();//successfully register
                Backendless.Messaging.subscribe(channelName, new AsyncCallback<List<Message>>() {
                    @Override
                    public void handleResponse(List<Message> messages) {
                        if (!myName.equals(messages.get(0).getHeaders().get("the partner"))) {
                            msg = messages.get(0).getData().toString();
                            hourOfDay = "" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                            minuteOfHour = "" + Calendar.getInstance().get(Calendar.MINUTE);
                            ((MessagesAdapter) chatList.getAdapter()).addMessage(new com.example.golan.whazap.ObjectIMade.Message(hourOfDay + ":" + minuteOfHour, partnerName, msg));
                            Toast.makeText(ChatActivity.this, "SHOULD DO THE MAGIC", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Toast.makeText(ChatActivity.this, "don`t have any message", Toast.LENGTH_LONG).show();
                    }
                }, new AsyncCallback<Subscription>() {
                    @Override
                    public void handleResponse(Subscription subscription) {
                        Toast.makeText(ChatActivity.this, "listening to " + channelName, Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Toast.makeText(ChatActivity.this, "does not listen to channel", Toast.LENGTH_LONG).show();

                    }
                });
            }

            public void handleFault(BackendlessFault e) {
                //failed
            }
        });
    }

    public void sendMsg(View v) {
        String input = chatText.getText().toString();
        if (!input.isEmpty()) {
            send(input);
            chatText.setText("");

        }
    }

    private void send(final String msg) {
        PublishOptions pOpts = new PublishOptions();
        savemsg(msg);
        pOpts.putHeader("the partner", myName);
        if (!validImageUrl(msg)) {
            hourOfDay = "" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            minuteOfHour = "" + Calendar.getInstance().get(Calendar.MINUTE);
            ((MessagesAdapter) chatList.getAdapter()).addMessage(new com.example.golan.whazap.ObjectIMade.Message(hourOfDay + ":" + minuteOfHour, myName, msg));
        }
        Backendless.Messaging.publish(chatChannel, msg, pOpts, new AsyncCallback<MessageStatus>() {
            public void handleResponse(MessageStatus messageStatus) {
                Toast.makeText(ChatActivity.this, "message received by other side", Toast.LENGTH_SHORT).show();
                //add message to adapter
            }

            public void handleFault(BackendlessFault e) {
                Toast.makeText(ChatActivity.this, "ChatActivity, 124", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void attach(View v) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        tempFile();//create temporary File container for img - and pass to camera
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));
        startActivityForResult(i, CAMERA);
    }

    private void tempFile() {
        String fileName = "IMG_" + Math.random() + System.currentTimeMillis() + ".jpg";
        imgFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);
        if (resultCode == RESULT_OK && requestCode == CAMERA) {
            msg = imgFile.getAbsolutePath();
            hourOfDay = "" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            minuteOfHour = "" + Calendar.getInstance().get(Calendar.MINUTE);
            ((MessagesAdapter) chatList.getAdapter()).addMessage(new com.example.golan.whazap.ObjectIMade.Message(hourOfDay + ":" + minuteOfHour, myName, msg));
            saveImg();
        }
    }

    public void saveImg() {
        Backendless.Files.upload(imgFile, "/golan/" + imgFile.getName(), new AsyncCallback<BackendlessFile>() {
            public void handleResponse(BackendlessFile res) {//success handling
                final String url = res.getFileURL();
                //save in Data - messages with given file
                Map msg = new HashMap();
                msg.put("Text", url);
                msg.put("senderName", myName);
                msg.put("chatName", chatChannel);
                Backendless.Data.of("Messages").save(msg, new AsyncCallback<Map>() {
                    public void handleResponse(Map res) {
                        Toast.makeText(ChatActivity.this, "image saved", Toast.LENGTH_LONG).show();
                        send(url);
                    }

                    public void handleFault(BackendlessFault e) {
                        Toast.makeText(ChatActivity.this, e.getCode(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void handleFault(BackendlessFault e) {//error handling
                Log.e("File upload", "FAILED with error " + e.getCode());
            }
        });
    }


    private boolean validImageUrl(String url) {
        String baseUrl = "https://api.backendless.com";
        //String validChars = "[a-zA-Z.0-9_/\\-]{1,300}";//lower case OR upper case letters OR . OR digits (0-9) OR / OR - OR _
        return url.matches("^" + baseUrl /*+ validChars*/ + LogInActivity.appID.toLowerCase() + /*validChars +*/ "jpg|png$");
    }

    public void savemsg(String text) {
        Map msg = new HashMap();
        msg.put("Text", text);
        msg.put("senderName", myName);
        msg.put("chatName", chatChannel);
        Backendless.Data.of("Messages").save(msg, new AsyncCallback<Map>() {
            public void handleResponse(Map res) {
                Toast.makeText(ChatActivity.this, "MSGBackedUp", Toast.LENGTH_LONG).show();
            }

            public void handleFault(BackendlessFault e) {
                Toast.makeText(ChatActivity.this, "ChatActivity,211", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        leavingChatTime = Calendar.getInstance().getTime();
        prefsLeavingTime.edit().putLong("the time", leavingChatTime.getTime()).apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //leavingChatTime = Calendar.getInstance().getTime();
        leavingChatTime = new Date(prefsLeavingTime.getLong("the time", 0));
        String whereClause = "created after " + leavingChatTime.getTime();
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("Messages").find(dataQueryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                for (int i = 0; i < response.size(); i++) {
                    Toast.makeText(ChatActivity.this, response.get(i).get("ChatName") + "woooooorrrkkkeeddd", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }

    public void cachUpMsg() {
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setPageSize(100);
        dataQueryBuilder.setSortBy("created");
        String whereClause = "ChatName = '"+chatChannel+"'";
        dataQueryBuilder.setWhereClause(whereClause);

        Backendless.Persistence.of("Messages").find(dataQueryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response) {
                int listLength = response.size();
                System.out.println(response);
                for (int i = 0; i < listLength; i++) {
                    hourOfDay = response.get(i).get("created").toString();
                    msg = response.get(i).get("Text").toString();
                    partnerName = response.get(i).get("SenderName").toString();
                    ((MessagesAdapter) chatList.getAdapter()).addMessage(new com.example.golan.whazap.ObjectIMade.Message(hourOfDay, partnerName, msg));
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }
}
