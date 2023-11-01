package com.arunk140.ollmchat.LLM;

import com.arunk140.ollmchat.Config.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatCompletionRequest {
    private ArrayList<Message> messages;
    private double temperature;
    private int max_tokens;
    private boolean stream;
    private String model;

    public ArrayList<Message> filterMessages() {
        this.messages.removeIf((d) -> Objects.equals(d.getRole(), "error"));
        return this.messages;
    }
    public ChatCompletionRequest(ArrayList<Message> messages, Settings config, boolean stream) {
        this.messages = messages;
//
        this.temperature = config.temperature;
        this.max_tokens = config.maxTokens;
        this.model = config.model;

        this.stream = stream;
    }
}
