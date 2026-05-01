package com.example.datemaker.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.datemaker.R;
import com.example.datemaker.activity.ChatActivity;
import com.example.datemaker.activity.MainActivity;
import com.example.datemaker.model.Message;
import com.example.datemaker.retrofit.ApiService;
import com.example.datemaker.retrofit.RetrofitClient;
import com.example.datemaker.utils.UserSessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFireBaseService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "partner_requests_channel";
    private static final String CHAT_CHANNEL_ID = "chat_messages_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        UserSessionManager session = new UserSessionManager(this);
        long userId = session.getUserId();

        if (userId != -1) {
            ApiService apiService = RetrofitClient.getRetrofitInstance(getApplicationContext()).create(ApiService.class);
            apiService.sendFcmToken(token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // ok
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // ignore
                }
            });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String type = data != null ? data.get("type") : null;

        if ("CHAT".equals(type)) {
            handleChatMessage(data);
            return;
        }

        String title = "New partner request";
        String body = "Someone sent you a partner request";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Partner requests",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_outline)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(1001, builder.build());
    }

    // Handle incoming chat messages (real-time update or notification)
    private void handleChatMessage(Map<String, String> data) {
        if (data == null) return;

        String content = data.get("content");

        long senderId;
        long receiverId;
        long messageId;

        try {
            senderId = Long.parseLong(data.get("senderId"));
            receiverId = Long.parseLong(data.get("receiverId"));
            messageId = Long.parseLong(data.get("messageId"));
        } catch (Exception e) {
            showNotification("New message", content != null ? content : "You received a new message");
            return;
        }

        // If chat with this partner is open, update it live
        if (ChatActivity.isActiveWithPartner(senderId)) {
            Message msg = new Message();
            msg.setId(messageId);
            msg.setSenderId(senderId);
            msg.setReceiverId(receiverId);
            msg.setContent(content);

            ChatActivity.onIncomingMessage(msg);
        } else {
            showChatNotification(senderId, content);
        }
    }

    private void showChatNotification(long partnerId, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHAT_CHANNEL_ID,
                    "Chat messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("partnerId", partnerId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) partnerId,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHAT_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_outline)
                .setContentTitle("New message")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (content != null && !content.isEmpty()) {
            builder.setContentText(content);
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}