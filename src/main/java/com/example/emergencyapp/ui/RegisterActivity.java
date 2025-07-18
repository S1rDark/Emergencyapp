package com.example.emergencyapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, nameField, passField, confirmField, roomField;
    private RadioGroup roleGroup;
    private RadioButton rbDoctor, rbPatient;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailField   = findViewById(R.id.editTextRegEmail);
        passField    = findViewById(R.id.editTextRegPassword);
        confirmField = findViewById(R.id.editTextRegConfirm);
        roomField    = findViewById(R.id.editTextRegRoom);
        roleGroup    = findViewById(R.id.radioGroupRegRole);
        rbDoctor     = findViewById(R.id.radioRegDoctor);
        rbPatient    = findViewById(R.id.radioRegPatient);
        btnRegister  = findViewById(R.id.buttonRegister);
        nameField    = findViewById(R.id.editTextRegName);
        mAuth    = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Показываем поле палаты
        roleGroup.setOnCheckedChangeListener((g, id) -> {
            roomField.setVisibility(id == R.id.radioRegPatient
                    ? View.VISIBLE : View.GONE);
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String name  = nameField .getText().toString().trim();
        String pass  = passField.getText().toString();
        String conf  = confirmField.getText().toString();
        String role  = rbPatient.isChecked() ? "patient" : "doctor";
        String room  = roomField.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || conf.isEmpty() || name.isEmpty()
                || (role.equals("patient") && room.isEmpty())) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(conf)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        DatabaseReference uRef = usersRef.child(uid);
                        uRef.child("email").    setValue(email);
                        uRef.child("role").     setValue(role);
                        uRef.child("confirmed").setValue(false);
                        uRef.child("name").     setValue(name);        // сохраняем имя
                        if (role.equals("patient")) {
                            uRef.child("room").setValue(room);
                        }
                        Toast.makeText(this,
                                "Регистрация успешна, ожидайте подтверждения",
                                Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Ошибка: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
