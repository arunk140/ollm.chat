package com.arunk140.ollmchat;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.arunk140.ollmchat.Config.Settings;
import com.arunk140.ollmchat.DB.Manager;

public class SettingsActivity extends AppCompatActivity {
    private EditText apiKeyEditText;
    private EditText maxTokensEditText;
    private EditText modelEditText;
    private EditText apiUrlEditText;
    private EditText temperatureEditText;

    private EditText systemPromptMultiLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        apiKeyEditText = findViewById(R.id.apiKey);
        modelEditText = findViewById(R.id.model);
        maxTokensEditText = findViewById(R.id.maxTokens);
        apiUrlEditText = findViewById(R.id.apiUrl);
        temperatureEditText = findViewById(R.id.temperature);
        systemPromptMultiLine = findViewById(R.id.systemPrompt);

        Manager manager = new Manager(this);
        manager.open();
        Settings settings = manager.getSettings();
        manager.close();

        apiKeyEditText.setText(settings.apiKey);
        maxTokensEditText.setText(String.valueOf(settings.maxTokens));
        apiUrlEditText.setText(settings.apiUrl);
        modelEditText.setText(settings.model);
        systemPromptMultiLine.setText(settings.systemPrompt);
        temperatureEditText.setText(String.valueOf(settings.temperature));

        addTextWatcher(apiKeyEditText, "api_key");
        addTextWatcher(maxTokensEditText, "max_tokens");
        addTextWatcher(apiUrlEditText, "api_url");
        addTextWatcher(modelEditText, "model");
        addTextWatcher(temperatureEditText, "temperature");
        addTextWatcher(systemPromptMultiLine, "system_prompt");
    }

    private void addTextWatcher(EditText editText, final String key) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveValueToDB(key, editText.getText().toString());
            }
        });
    }

    private void saveValueToDB(String key, String value) {
        // Save the key-value pair to the database
        Manager manager = new Manager(this);
        manager.open();
        manager.setConfigParam(key, value);
        manager.close();
    }
}
