package com.example.emergencyapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emergencyapp.R;
import com.example.emergencyapp.model.User;
import com.google.firebase.database.*;

import java.util.List;

public class SuperUserAdapter extends RecyclerView.Adapter<SuperUserAdapter.UserViewHolder> {

    private List<User> users;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    public SuperUserAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User u = users.get(position);
        holder.textEmail.setText(
                u.name != null && !u.name.isEmpty()
                        ? u.name
                        : u.email
        );
        holder.textRole.setText("Роль: " + u.role);

        if ("patient".equals(u.role) && u.room != null) {
            holder.textRoom.setText("Палата: " + u.room);
            holder.textRoom.setVisibility(View.VISIBLE);
        } else {
            holder.textRoom.setVisibility(View.GONE);
        }

        holder.buttonConfirm.setOnClickListener(v -> {
            usersRef.child(u.uid).child("confirmed").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(),
                                "Пользователь " + u.email + " подтверждён",
                                Toast.LENGTH_SHORT).show();
                        removeAt(position);
                    });
        });

        holder.buttonReject.setOnClickListener(v -> {
            usersRef.child(u.uid).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(),
                                "Пользователь " + u.email + " отклонён",
                                Toast.LENGTH_SHORT).show();
                        removeAt(position);
                    });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private void removeAt(int pos) {
        users.remove(pos);
        notifyItemRemoved(pos);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textEmail, textRole, textRoom;
        Button buttonConfirm, buttonReject;

        UserViewHolder(View itemView) {
            super(itemView);
            textEmail = itemView.findViewById(R.id.textName);
            textRole  = itemView.findViewById(R.id.textRole);
            textRoom  = itemView.findViewById(R.id.textRoom);
            buttonConfirm = itemView.findViewById(R.id.buttonConfirm);
            buttonReject  = itemView.findViewById(R.id.buttonReject);
        }
    }
}
