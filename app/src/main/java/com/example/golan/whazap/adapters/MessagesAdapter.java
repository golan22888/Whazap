package com.example.golan.whazap.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.backendless.Backendless;
import com.example.golan.whazap.HttpRequest;
import com.example.golan.whazap.LogInActivity;
import com.example.golan.whazap.ObjectIMade.Message;
import com.example.golan.whazap.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Looper.getMainLooper;

/**
 * Created by golan on 27/06/2017.
 */

public class MessagesAdapter extends BaseAdapter {
    Handler h = new Handler(getMainLooper());
    private AppCompatActivity context;
    Bitmap bmp,bm;
    private final ArrayList<Message> messages;


    public MessagesAdapter(AppCompatActivity context) {
        this.context = context;
        this.messages = new ArrayList<>();
    }


    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = context.getLayoutInflater().inflate(R.layout.message_item, parent, false);

        RelativeLayout msg_layout = (RelativeLayout) convertView;
        final Message currentMessage = messages.get(i);

        TextView msgTime, msgContent, msgSender;
        final ImageView img;
        img = (ImageView) msg_layout.findViewById(R.id.pictureInTextView);
        msgTime = (TextView) msg_layout.findViewById(R.id.hour);
        msgContent = (TextView) msg_layout.findViewById(R.id.msgContent);
        msgSender = (TextView) msg_layout.findViewById(R.id.senderName);
        View bubble = msg_layout.findViewById(R.id.msgBubble);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bubble.getLayoutParams();
        //check if the sender is me
        if (currentMessage.getSender().equals(Backendless.UserService.CurrentUser().getProperty("name").toString())) {
            msgSender.setVisibility(View.GONE);
            bubble.setBackgroundResource(R.drawable.blue_bubble_right);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            msgContent.setTextColor(Color.WHITE);
            msgTime.setText(currentMessage.getTime());
            img.setVisibility(View.VISIBLE);
            msgContent.setText("IMAGE");
            //check if it is a picture
            if (pictureFromMyPhone(currentMessage.getContent())) {
                bm = BitmapFactory.decodeFile(currentMessage.getContent());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 6    ;
                try {
                    Bitmap preview_bitmap = BitmapFactory.decodeStream(new FileInputStream(currentMessage.getContent()), null, options);
                    img.setImageBitmap(preview_bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else if(validImageUrl(currentMessage.getContent())){
                img.setVisibility(View.VISIBLE);
                msgContent.setText("IMAGE");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            byte[] s = new HttpRequest(currentMessage.getContent()).prepare(HttpRequest.Method.GET).sendAndReadBytes();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 6;
                            bmp = BitmapFactory.decodeByteArray(s, 0, s.length,options);
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    img.setImageBitmap(bmp);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }else {
                //hide sender text view
                msgContent.setText(currentMessage.getContent());
                img.setVisibility(View.GONE);
                //drag bubble to right
            }
        } else {
            //show sender name
            bubble.setBackgroundResource(R.drawable.orange_bubble_left);
            msgSender.setVisibility(View.VISIBLE);
            msgSender.setText(currentMessage.getSender());
            msgContent.setTextColor(Color.BLACK);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            msgTime.setText(currentMessage.getTime());
            if (validImageUrl(currentMessage.getContent())) {
                img.setVisibility(View.VISIBLE);
                msgContent.setText("IMAGE");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            byte[] s = new HttpRequest(currentMessage.getContent()).prepare(HttpRequest.Method.GET).sendAndReadBytes();
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 6;
                            bmp = BitmapFactory.decodeByteArray(s, 0, s.length,options);
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    img.setImageBitmap(bmp);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                //drag bubble to left
            } else {
                msgContent.setText(currentMessage.getContent());
                img.setVisibility(View.GONE);
            }
        }
        return msg_layout;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    private boolean validImageUrl(String url) {
        String baseUrl = "https://api.backendless.com";
        //example without REGEX
        //return ((url.startsWith(baseUrl) && url.contains(appId)) && url.endsWith(".jpg") || url.endsWith(".png"));
        //example with REGEX
        String validChars = "[a-zA-Z.0-9_/\\-]{1,300}";//lower case OR upper case letters OR . OR digits (0-9) OR / OR - OR _
        return (url.startsWith(baseUrl) && url.endsWith(".jpg") || url.endsWith(".png"));
        /*url.matches("^" + baseUrl + LogInActivity.appID.toLowerCase() + LogInActivity.key.toLowerCase() + validChars + "jpg|png$");*/
    }

    private boolean pictureFromMyPhone(String path) {
        String base = "/storage/emulated/0/Android/data/";
        return (path.startsWith(base) && path.endsWith(".jpg") || path.endsWith(".png"));
    }
}
