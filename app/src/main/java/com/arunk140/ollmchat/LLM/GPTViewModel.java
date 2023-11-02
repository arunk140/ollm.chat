package com.arunk140.ollmchat.LLM;

import android.content.Context;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arunk140.ollmchat.Config.Settings;
import com.arunk140.ollmchat.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GPTViewModel extends ViewModel {
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    public MutableLiveData<Exception> errorLiveData = new MutableLiveData<>();
    public MutableLiveData<ChatCompletionChunk> dataLiveData = new MutableLiveData<>();
    StringBuffer response;
    Handler hn;
    public void loadData(ChatCompletionRequest requestBody, Handler hn, Settings config, Context c) {
        if (Thread.interrupted()) {
            hn.post(() -> loadingLiveData.setValue(false));
            return; // Exit the method if the thread is interrupted
        }
        hn.post(() -> loadingLiveData.setValue(true));
        hn.post(() -> errorLiveData.setValue(null));
        response = new StringBuffer();
        this.hn = hn;
        try {
            if (config.apiUrl.equals(c.getString(R.string.defaultApiUrl)) && config.apiKey.length() < 1) {
                throw new Exception("Error message: " + "Set API Key in Settings");
            }
            if (config.apiUrl.equals(c.getString(R.string.defaultApiUrl)) && config.maxTokens < 1) {
                throw new Exception("Error message: " + "Increase the Max Tokens in Settings");
            }
            URL url = new URL(config.apiUrl + "v1/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Content-Type", "application/json");
            if (config.apiKey.length() > 0) {
                connection.setRequestProperty("Authorization", "Bearer " + config.apiKey);
            }

            requestBody.filterMessages();

            Gson gson = new Gson();
            String json = gson.toJson(requestBody);

            connection.setDoOutput(true);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(json.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                // Handle the error response (e.g., connection.getErrorStream())
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        String errorMessage = errorResponse.toString();
                        throw new Exception("Error message: " + errorMessage);

                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new Exception("Error message: " + e.getMessage());
                    }
                } else {
                    System.err.println("No error stream available for the response.");
                    throw new Exception("Error message: " + "Unknown Error");
                }
            }

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("data: [DONE]")) {
                    hn.post(() -> loadingLiveData.setValue(false));
                    hn.post(() -> errorLiveData.setValue(null));
                } else if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    ChatCompletionChunk chatResponse = new Gson().fromJson(jsonData, ChatCompletionChunk.class);
                    hn.post(() -> dataLiveData.postValue(chatResponse));
                    hn.post(() -> errorLiveData.setValue(null));
                }
                if (Thread.interrupted()) {
                    reader.close();
                    connection.disconnect();
                    hn.post(() -> loadingLiveData.setValue(false));
                    return;
                }
            }
            if (Thread.interrupted()) {
                reader.close();
                connection.disconnect();
                hn.post(() -> loadingLiveData.setValue(false));
                return;
            }
            reader.close();
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            hn.post(() -> errorLiveData.setValue(e));
        }
        hn.post(() -> loadingLiveData.setValue(false));
    }
}
