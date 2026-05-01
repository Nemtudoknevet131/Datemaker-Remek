package com.example.datemaker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.adapter.MessageAdapter;
import com.example.datemaker.model.AiDateRequest;
import com.example.datemaker.model.AiDateResponse;
import com.example.datemaker.model.Message;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiDateChatActivity extends AppCompatActivity {

    private RecyclerView rvAiChat;
    private EditText etAiMessage;
    private Button btnAiSend;
    private MessageAdapter adapter;
    private long currentUserId;
    private final long AI_BOT_ID = 0L;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_date_chat);

        UserSessionManager session = new UserSessionManager(this);
        currentUserId = session.getUserId();

        rvAiChat = findViewById(R.id.rvAiChat);
        etAiMessage = findViewById(R.id.etAiMessage);
        btnAiSend = findViewById(R.id.btnAiSend);

        setupRecyclerView();

        apiService = RetrofitClient.getRetrofitInstance(AiDateChatActivity.this).create(ApiService.class);

        postAiMessage("Hello! I'm your AI Date Planner. Tell me what kind of date you're looking for (e.g., romantic, adventurous, cozy) and I'll give you some ideas!");

        btnAiSend.setOnClickListener(v -> sendMessage());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        View rootView = getWindow().getDecorView();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(currentUserId);
        rvAiChat.setLayoutManager(new LinearLayoutManager(this));
        rvAiChat.setAdapter(adapter);
    }

    private void sendMessage() {
        String text = etAiMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        Message userMsg = new Message();
        userMsg.setSenderId(currentUserId);
        userMsg.setContent(text);
        adapter.addMessage(userMsg);
        rvAiChat.scrollToPosition(adapter.getItemCount() - 1);

        etAiMessage.setText("");

        Message typingMsg = new Message();
        typingMsg.setSenderId(AI_BOT_ID);
        typingMsg.setContent("Typing...");
        adapter.addMessage(typingMsg);
        int typingPosition = adapter.getItemCount() - 1;
        rvAiChat.scrollToPosition(typingPosition);

        AiDateRequest request = new AiDateRequest(currentUserId, text);

        apiService.getAiDateIdea(request).enqueue(new Callback<AiDateResponse>() {
            @Override
            public void onResponse(Call<AiDateResponse> call, Response<AiDateResponse> response) {
                if (typingPosition >= 0 && typingPosition < adapter.getItemCount()) {
                    adapter.removeAt(typingPosition);
                }

                if (response.isSuccessful() && response.body() != null) {
                    postAiMessage(response.body().getReply());
                } else {
                    postAiMessage("Oops, retardation, demolition");
                }
            }

            @Override
            public void onFailure(Call<AiDateResponse> call, Throwable t) {
                if (typingPosition >= 0 && typingPosition < adapter.getItemCount()) {
                    adapter.removeAt(typingPosition);
                }
                postAiMessage("I had trouble connecting to the server. Please check your internet and try again.");
            }
        });
    }

    private void postAiMessage(String text) {
        Message aiMsg = new Message();
        aiMsg.setSenderId(AI_BOT_ID);
        aiMsg.setContent(text);
        adapter.addMessage(aiMsg);
        rvAiChat.scrollToPosition(adapter.getItemCount() - 1);
    }
}
