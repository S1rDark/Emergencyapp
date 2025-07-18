// PatientAdapter.java
package com.example.emergencyapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.emergencyapp.R;
import com.example.emergencyapp.model.User;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private final List<User> patients;
    private OnItemClickListener listener;

    public PatientAdapter(List<User> patients) {
        this.patients = patients;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        User u = patients.get(position);
        // Показываем имя, если есть, иначе email
        String display = (u.name != null && !u.name.isEmpty()) ? u.name : u.email;
        holder.textName.setText(display);
        holder.textEmail.setText(u.email);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(u);
        });
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail;
        PatientViewHolder(View itemView) {
            super(itemView);
            textName  = itemView.findViewById(R.id.textPatientName);
            textEmail = itemView.findViewById(R.id.textPatientEmail);
        }
    }
}
