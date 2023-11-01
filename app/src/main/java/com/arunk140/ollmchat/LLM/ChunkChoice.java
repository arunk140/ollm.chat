package com.arunk140.ollmchat.LLM;

public class ChunkChoice {
    private int index;
    private ChunkDelta delta;
    private String finish_reason;

    public ChunkDelta getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        return "ChunkChoice{" +
                "index=" + index +
                ", delta=" + delta +
                ", finish_reason='" + finish_reason + '\'' +
                '}';
    }
}
