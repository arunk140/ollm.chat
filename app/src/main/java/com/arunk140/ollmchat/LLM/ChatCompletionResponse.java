package com.arunk140.ollmchat.LLM;

import java.util.ArrayList;
import java.util.List;

public class ChatCompletionResponse {
    private String id;
    private ArrayList<Choice> choices;

    public ChatCompletionResponse(String id, String role, String content) {
        this.id = id;
        this.choices = new ArrayList<>();
        choices.add(
                new Choice(new Message(role, content))
        );
    }

    @Override
    public String toString() {
        return "ChatCompletionResponse{" +
                "id='" + id + '\'' +
                ", choices=" + choices +
                '}';
    }

    public ArrayList<Choice> getChoices() {
        return choices;
    }
}
