package com.example.emergencyapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class PatientActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1001;

    private Button buttonMyCard;
    private Button buttonPanic;
    private EditText editTextDescription;

    private DatabaseReference usersRef;
    private DatabaseReference messagesRef;
    private String uid;
    private String email;
    private String currentName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        // Найти View
        buttonMyCard        = findViewById(R.id.buttonMyCard);
        buttonPanic         = findViewById(R.id.buttonPanic);

        // Изначально выключаем кнопку паники, пока не загрузим имя
        buttonPanic.setEnabled(false);

        // Инициализируем Firebase
        uid         = FirebaseAuth.getInstance().getCurrentUser().getUid();
        email       = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        usersRef    = FirebaseDatabase.getInstance().getReference("users").child(uid);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Загрузка имени пользователя для отображения в сообщениях/SMS
        usersRef.child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String n = snap.getValue(String.class);
                        currentName = (n != null && !n.isEmpty()) ? n : email;
                        buttonPanic.setEnabled(true);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        currentName = email;
                        buttonPanic.setEnabled(true);
                    }
                });

        // Обработчик кнопки "Моя медкарта" (режим просмотра)
        buttonMyCard.setOnClickListener(v -> {
            Intent i = new Intent(PatientActivity.this, MedicalCardActivity.class);
            i.putExtra("mode", "view");
            i.putExtra("userId", uid);
            startActivity(i);
        });

        // Обработчик паники
        buttonPanic.setOnClickListener(v -> {
            Intent intent = new Intent(PatientActivity.this, EmergencyFormActivity.class);
            intent.putExtra("senderRole", "patient");
            intent.putExtra("userId", uid); // чтобы автоматически прикреплять карту
            startActivity(intent);
        });
    }

    private void sendEmergency(String desc) {
        if (isNetworkAvailable()) {
            // Отправляем в Firebase
            String key = messagesRef.push().getKey();
            if (key == null) {
                Toast.makeText(this, "Не удалось отправить сообщение", Toast.LENGTH_SHORT).show();
                return;
            }
            Message m = new Message(
                    key,
                    uid,
                    email,
                    currentName,
                    desc,
                    System.currentTimeMillis(),
                    true
            );
            messagesRef.child(key).setValue(m)
                    .addOnSuccessListener(a ->
                            Toast.makeText(this, "Сигнал отправлен", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Ошибка при отправке: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Fallback на SMS
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE
                );
            } else {
                sendSmsFallback(desc);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private void sendSmsFallback(String text) {
        String doctorPhone = "+375293361695"; // TODO: заменить на реальный номер из БД
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(
                doctorPhone,
                null,
                "ЭКСТРЕННО от " + currentName + ": " + text,
                null,
                null
        );
        Toast.makeText(this, "SMS-альтернативa отправлена", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Повторная отправка SMS после получения разрешения
            sendSmsFallback(editTextDescription.getText().toString().trim());
        } else {
            Toast.makeText(this,
                    "Для SMS-альтернативы необходимо разрешение SEND_SMS",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
