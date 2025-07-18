// PatientsListActivity.java
package com.example.emergencyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class PatientsListActivity extends AppCompatActivity {
    RecyclerView recycler;
    PatientAdapter adapter;
    List<User> patients = new ArrayList<>();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_patients_list);
        recycler = findViewById(R.id.recyclerPatients);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatientAdapter(patients);
        recycler.setAdapter(adapter);

        adapter.setOnItemClickListener(user -> {
            // mode=manageCards → редактировать, иначе view собственного
            startActivity(new Intent(this, MedicalCardActivity.class)
                    .putExtra("userId", user.uid)
                    .putExtra("mode", getIntent().getStringExtra("mode")));
        });

        ref.orderByChild("role").equalTo("patient")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        patients.clear();
                        for (DataSnapshot ds: s.getChildren()) {
                            User u = ds.getValue(User.class);
                            u.uid = ds.getKey();
                            if (Boolean.TRUE.equals(u.confirmed))
                                patients.add(u);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }
}
