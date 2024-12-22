package com.lastterm.finalexam.ui.fragments.appointment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Appointment;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.AppointmentAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppointmentFrament extends Fragment {
    String role;
    RecyclerView ap_recylerview;
    ArrayList<Appointment> appointments;
    AppointmentAdapter adapter;
    RoomRepository repository;

    public AppointmentFrament(String role) {
        this.role = role;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apointment, container, false);
        ap_recylerview = view.findViewById(R.id.appointment_recylerview);
        repository = new RoomRepository();
        appointments = new ArrayList<>();
        ap_recylerview.setLayoutManager(new LinearLayoutManager(getContext()));
        loadAppointments();


        return view;
    }

    private void loadAppointments(){
        repository.getAllAppointments(res -> {
            Log.d("loadAppointments", "loadAppointments: " + res.size());
            if(res.size() >= 0){
                appointments.clear();
                appointments.addAll(res);
                sortAppointmentsByDate(appointments);
                adapter = new AppointmentAdapter(getContext(), role, appointments);
                ap_recylerview.setAdapter(adapter);
            }
        }, error -> {});
    }

    void sortAppointmentsByDate(List<Appointment> appointments) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Collections.sort(appointments, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment app1, Appointment app2) {
                try {
                    return dateFormat.parse(app2.getDate()).compareTo(dateFormat.parse(app1.getDate()));
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
    }
}
