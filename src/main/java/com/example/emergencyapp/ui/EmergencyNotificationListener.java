package com.example.emergencyapp.ui;

import android.app.*;
import android.content.*;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class EmergencyNotificationListener {

    private final Context context;
    private final DatabaseReference messagesRef;
    private final String currentUid;

    public EmergencyNotificationListener(Context context) {
        this.context = context;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.messagesRef = FirebaseDatabase.getInstance().getReference("messages");
    }

    public void start() {
        messagesRef.orderByChild("emergency").equalTo(true)
                .addChildEventListener(new ChildEventListener() {
                    @Override public void onChildAdded(@NonNull DataSnapshot snapshot, String prev) {
                        Message msg = snapshot.getValue(Message.class);
                        if (msg != null && "new".equals(msg.status) && !msg.senderId.equals(currentUid)) {
                            showEmergencyNotification(msg);
                        }
                    }
                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, String prev) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String prev) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void showEmergencyNotification(Message msg) {
        String channelId = "emergency_channel";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(channelId) == null) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Экстренные сообщения", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("🚨 ЭКСТРЕННАЯ СИТУАЦИЯ")
                .setContentText(msg.senderName + ": " + msg.text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        manager.notify((int) System.currentTimeMillis(), notification);
    }
}
