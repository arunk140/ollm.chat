package com.arunk140.ollmchat.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.arunk140.ollmchat.Config.Settings;

public class Manager {
    private Helper dbHelper;
    private Context context;
    private SQLiteDatabase database;
    public Manager(Context c) {
        context = c;
    }

    public Manager open() throws SQLException {
        dbHelper = new Helper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void setConfigParam(String key, String value) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(Helper.configKeyCol, key);
        contentValue.put(Helper.configValueCol, value);
        database.replace(Helper.configTableName, null, contentValue);
    }
    @SuppressLint("Range")
    public String getConfigParam(String key) {
        String selection = Helper.configKeyCol + "=?";
        String[] selectionArgs = { String.valueOf(key) };
        Cursor cursor = database.query(Helper.configTableName, null, selection, selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(Helper.configValueCol));
        } else {
            return null;
        }
    }

    public Settings getSettings() {
        Settings settings = new Settings();
        String apiKey = getConfigParam("api_key");
        int maxTokens = -1; // Default value
        String apiUrl = getConfigParam("api_url");
        String systemPrompt = getConfigParam("system_prompt");
        String model = getConfigParam("model");
        float temperature = 0.7F; // Default value
        try {
            String maxTdb = getConfigParam("max_tokens");
            if (maxTdb != null) {
                maxTokens = Integer.parseInt(getConfigParam("max_tokens"));
            }
        } catch (NumberFormatException e) {
            // Handle invalid maxTokens value
        }
        try {
            String tempDb = getConfigParam("temperature");
            if (tempDb != null) {
                temperature = Float.parseFloat(getConfigParam("temperature"));
            }
        } catch (NumberFormatException e) {
            // Handle invalid temperature value
        }
        if (apiKey != null && !apiKey.isEmpty()) {
            settings.apiKey = apiKey;
        }
        if (model != null && !model.isEmpty()) {
            settings.model = model;
        }
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            settings.systemPrompt = systemPrompt;
        }
        if (maxTokens > 0) {
            settings.maxTokens = maxTokens;
        }
        if (apiUrl != null && !apiUrl.isEmpty()) {
            if (!apiUrl.endsWith("/")) {
                apiUrl += "/";
            }
            settings.apiUrl = apiUrl;
        }
        if (temperature >= 0.0 && temperature <= 1.0) {
            settings.temperature = temperature;
        }
        return settings;
    }

}
