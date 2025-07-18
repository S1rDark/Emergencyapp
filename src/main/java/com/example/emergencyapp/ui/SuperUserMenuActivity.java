package com.example.emergencyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class SuperUserMenuActivity extends AppCompatActivity {

    private Button btnConfirmations, btnChat,  buttonEmergency,  btnManageCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superuser_menu);

        btnConfirmations = findViewById(R.id.buttonConfirmations);
        btnChat          = findViewById(R.id.buttonChat);
        btnManageCards = findViewById(R.id.buttonManageCards);
        buttonEmergency = findViewById(R.id.buttonEmergency);
        btnManageCards.setOnClickListener(v -> {
            startActivity(new Intent(this, PatientsListActivity.class)
                    .putExtra("mode","manageCards"));
        });
        btnConfirmations.setOnClickListener(v -> {
            startActivity(new Intent(
                    SuperUserMenuActivity.this,
                    ConfirmationsActivity.class
            ));
        });
        buttonEmergency.setOnClickListener(v -> {
            Intent intent = new Intent(SuperUserMenuActivity.this, DoctorEmergencyFormActivity.class);
            startActivity(intent);
        });
        btnChat.setOnClickListener(v -> {
            startActivity(new Intent(
                    SuperUserMenuActivity.this,
                    ChatActivity.class
            ));
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();

                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(uid)
                                .child("fcmToken")
                                .setValue(token);
                    }
    });
    }
}
