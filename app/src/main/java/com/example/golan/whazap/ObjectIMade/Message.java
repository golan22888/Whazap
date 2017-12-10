package com.example.golan.whazap.ObjectIMade;

public class Message {
    private String time;
    private String sender;
    private String content;

    public Message(String time, String sender, String content){
        this.time = time;
        this.sender = sender;
        this.content = content;
    }
    public String getTime(){
        return time;
    }
    public String getSender(){
    return sender;
    }
    public String getContent(){
        return content;
    }

}
