    package com.example.emergencyapp.service;

    import android.app.NotificationChannel;
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.content.Context;
    import android.content.Intent;
    import android.media.RingtoneManager;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.os.Build;
    import androidx.core.app.NotificationCompat;

    import com.example.emergencyapp.R;
    import com.example.emergencyapp.ui.ChatActivity;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.messaging.FirebaseMessagingService;
    import com.google.firebase.messaging.RemoteMessage;

    public class MyFirebaseMessagingService extends FirebaseMessagingService {

        private static final String CHANNEL_ID = "emergency_alerts";

        @Override
        public void onNewToken(String token) {
            super.onNewToken(token);
            String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;

            if (uid != null) {
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("fcmToken")
                        .setValue(token);
            }
        }

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            String title = "Новое сообщение";
            String body = "Экстренная ситуация";

            String type = null;
            String patientUid = null;

            if (remoteMessage.getData().size() > 0) {
                title = remoteMessage.getData().get("title");
                body = remoteMessage.getData().get("body");
                type = remoteMessage.getData().get("type"); // например: "emergency", "chat", "confirmation"
                patientUid = remoteMessage.getData().get("patientUid");
            }

            sendNotification(title, body, type, patientUid);
        }

        private void sendNotification(String title, String messageBody, String type, String patientUid) {
            Intent intent;

            if ("medicalCard".equals(type) && patientUid != null) {
                intent = new Intent(this, com.example.emergencyapp.ui.MedicalCardActivity.class);
                intent.putExtra("patientUid", patientUid);
            } else if ("confirmation".equals(type)) {
                intent = new Intent(this, com.example.emergencyapp.ui.ConfirmationsActivity.class);
            } else {
                intent = new Intent(this, com.example.emergencyapp.ui.ChatActivity.class);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Экстренные оповещения",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Канал для экстренных уведомлений");
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);

            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }


        // Пример проверки сети: если нет интернета, можно тут инициировать отправку SMS
        private boolean isNetworkAvailable() {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }
