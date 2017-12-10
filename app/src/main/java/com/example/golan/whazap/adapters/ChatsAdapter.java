package com.example.golan.whazap.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by golan on 18/06/2017.
 */

public class ChatsAdapter extends BaseAdapter {
    private final List<String> chats;
    private final Context context;


    public ChatsAdapter(Context context, List<String> chat) {
        this.context = context;
        this.chats = chat;
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public String getItem(int i) {
        return chats.get(i);
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

        return recycledView;
    }

    public void addChat(String chat) {
        this.chats.add(chat);
        notifyDataSetChanged();
    }
}