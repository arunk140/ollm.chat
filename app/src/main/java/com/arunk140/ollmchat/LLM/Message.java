package com.arunk140.ollmchat.LLM;

import android.util.Log;

public class Message {
    private final String role;

    public String getRole() {
        return role;
    }

    private String content;

    public String getContent() {
        return content;
    }
    public void appendDelta(String delta) {
        this.content = this.content + delta;
        Log.d("appending", "appendDelta: "+delta);
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
