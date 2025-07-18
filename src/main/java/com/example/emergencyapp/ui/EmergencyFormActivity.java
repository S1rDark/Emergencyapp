package com.example.emergencyapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class EmergencyFormActivity extends AppCompatActivity {

    private Spinner spinnerReason;
    private EditText editTextDetails;
    private Button buttonSend;

    private DatabaseReference messagesRef, usersRef;
    private String currentUid, currentEmail, currentName, attachedPatientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_form);

        spinnerReason     = findViewById(R.id.spinnerReason);
        editTextDetails   = findViewById(R.id.editTextDetails);
        buttonSend        = findViewById(R.id.buttonSend);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        usersRef    = FirebaseDatabase.getInstance().getReference("users");

        currentUid   = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        attachedPatientId = getIntent().getStringExtra("userId");

        // Предзаполнение списка причин
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList("Травма", "Потеря сознания", "Боль", "Другое")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReason.setAdapter(adapter);

        // Получаем имя для подписи
        usersRef.child(currentUid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        currentName = (name != null && !name.isEmpty()) ? name : currentEmail;
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        currentName = currentEmail;
                    }
                });

        buttonSend.setOnClickListener(v -> sendEmergency());
    }

    private void sendEmergency() {
        String reason = spinnerReason.getSelectedItem().toString();
        String extra  = editTextDetails.getText().toString().trim();
        String fullMessage = reason + (extra.isEmpty() ? "" : ": " + extra);

        if (isNetworkAvailable()) {
            String key = messagesRef.push().getKey();
            if (key == null) {
                Toast.makeText(this, "Ошибка генерации ключа", Toast.LENGTH_SHORT).show();
                return;
            }

            Message msg = new Message(
                    key,
                    currentUid,
                    currentEmail,
                    currentName,
                    fullMessage,

                    System.currentTimeMillis(),
                    true,
                    currentUid, // ⬅ patient сам себе
                    "users/" + currentUid + "/medicalCard",
                    "patient",
                    reason
            );
            msg.attachedPatientUid = attachedPatientId;
            msg.patientId = currentUid;
            messagesRef.child(key).setValue(msg)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Сигнал отправлен", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE
                );
            } else {
                sendSmsFallback(fullMessage);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String reason = spinnerReason.getSelectedItem().toString();
            String extra  = editTextDetails.getText().toString().trim();
            String fullMessage = reason + (extra.isEmpty() ? "" : ": " + extra);
            sendSmsFallback(fullMessage);
        } else {
            Toast.makeText(this, "Разрешение на SMS не получено", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int SMS_PERMISSION_CODE = 2001;

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }

    private void sendSmsFallback(String text) {
        String doctorPhone = "+375293361695"; // TODO: заменить или получать из профиля врача
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(
                doctorPhone,
                null,
                "ЭКСТРЕННО от " + currentName + ": " + text,
                null,
                null
        );
        Toast.makeText(this, "SMS-альтернатива отправлена", Toast.LENGTH_SHORT).show();
    }

}
