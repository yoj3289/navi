package com.example.naviproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMediAdapter extends RecyclerView.Adapter<ChatMediAdapter.ViewHolder>{
    private List<ChatMediItem> chatMediList;
    private Context context;

    public ChatMediAdapter(List<ChatMediItem> chatMediList) {
        this.chatMediList = chatMediList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_medi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMediItem chatMediItem = chatMediList.get(position);

        holder.chatMessage.setText(chatMediItem.getChatMessage());

        holder.sender.setText(chatMediItem.getSender());

        if ("MiniBi".equals(chatMediItem.getSender())) {
            holder.line.setVisibility(View.VISIBLE); // line을 보이게 함
        } else {
            holder.line.setVisibility(View.GONE); // 그렇지 않으면 line을 숨김
        }


    }

    @Override
    public int getItemCount() {
        return chatMediList.size();
    }

    public void updateData(List<ChatMediItem> newChatMediList) {
        chatMediList.clear();
        chatMediList.addAll(newChatMediList);
        ChatMediAdapter.this.notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView chatMessage;
        TextView sender;

        View line;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatMessage = itemView.findViewById(R.id.messageTextView);
            sender=itemView.findViewById(R.id.messageSender);
            line=itemView.findViewById(R.id.line);
        }
    }
    public void setData(List<ChatMediItem> newData) {
        this.chatMediList = newData;
        notifyDataSetChanged();
    }
}
