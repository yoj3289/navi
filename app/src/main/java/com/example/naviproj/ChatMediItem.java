package com.example.naviproj;

import java.util.ArrayList;
import java.util.List;

public class ChatMediItem {
    private String chatMessage;

    private String sender;



    public ChatMediItem(String chatMessage,String sender){

        this.chatMessage=chatMessage;
        this.sender=sender;

    }
    public String getChatMessage() {
        return chatMessage;
    }

    public String getSender(){return sender;}



    //체중 이상 감지에 사용될 리사이클러뷰
    public static List<ChatMediItem> getchatMedi() {
        List<ChatMediItem> chatMediList = new ArrayList<>();

        return chatMediList;
    }
}
