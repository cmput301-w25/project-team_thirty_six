package com.example.androidproject;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Activity that facilitates communication with a chatbot powered by OpenAI via Azure.
 * Provides a user interface for sending messages and receiving responses from the chatbot.
 */
public class ChatBotActivity extends AppCompatActivity {
    private OpenAIClientHelper openAIHelper;
    private EditText input;
    private Button sendButton;
    private TextView output;
    private ProgressBar loadingIndicator;
    private String currentUser;

    /**
     * Called when the activity is first created.
     * Initializes the UI components, sets up the OpenAI helper, and handles button actions.
     *
     * @param savedInstanceState The saved instance state of the activity, if any.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        ImageView backButton = findViewById(R.id.backButton);

        // Retrieve the currentUser from the Intent
        currentUser = (String) getIntent().getSerializableExtra("currentUser");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Initialize with Azure OpenAI credentials
        openAIHelper = new OpenAIClientHelper(
                "https://models.inference.ai.azure.com", // Azure OpenAI endpoint
                "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", // Azure Open AI API token
                currentUser
        );

        // Initialize views
        input = findViewById(R.id.user_input);
        sendButton = findViewById(R.id.send_button);
        output = findViewById(R.id.response_output);
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Set a hint to guide users
        input.setHint("Ask me anything...");

        sendButton.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                // Show loading indicator and disable input
                loadingIndicator.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                input.setEnabled(false);

                // Clear input immediately
                input.setText("");

                openAIHelper.getChatCompletion(message, new OpenAIClientHelper.OpenAICallback() {
                    /**
                     * Handles receiving the response from the ai
                     * @param response
                     *   the string response provided
                     */
                    @Override
                    public void onSuccess(String response) {
                        output.setText(response.replaceAll("[*#]", ""));
                        // Hide loading indicator and re-enable input
                        loadingIndicator.setVisibility(View.GONE);
                        sendButton.setEnabled(true);
                        input.setEnabled(true);
                    }

                    /**
                     * Handles receiving an error from the ai
                     * @param error
                     *   the error message
                     */
                    @Override
                    public void onError(String error) {
                        output.setText("Error: " + error);
                        // Hide loading indicator and re-enable input
                        loadingIndicator.setVisibility(View.GONE);
                        sendButton.setEnabled(true);
                        input.setEnabled(true);
                    }
                });
            }
        });
    }

    /**
     * Called when the activity is destroyed.
     * This method is responsible for cleaning up resources and shutting down the OpenAI helper.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        openAIHelper.shutdown();
    }
}
