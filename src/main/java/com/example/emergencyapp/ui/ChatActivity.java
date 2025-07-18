package com.example.emergencyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText editTextMessage;
    private Button buttonSend;

    private MessageAdapter adapter;
    private List<Message> msgList = new ArrayList<>();

    private DatabaseReference messagesRef;
    private DatabaseReference usersRef;
    private String currentUid;
    private String currentEmail;
    private String currentName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerChat    = findViewById(R.id.recyclerChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend      = findViewById(R.id.buttonSend);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUid   = auth.getCurrentUser().getUid();
        currentEmail = auth.getCurrentUser().getEmail();
        usersRef     = FirebaseDatabase.getInstance().getReference("users");
        messagesRef  = FirebaseDatabase.getInstance().getReference("messages");

        // Проверка и запуск чата
        usersRef.child(currentUid).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if (!"doctor".equals(role) && !"superuser".equals(role)) {
                    Toast.makeText(ChatActivity.this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                new EmergencyNotificationListener(ChatActivity.this).start(); // ✅ Запуск
                setupChat(role); // ✅ вызываем метод
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                setupChat("doctor"); // fallback
            }
        });

        // Имя пользователя
        usersRef.child(currentUid).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String n = snap.getValue(String.class);
                        currentName = (n != null && !n.isEmpty()) ? n : currentEmail;
                        buttonSend.setEnabled(true);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {
                        currentName = currentEmail;
                        buttonSend.setEnabled(true);
                    }
                });
    }

    // ✅ Вынесенный метод
    private void setupChat(String role) {
        boolean isAdminOrDoctor = "doctor".equals(role) || "superuser".equals(role);
        adapter = new MessageAdapter(
                this, msgList, currentUid, role
        );
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);

        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot ds, String prev) {
                Message m = ds.getValue(Message.class);
                if (m != null) {
                    m.id = ds.getKey();
                    msgList.add(m);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    recyclerChat.scrollToPosition(msgList.size() - 1);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot ds, String prevChildKey) {
                Message updated = ds.getValue(Message.class);
                if (updated != null) {
                    updated.id = ds.getKey();
                    for (int i = 0; i < msgList.size(); i++) {
                        if (msgList.get(i).id.equals(updated.id)) {
                            msgList.set(i, updated);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot ds) {}
            @Override public void onChildMoved(@NonNull DataSnapshot ds, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        buttonSend.setOnClickListener(v -> sendMessage());
    }


    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String key = messagesRef.push().getKey();
        if (key == null) return;

        String nameToSend = (currentName != null) ? currentName : currentEmail;

        Message m = new Message(
                key,
                currentUid,
                currentEmail,
                nameToSend,
                text,
                System.currentTimeMillis(),
                false
        );

        messagesRef.child(key).setValue(m)
                .addOnSuccessListener(a -> editTextMessage.setText(""))
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Ошибка отправки: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }
}
