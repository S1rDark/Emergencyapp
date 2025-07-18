package com.example.emergencyapp.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ConfirmationsActivity extends AppCompatActivity {
    RecyclerView recycler;
    SuperUserAdapter adapter;
    List<User> users = new ArrayList<>();
    DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_super_user);
        recycler = findViewById(R.id.recyclerUsers);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SuperUserAdapter(users);
        recycler.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        refUsers.child(uid).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String role = snap.getValue(String.class);
                        if (!"superuser".equals(role) && !"doctor".equals(role)) {
                            Toast.makeText(ConfirmationsActivity.this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                            finish(); return;
                        }
                        loadPending(role);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void loadPending(String role) {
        // admin видит всех, doctor — только пациентов
        refUsers.orderByChild("confirmed").equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        users.clear();
                        for (DataSnapshot ds: snap.getChildren()) {
                            User u = ds.getValue(User.class);
                            u.uid = ds.getKey();
                            if ("superuser".equals(role) || "patient".equals(u.role))
                                users.add(u);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }
}
