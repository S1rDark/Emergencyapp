package com.example.emergencyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;

public class DoctorDashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_doctor_dashboard);
        findViewById(R.id.btnConfirm).setOnClickListener(v ->
                startActivity(new Intent(this, ConfirmationsActivity.class)));
        findViewById(R.id.btnMedCards).setOnClickListener(v ->
                startActivity(new Intent(this, PatientsListActivity.class)
                        .putExtra("mode","manageCards")));
        findViewById(R.id.btnChat).setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));
        Button buttonEmergency = findViewById(R.id.buttonEmergency);

        buttonEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, DoctorEmergencyFormActivity.class);
            startActivity(intent);
        });
    }
}

