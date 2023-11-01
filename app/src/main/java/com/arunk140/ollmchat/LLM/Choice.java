package com.arunk140.ollmchat.LLM;

public class Choice {
    private int index;
    private String finish_reason;
    private Message message;
    @Override
    public String toString() {
        return message.toString();
    }

    public Choice(Message message) {
        this.index = 0;
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}