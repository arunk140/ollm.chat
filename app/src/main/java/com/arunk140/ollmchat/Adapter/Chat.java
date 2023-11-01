package com.arunk140.ollmchat.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arunk140.ollmchat.LLM.Message;
import com.arunk140.ollmchat.R;

import java.util.ArrayList;
import java.util.logging.Handler;

public class Chat extends
        RecyclerView.Adapter<Chat.ViewHolder>{
    ArrayList<Message> msgs;

    public Chat(ArrayList<Message> msgList) {
        msgs = msgList;
    }

    @NonNull
    @Override
    public Chat.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View chatBubble;
        if (viewType == 0) {
            chatBubble = inflater.inflate(R.layout.incoming_bubble, parent, false);
        } else if (viewType == 1) {
            chatBubble = inflater.inflate(R.layout.outgoing_bubble, parent, false);
        } else if (viewType == 2) {
            chatBubble = inflater.inflate(R.layout.system_bubble, parent, false);
        }  else {
            chatBubble = inflater.inflate(R.layout.error_bubble, parent, false);
        }
        Log.d("onCreate", "onCreateViewHolder: "+viewType);
        return new ViewHolder(chatBubble);
    }

    @Override
    public void onBindViewHolder(@NonNull Chat.ViewHolder holder, int position) {
        TextView textView = holder.msgText;
        textView.setText(msgs.get(position).getContent());
        Log.d("onbind", "onBindViewHolder: "+msgs.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return msgs.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = msgs.get(position);
        if (message.getRole().equals("error")) {
            return 3;
        }
        if (message.getRole().equals("system")) {
            return 2;
        }
        if (message.getRole().equals("user")) {
            return 1;
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView msgText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            msgText = (TextView) itemView.findViewById(R.id.msgText);
        }
    }
}
