package com.example.datemaker.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.datemaker.adapter.MessageAdapter;
import com.example.datemaker.databinding.ActivityChatBinding;
import com.example.datemaker.model.Message;
import com.example.datemaker.model.SendMessageRequest;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private ApiService apiService;
    private long partnerId;
    private long myUserId;
    private MessageAdapter messageAdapter;
    private static ChatActivity currentInstance;
    private static long currentPartnerId = -1L;

    public static boolean isActiveWithPartner(long partnerId) {
        return currentInstance != null && currentPartnerId == partnerId;
    }

    public static void onIncomingMessage(com.example.datemaker.model.Message msg) {
        if (currentInstance == null) return;

        currentInstance.runOnUiThread(() -> {
            currentInstance.messageAdapter.addMessage(msg);
            currentInstance.binding.rvMessages.scrollToPosition(
                    currentInstance.messageAdapter.getItemCount() - 1
            );
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserSessionManager session = new UserSessionManager(this);
        myUserId = session.getUserId();

        partnerId = getIntent().getLongExtra("partnerId", -1L);
        String partnerName = getIntent().getStringExtra("partnerName");

        if (partnerId == -1L) {
            Toast.makeText(this, "Invalid partner", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvChatTitle.setText(partnerName != null ? partnerName : "Chat");

        apiService = RetrofitClient.getRetrofitInstance(ChatActivity.this).create(ApiService.class);

        messageAdapter = new MessageAdapter(myUserId);
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessages.setAdapter(messageAdapter);

        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            sendMessage(text);
        });

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
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

        loadMessages();
    }

    private void loadMessages() {
        apiService.getMessages(partnerId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ChatActivity.this, "Failed to load messages (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                messageAdapter.setItems(response.body());
                binding.rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String text) {
        SendMessageRequest request = new SendMessageRequest(partnerId, text);

        apiService.sendMessage(request).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    return;
                }

                Message sent = response.body();
                messageAdapter.addMessage(sent);
                binding.etMessage.setText("");
                binding.rvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentInstance = this;
        currentPartnerId = partnerId;
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentInstance = null;
        currentPartnerId = -1L;
    }
}