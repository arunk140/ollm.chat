package com.arunk140.ollmchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arunk140.ollmchat.Adapter.Chat;
import com.arunk140.ollmchat.Config.Settings;
import com.arunk140.ollmchat.DB.Manager;
import com.arunk140.ollmchat.LLM.ChatCompletionChunk;
import com.arunk140.ollmchat.LLM.ChatCompletionRequest;
import com.arunk140.ollmchat.LLM.GPTViewModel;
import com.arunk140.ollmchat.LLM.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    EditText msgText;
    TextView noMessages;
    boolean disableSending;
    ProgressBar waitingForLLM;
    ImageButton settingsBtn;
    ImageButton refreshBtn;
    RecyclerView chatListView;
    LinearLayoutManager linearLayoutManager;

    ArrayList<Message> messages;
    Chat chatAdapter;
    private Handler mainHandler;

    private GPTViewModel viewModel;
    Manager manager;
    Settings settings;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        disableSending = false;
        messages = new ArrayList<>();
        chatAdapter = new Chat(messages);

        waitingForLLM = findViewById(R.id.waitingForLLM);
        chatListView = findViewById(R.id.chatList);
        msgText = findViewById(R.id.editTextText);
        settingsBtn = findViewById(R.id.settingsBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        noMessages = findViewById(R.id.noMessages);

        manager = new Manager(this);
        manager.open();
        settings = manager.getSettings();
        manager.close();

        settingsBtn.setOnClickListener(v -> {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        refreshBtn.setOnClickListener(v -> {
            restart();
        });

        DividerItemDecoration dId = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        dId.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getApplicationContext(), R.drawable.spacer)));
        chatListView.addItemDecoration(dId);

        chatListView.setAdapter(chatAdapter);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true);
        chatListView.setLayoutManager(linearLayoutManager);

        mainHandler = new Handler(Looper.getMainLooper());
        viewModel = new ViewModelProvider(this).get(GPTViewModel.class);
        viewModel.dataLiveData.observe(this, this::updateTextView);
        viewModel.loadingLiveData.observe(this, this::setLoaderState);
        viewModel.errorLiveData.observe(this, this::checkErrors);

        msgText.setOnKeyListener((v, keyCode, event) -> {
            if (disableSending) {
                return true;
            }
            if (messages.size() > 0) {
                noMessages.setVisibility(View.GONE);
            } else {
                noMessages.setVisibility(View.VISIBLE);
            }
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String inputMsg = msgText.getText().toString().trim();
                if (inputMsg.equals("")) {
                    return true;
                }
                if (inputMsg.equals("clear")) {
                    restart();
                    return true;
                }

                if (messages.size() == 0 && settings.systemPrompt.length() > 0) {
                    messages.add(0, new Message("user", inputMsg));
                    messages.add(1, new Message("system", settings.systemPrompt));
                    chatAdapter.notifyItemRangeInserted(0,2);
                } else {
                    messages.add(0, new Message("user", inputMsg));
                    chatAdapter.notifyItemInserted(0);
                }

                msgText.setText("");
                new Thread(() -> {
                    ChatCompletionRequest request = new ChatCompletionRequest(reorderMessages(messages), settings, true);
                    viewModel.loadData(request, mainHandler, settings, getApplicationContext());
                }).start();
            }
            return true;
        });
    }

    private void updateTextView(ChatCompletionChunk chunk) {
        if (messages.size() > 0 && chunk.getChoices().size() > 0) {
            if (chunk.getChoices().get(0).getDelta().getContent() == null) {
                return;
            }
            String role = "assistant";
            String content = chunk.getChoices().get(0).getDelta().getContent();

            boolean isSameRole = Objects.equals(messages.get(0).getRole(), role);
            if (isSameRole) {
                messages.get(0).appendDelta(content);
                chatAdapter.notifyItemChanged(0);
            } else {
                messages.add(0, new Message(role, content));
                chatAdapter.notifyItemInserted(0);
            }
            chatListView.scrollToPosition(0);
        }
    }
    public ArrayList<Message> reorderMessages(ArrayList<Message> messages) {
        ArrayList<Message> x = (ArrayList<Message>) messages.clone();
        Collections.reverse(x);
        return x;
    }
    private void setLoaderState(boolean loaderState) {
        if (loaderState) {
            waitingForLLM.setVisibility(View.VISIBLE);
//            msgText.setEnabled(false);
            disableSending = true;
        } else {
            waitingForLLM.setVisibility(View.GONE);
//            msgText.setEnabled(true);
            disableSending = false;
        }
    }

    private void checkErrors(Exception e) {
        if (e != null) {
            messages.add(0, new Message("error", e.getMessage()));
            chatAdapter.notifyItemInserted(0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restart() {
        disableSending = false;
        waitingForLLM.setVisibility(View.GONE);
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        manager = new Manager(this);
        manager.open();
        settings = manager.getSettings();
        manager.close();
        noMessages.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        restart();
        super.onResume();
    }
}