package com.example.emergencyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.Message;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final List<Message> messageList;
    private final String currentUserId;
    private final String currentUserRole;
    private final Context context;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId, String currentUserRole) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.currentUserRole = currentUserRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Message msg = messageList.get(pos);

        h.textSender.setText(msg.senderName);
        h.textMessage.setText(msg.text);

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(msg.timestamp));
        h.textTime.setText(time);

        boolean isEmergency = msg.emergency;
        boolean isResolved = "done".equals(msg.status);
        boolean isAdmin = "superuser".equals(currentUserRole);
        boolean canViewCard = msg.patientId != null &&
                (currentUserRole.equals("doctor") || currentUserRole.equals("superuser"));

        // Цвет фона по статусу
        if (isEmergency) {
            if (isResolved) {
                h.itemView.setBackgroundColor(Color.parseColor("#E0F7FA")); // светло-голубой
            } else {
                h.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); // розовый
            }
        } else {
            h.itemView.setBackgroundColor(Color.WHITE);
        }

        // Кнопка "Выполнено"
        if (isEmergency && !isResolved && isAdmin) {
            h.buttonMarkDone.setVisibility(View.VISIBLE);
            h.buttonMarkDone.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("messages")
                        .child(msg.id)
                        .child("status")
                        .setValue("done");
            });
        } else {
            h.buttonMarkDone.setVisibility(View.GONE);
        }

        // Кнопка "Медкарта"
        if (canViewCard) {
            h.buttonViewCard.setVisibility(View.VISIBLE);
            h.buttonViewCard.setOnClickListener(v -> {
                if (msg.patientId != null && !msg.patientId.isEmpty()) {
                Intent intent = new Intent(context, MedicalCardActivity.class);
                intent.putExtra("userId", msg.patientId);
                intent.putExtra("mode", "view");  // или "edit", если хочешь дать врачам редактировать
                context.startActivity(intent);  }
        else {
                Toast.makeText(context, "Нет данных пациента", Toast.LENGTH_SHORT).show();
            }
            });
        } else {
            h.buttonViewCard.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime;
        Button buttonMarkDone, buttonViewCard;

        public ViewHolder(@NonNull View v) {
            super(v);
            textSender = v.findViewById(R.id.textSender);
            textMessage = v.findViewById(R.id.textMessage);
            textTime = v.findViewById(R.id.textTime);
            buttonMarkDone = v.findViewById(R.id.buttonMarkDone);
            buttonViewCard = v.findViewById(R.id.buttonViewCard);
        }
    }
}
