package com.example.golan.whazap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.backendless.Backendless;

import java.util.LinkedList;

/**
 * Created by golan on 18/06/2017.
 */

public class UsersListAdapter extends BaseAdapter {
    private final LinkedList<String> chat;
    private final Context context;
    String myName = Backendless.UserService.CurrentUser().getProperty("name").toString();


    public UsersListAdapter(Context context, LinkedList<String> chat) {
        this.context = context;
        this.chat = chat;
    }

    @Override
    public int getCount() {
        return chat.size();
    }

    @Override
    public String getItem(int i) {
        return chat.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View recycledView, ViewGroup parent) {

        if (recycledView == null) recycledView = new TextView(context);
        TextView tv = ((TextView) recycledView);
        tv.setText(getItem(i));
        tv.setTextSize(30);
        tv.setTextColor(Color.WHITE);
        //tv.setBackgroundResource(R.drawable.chat_bubble);
        tv.setGravity(Gravity.NO_GRAVITY);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);


        return recycledView;
    }
}
