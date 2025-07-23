package com.example.emergencyapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class DoctorEmergencyFormActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 2002;

    private Spinner spinnerPatient;
    private Spinner spinnerReason;
    private EditText editTextDetails;
    private Button buttonSend;

    private DatabaseReference usersRef, messagesRef;
    private String currentUid, currentEmail, currentName;

    private Map<String, String> patientMap = new HashMap<>(); // name -> uid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_emergency_form);

        spinnerPatient = findViewById(R.id.spinnerPatient);
        spinnerReason  = findViewById(R.id.spinnerReason);
        editTextDetails= findViewById(R.id.editTextDetails);
        buttonSend     = findViewById(R.id.buttonSendEmergency);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUid   = auth.getCurrentUser().getUid();
        currentEmail = auth.getCurrentUser().getEmail();

        usersRef    = FirebaseDatabase.getInstance().getReference("users");
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // Загрузка имени врача/админа
        usersRef.child(currentUid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        currentName = snap.getValue(String.class);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        currentName = currentEmail;
                    }
                });

        // Загрузка пациентов
        usersRef.orderByChild("role").equalTo("patient")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        List<String> names = new ArrayList<>();
                        for (DataSnapshot s : snap.getChildren()) {
                            String name = s.child("name").getValue(String.class);
                            if (name == null) continue;
                            names.add(name);
                            patientMap.put(name, s.getKey());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                DoctorEmergencyFormActivity.this,
                                android.R.layout.simple_spinner_item,
                                names
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPatient.setAdapter(adapter);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { }
                });

        buttonSend.setOnClickListener(v -> sendEmergency());
    }

    private void sendEmergency() {
        String patientName = (String) spinnerPatient.getSelectedItem();
        String reason = (String) spinnerReason.getSelectedItem();
        String extra  = editTextDetails.getText().toString().trim();

        if (patientName == null) {
            Toast.makeText(this, "Выберите пациента", Toast.LENGTH_SHORT).show();
            return;
        }

        String patientUid = patientMap.get(patientName);
        String message = reason + (extra.isEmpty() ? "" : ": " + extra);

        if (isNetworkAvailable()) {
            String key = messagesRef.push().getKey();
            if (key == null) {
                Toast.makeText(this, "Ошибка генерации ключа", Toast.LENGTH_SHORT).show();
                return;
            }

            Message m = new Message(
                    key,
                    currentUid,
                    currentEmail,
                    currentName,
                    message,
                    System.currentTimeMillis(),
                    true,
                    patientUid,
                    "users/" + patientUid + "/medicalCard",
                    "doctor",
                    reason
            );
            m.attachedPatientUid = patientUid;
            m.patientId = patientUid;

            messagesRef.child(key).setValue(m)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Сигнал отправлен", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DoctorEmergencyFormActivity.this, ChatActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Ошибка при отправке: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // fallback SMS
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SMS_PERMISSION_CODE
                );
            } else {
                sendSmsFallback(message);
            }
        }
        Intent intent = new Intent(DoctorEmergencyFormActivity.this, ChatActivity.class);

    }

    private void sendSmsFallback(String text) {
        String doctorPhone = "+37529"; // TODO: можно сделать выбор по пациенту
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(
                doctorPhone,
                null,
                "ЭКСТРЕННО: " + text,
                null,
                null
        );
        Toast.makeText(this, "SMS отправлена", Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(requestCode, p, r);
        if (requestCode == SMS_PERMISSION_CODE &&
                r.length > 0 &&
                r[0] == PackageManager.PERMISSION_GRANTED) {
            sendSmsFallback(spinnerReason.getSelectedItem().toString());
        } else {
            Toast.makeText(this, "SMS недоступна", Toast.LENGTH_SHORT).show();
        }
    }
}
