package com.lastterm.finalexam.ui.adapter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Appointment;
import com.lastterm.finalexam.data.repositories.RoomRepository;

import org.checkerframework.checker.units.qual.C;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    Context context;
    String role;
    ArrayList<Appointment> appointments;
    RoomRepository repository = new RoomRepository();

    public AppointmentAdapter(Context context, String role, ArrayList<Appointment> appointments) {
        this.context = context;
        this.role = role;
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
        holder.confirm_layout.setVisibility(View.GONE);

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

        if(role.equals("owner") && appointment.getStatus() == 0){
            holder.itemView.setOnClickListener(view -> {
                if(holder.confirm_layout.getVisibility() == View.VISIBLE){
                    holder.confirm_layout.setVisibility(View.GONE);
                    return;
                }
                holder.confirm_layout.setVisibility(View.VISIBLE);
                holder.accept.setOnClickListener(v -> {
                    appointment.setStatus(1);
                    repository.updateAppointment(appointment, (s) -> {
                        String msg = "Đã xác nhận cuộc hẹn vào lúc " + appointment.getDate() + " " + appointment.getTime();
                        repository.sendMessage(context, appointment.getChatID(), msg, null, (message) -> {
                        }, (e) -> {});
                        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.appointment_comf));
                        holder.confirm_layout.setVisibility(View.GONE);
                    }, (e) -> {});
                });
                holder.deny.setOnClickListener(v -> {
                    appointment.setStatus(2);
                    repository.updateAppointment(appointment, (s) -> {
                        String msg = "Đã từ chối cuộc hẹn vào lúc " + appointment.getDate() + " " + appointment.getTime();
                        repository.sendMessage(context, appointment.getChatID(), msg, null, (message) -> {
                        }, (e) -> {});
                        holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.appointment_not_accept));
                        holder.confirm_layout.setVisibility(View.GONE);
                    }, (e) -> {});
                });
            });
        }
        if(role.equals("tenant") && appointment.getStatus() == 0){
            holder.itemView.setOnClickListener(view -> {
                try {
                    showDialogUpdateAppointment(position);
                }
                catch (Exception e){
                    Log.d("Exception", "onBindViewHolder: " + e.getMessage());
                    Toast.makeText(context, "Lỗi khi cập nhật lịch hẹn.", Toast.LENGTH_SHORT).show();
                }

            });
        }

    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    class AppointmentViewHolder extends RecyclerView.ViewHolder{
        CardView card;
        TextView tile, date, time;
        Button accept, deny;
        LinearLayout confirm_layout;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_appointment);
            tile = itemView.findViewById(R.id.text_Title_Appointment);
            date = itemView.findViewById(R.id.text_Date_Appointment);
            time = itemView.findViewById(R.id.text_Time_Appointment);
            accept = itemView.findViewById(R.id.accept_app);
            deny = itemView.findViewById(R.id.deny);
            confirm_layout = itemView.findViewById(R.id.confirm_layout);
        }
    }

    private void showDialogUpdateAppointment(int position) throws ParseException {
        Appointment appointment = appointments.get(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_create_appointment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(view);

        EditText date = view.findViewById(R.id.date_app);
        EditText time = view.findViewById(R.id.time_app);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateformated =  dateFormat.parse(appointment.getDate());
        Calendar celendar = Calendar.getInstance();
        celendar.set(dateformated.getYear(), dateformated.getMonth(), dateformated.getDay());
        date.setText(appointment.getDate());

        date.setFocusable(false);
        date.setOnClickListener((v) -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view1, year, month, dayOfMonth) -> {
                        celendar.set(year, month, dayOfMonth);
                        String str = celendar.get(Calendar.DAY_OF_MONTH) + "-" + (celendar.get(Calendar.MONTH) + 1) + "-" + celendar.get(Calendar.YEAR);
                        date.setText(str);
                    },
                    celendar.get(Calendar.YEAR), celendar.get(Calendar.MONTH), celendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        time.setText(appointment.getTime());
        time.setFocusable(false);
        time.setOnClickListener((n)-> {
            time.requestFocus();
            Log.d("EditTextFocus", "Time EditText has focus: " + time.hasFocus());
            TimePickerDialog timePicker = new TimePickerDialog(context,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int min) {
                            time.setText(String.format("%02d:%02d", hour, min));
                        }
                    }
                    , celendar.get(Calendar.HOUR_OF_DAY), celendar.get(Calendar.MINUTE), true);
            timePicker.show();

        });

        builder.setTitle("Thêm lịch hẹn")
                .setPositiveButton("Thay đổi", (dialog, which) -> {
                    if(date.getText().toString() != "" && time.getText().toString() != ""){
                        appointment.setDate(date.getText().toString());
                        appointment.setTime(time.getText().toString());
                        repository.updateAppointment(appointment,(s) -> {
                            String msg = "Đặt lại cuộc hẹn vào lúc " + appointment.getDate() + " " + appointment.getTime();
                            repository.sendMessage(context, appointment.getChatID(), msg, null, (message) -> {
                            }, (e) -> {});
                            Toast.makeText(context,"Đã sữa lại lịch.", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                        }, (e) -> {
                            Toast.makeText(context,"Lỗi khi sửa cuộc hẹn.", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNeutralButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Xóa", (dialog, which) -> {
                    repository.deleteAppointment(appointment, (s) -> {}, (e) -> {});
                    String msg = "Đã xóa cuộc hẹn vào lúc " + appointment.getDate() + " " + appointment.getTime();
                    repository.sendMessage(context, appointment.getChatID(), msg, null, (message) -> {
                    }, (e) -> {});
                    appointments.remove(position);
                    notifyDataSetChanged();
                });
        builder.show();

    }
}
