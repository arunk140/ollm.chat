package com.arunk140.ollmchat.LLM;

import java.util.ArrayList;

public class ChatCompletionChunk {
    private String id;
    private String object;
    private long created;

    public ArrayList<ChunkChoice> getChoices() {
        return choices;
    }

    private String model;
    private ArrayList<ChunkChoice> choices;

    @Override
    public String toString() {
        return "ChatCompletionChunk{" +
                "id='" + id + '\'' +
                ", object='" + object + '\'' +
                ", created=" + created +
                ", model='" + model + '\'' +
                ", choices=" + choices +
                '}';
    }
}
