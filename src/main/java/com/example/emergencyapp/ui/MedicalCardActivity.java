package com.example.emergencyapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.emergencyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class MedicalCardActivity extends AppCompatActivity {

    private EditText eAllergies, eConditions, eNotes;
    private Button btnSave;
    private DatabaseReference userRef;
    private String userId;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_medical_card);

        eAllergies  = findViewById(R.id.editAllergies);
        eConditions = findViewById(R.id.editConditions);
        eNotes      = findViewById(R.id.editNotes);
        btnSave     = findViewById(R.id.buttonSaveCard);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "Ошибка: не передан ID пользователя", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("medicalCard");

        // Загрузка данных карточки
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                eAllergies.setText(snap.child("allergies").getValue(String.class));
                eConditions.setText(snap.child("conditions").getValue(String.class));
                eNotes.setText(snap.child("notes").getValue(String.class));
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Проверка прав на редактирование
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(currentUid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = snapshot.getValue(String.class);
                        boolean canEdit = "doctor".equals(role) || "superuser".equals(role);

                        // Только врачи/админы могут редактировать чужие карты
                        // Пациент может редактировать только свою (по желанию можно ограничить)
                        if (!canEdit && !currentUid.equals(userId)) {
                            canEdit = false;
                        }

                        eAllergies.setEnabled(canEdit);
                        eConditions.setEnabled(canEdit);
                        eNotes.setEnabled(canEdit);
                        btnSave.setVisibility(canEdit ? View.VISIBLE : View.GONE);
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Сохраняем изменения
        btnSave.setOnClickListener(v -> {
            Map<String, Object> card = new HashMap<>();
            card.put("allergies", eAllergies.getText().toString());
            card.put("conditions", eConditions.getText().toString());
            card.put("notes", eNotes.getText().toString());

            userRef.setValue(card).addOnSuccessListener(a -> {
                Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
