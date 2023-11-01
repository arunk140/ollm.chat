package com.arunk140.ollmchat.LLM;

public class ChunkDelta {
    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "ChunkDelta{" +
                "role='" + role + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
