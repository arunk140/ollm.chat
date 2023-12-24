package com.arunk140.ollmchat.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arunk140.ollmchat.Adapter.Chat;
import com.arunk140.ollmchat.Config.Settings;
import com.arunk140.ollmchat.DB.Manager;
import com.arunk140.ollmchat.LLM.ChatCompletionChunk;
import com.arunk140.ollmchat.LLM.ChatCompletionRequest;
import com.arunk140.ollmchat.LLM.GPTViewModel;
import com.arunk140.ollmchat.LLM.Message;
import com.arunk140.ollmchat.MainActivity;
import com.arunk140.ollmchat.R;
import com.arunk140.ollmchat.SettingsActivity;
import com.arunk140.ollmchat.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    EditText msgText;
    TextView noMessages;
    boolean disableSending;
    ProgressBar waitingForLLM;
    ImageButton settingsBtn;
    ImageButton refreshBtn;
    ImageButton drawerBtn;
    ImageButton actionBtn;
    Button regenBtn;
    RecyclerView chatListView;
    LinearLayoutManager linearLayoutManager;
    ArrayList<Message> messages;
    Chat chatAdapter;
    private Handler mainHandler;
    private GPTViewModel viewModel;
    Manager manager;
    Settings settings;
    Thread currentInvocation;
    Runnable currentRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        disableSending = false;
        messages = new ArrayList<>();
        chatAdapter = new Chat(messages);

        waitingForLLM = binding.waitingForLLM;
        chatListView = binding.chatList;
        msgText = binding.editTextText;
        settingsBtn = binding.settingsBtn;
        refreshBtn = binding.refreshBtn;
        drawerBtn = binding.drawerBtn;
        noMessages = binding.noMessages;
        actionBtn = binding.stopBtn;
        regenBtn = binding.regenBtn;

        manager = new Manager(getContext());
        manager.open();
        settings = manager.getSettings();
        manager.close();


        settingsBtn.setOnClickListener(v -> {
            Intent myIntent = new Intent(requireActivity(), SettingsActivity.class);
            requireActivity().startActivity(myIntent);
        });

        drawerBtn.setOnClickListener(v -> {
            MainActivity x = (MainActivity)getActivity();
            x.toggleDrawer();
        });

        refreshBtn.setOnClickListener(v -> {
            restart();
        });

        DividerItemDecoration dId = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
        dId.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(requireActivity(), R.drawable.spacer)));
        chatListView.addItemDecoration(dId);

        chatListView.setAdapter(chatAdapter);
        linearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true);
        chatListView.setLayoutManager(linearLayoutManager);

        mainHandler = new Handler(Looper.getMainLooper());
        viewModel = new ViewModelProvider(this).get(GPTViewModel.class);
        viewModel.dataLiveData.observe(requireActivity(), this::updateTextView);
        viewModel.loadingLiveData.observe(requireActivity(), this::setLoaderState);
        viewModel.errorLiveData.observe(requireActivity(), this::checkErrors);

        actionBtn.setOnClickListener(v -> {
            if (currentInvocation != null) {
                currentInvocation.interrupt(); // Interrupt the thread
            }
        });

        regenBtn.setOnClickListener(v -> {
            regenBtn.setVisibility(View.GONE);
            if (messages.size() > 1) {
                messages.remove(0);
                chatAdapter.notifyItemRemoved(0);
                sendToLLM(messages.get(0).getContent(), false);
            }
        });

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
                sendToLLM(inputMsg, true);
            }
            return true;
        });
//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void sendToLLM(String inputMsg, boolean addNewMessage) {
        if (inputMsg.equals("")) {
            return;
        }
        if (inputMsg.equals("clear")) {
            restart();
            return;
        }

        if (addNewMessage) {
            if (messages.size() == 0 && settings.systemPrompt.length() > 0) {
                messages.add(0, new Message("user", inputMsg));
                messages.add(1, new Message("system", settings.systemPrompt));
                chatAdapter.notifyItemRangeInserted(0,2);
            } else {
                messages.add(0, new Message("user", inputMsg));
                chatAdapter.notifyItemInserted(0);
            }
        }

        msgText.setText("");
        currentRunnable = () -> {
            if (Thread.interrupted()) {
                return;
            }
            ChatCompletionRequest request = new ChatCompletionRequest(reorderMessages(messages), settings, true);
            viewModel.loadData(request, mainHandler, settings, getActivity());
        };
        currentInvocation = new Thread(currentRunnable);
        currentInvocation.start();
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
            actionBtn.setVisibility(View.VISIBLE);
//            msgText.setEnabled(false);
            disableSending = true;
            regenBtn.setVisibility(View.GONE);
        } else {
            waitingForLLM.setVisibility(View.GONE);
            actionBtn.setVisibility(View.GONE);
//            msgText.setEnabled(true);
            disableSending = false;
            if (!Objects.equals(messages.get(0).getRole(), "user")) {
                regenBtn.setVisibility(View.VISIBLE);
            } else {
                regenBtn.setVisibility(View.GONE);
            }
        }
    }

    private void checkErrors(Exception e) {
        if (e != null) {
            messages.add(0, new Message("error", e.getMessage()));
            chatAdapter.notifyItemInserted(0);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void restart() {
        disableSending = false;
        waitingForLLM.setVisibility(View.GONE);
        regenBtn.setVisibility(View.GONE);
        messages.clear();
        chatAdapter.notifyDataSetChanged();
        manager = new Manager(getContext());
        manager.open();
        settings = manager.getSettings();
        manager.close();
        noMessages.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}