package com.example.androidproject;

import static android.content.ContentValues.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Helper class for interacting with OpenAI's API.
 * Handles asynchronous requests for chat completion and mood analysis.
 */
public class OpenAIClientHelper {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String apiKey;
    private final String endpoint;
    private final OkHttpClient client;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ArrayList<MoodState> moodHistory;
    private MoodHistoryManager moodHistoryManager;
    private String currentUser;

    /**
     * Constructor for OpenAIClientHelper.
     *
     * @param endpoint API endpoint for OpenAI.
     * @param apiKey   API key for authentication.
     * @param user     The current user for mood history retrieval.
     */

    public OpenAIClientHelper(String endpoint, String apiKey, String user) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
        this.moodHistory = new ArrayList<>();
        this.moodHistoryManager = new MoodHistoryManager();
        this.currentUser = user;
        fetchMoodHistory();
    }
    /**
     * Interface for handling OpenAI API responses.
     */

    public interface OpenAICallback {
        void onSuccess(String response);
        void onError(String error);
    }


    /**
     * Fetches the user's mood history asynchronously and updates the local cache.
     */
    private void fetchMoodHistory() {
        if (currentUser != null) {
            moodHistoryManager.fetchMoodHistory(currentUser, new MoodHistoryManager.MoodHistoryCallback() {
                @Override
                public void onCallback(ArrayList<MoodState> retrievedMoods) {
                    if (retrievedMoods != null) {
                        moodHistory.clear();
                        moodHistory.addAll(retrievedMoods);
                        Log.d(TAG, "Fetched " + moodHistory.size() + " moods for analysis.");
                    } else {
                        Log.e(TAG, "No moods found for user: " + currentUser);
                    }
                }
            });
        }
    }
    /**
     * Sends a request to OpenAI's API for chat completion, incorporating the user's mood history.
     *
     * @param userMessage The message sent by the user.
     * @param callback    Callback to handle API response.
     */

    public void getChatCompletion(String userMessage, OpenAICallback callback) {
        executor.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-4o");

                JSONArray messages = new JSONArray();
                
                // Create a system message that includes mood history context
                StringBuilder systemMessage = new StringBuilder();
                systemMessage.append("You are a helpful AI therapist and mood analysis tool. ");
                systemMessage.append("You have access to the user's mood history to provide better context and support. ");
                systemMessage.append("The user's mood history is as follows:\n\n");
                
                if (moodHistory != null && !moodHistory.isEmpty()) {
                    for (MoodState mood : moodHistory) {
                        systemMessage.append("{Date: ").append(mood.formatDateTime())
                                  .append(", Mood: ").append(mood.getMood());
                        if (mood.getReason() != null && !mood.getReason().isEmpty()) {
                            systemMessage.append(", Reason: ").append(mood.getReason());
                        }
                        systemMessage.append("\n}");
                    }
                } else {
                    systemMessage.append("No mood history available yet.\n");
                }
                
                systemMessage.append("\nPlease analyze the user's mood patterns and provide supportive, therapeutic responses.");
                
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", systemMessage.toString()));
                
                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content", userMessage));
                
                requestBody.put("messages", messages);
                requestBody.put("max_tokens", 500);

                Request request = new Request.Builder()
                        .url(endpoint + "/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-15-preview")
                        .addHeader("api-key", apiKey)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "No error details";
                        throw new IOException("Unexpected response code: " + response.code() + "\nDetails: " + errorBody);
                    }

                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String result = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    handler.post(() -> callback.onSuccess(result));
                }
            } catch (Exception e) {
                Log.e("Azure OpenAI", "Error: " + e.getMessage());
                handler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    /**
     * Shuts down the executor service to free up resources.
     */
    public void shutdown() {
        executor.shutdown();
    }
}