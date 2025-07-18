package com.example.emergencyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView goRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField    = findViewById(R.id.editTextLoginEmail);
        passwordField = findViewById(R.id.editTextLoginPassword);
        loginButton   = findViewById(R.id.buttonLogin);
        goRegister    = findViewById(R.id.textGoRegister);

        mAuth    = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        goRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String pass  = passwordField.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Ошибка входа: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();
                    usersRef.child(uid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String role = snap.child("role").getValue(String.class);

                                    // 1. Пропускаем супер-пользователя сразу, без confirmed
                                    if ("superuser".equals(role)) {
                                        startActivity(new Intent(
                                                LoginActivity.this, SuperUserMenuActivity.class));
                                        finish();
                                        return;
                                    }

                                    // 2. Для остальных проверяем confirmed
                                    Boolean confirmed = snap.child("confirmed")
                                            .getValue(Boolean.class);
                                    if (confirmed != null && confirmed) {
                                        Intent i;
                                        if ("doctor".equals(role)) {
                                            i = new Intent(LoginActivity.this, DoctorDashboardActivity.class);
                                        } else {  // пациент
                                            i = new Intent(LoginActivity.this, PatientActivity.class);
                                        }
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this,
                                                "Ожидайте подтверждения регистрации",
                                                Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError e) { }
                            });
                });
    }

}
