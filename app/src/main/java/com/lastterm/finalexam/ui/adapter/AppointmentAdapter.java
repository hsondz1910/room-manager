package com.lastterm.finalexam.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Appointment;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    Context context;
    ArrayList<Appointment> appointments;

    public AppointmentAdapter(Context context, ArrayList<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tile.setText(appointment.getTitle());
        holder.date.setText(appointment.getDate());
        holder.time.setText(appointment.getTime());
        switch (appointment.getStatus()){
            case 0:
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.appointment_not_comf));
                break;
            case 1:
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.appointment_comf));
                break;
            case 2:
                holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.appointment_not_accept));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder{
        CardView card;
        TextView tile, date, time;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_appointment);
            tile = itemView.findViewById(R.id.text_Title_Appointment);
            date = itemView.findViewById(R.id.text_Date_Appointment);
            time = itemView.findViewById(R.id.text_Time_Appointment);
        }
    }
}
