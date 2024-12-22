package com.lastterm.finalexam.ui.fragments.contact;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Appointment;
import com.lastterm.finalexam.data.entities.ChatRoom;

import com.lastterm.finalexam.data.entities.MessageClass;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.AppointmentAdapter;
import com.lastterm.finalexam.ui.adapter.ChatAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class ChatRoomFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private ChatRoom chatRoom;
    private ChatAdapter adapter;
    private ImageView imagePreview;
    private Toolbar toolbar;
    private TextView toolbarTitle;

    private EditText textMsg;
    private ImageButton btnSent, btnAddImage;
    private Uri selectedImageUri = null;

    private RoomRepository repository;

    private List<MessageClass> messages;
    private ArrayList<Appointment> appointments;

    private String name;
    private String role;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ChatRoomFragment(String role, ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
        this.role = role;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolBarChat);
        toolbarTitle = view.findViewById(R.id.toolbar_title);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        if(chatRoom.getUsers().contains("Support") && !role.equals("admin")){
            toolbarTitle.setText("Hỗ trợ");
        }else {
            if(role.equals("admin")){
                repository.getNameByUserID(chatRoom.getUsers().get(0), (name) -> {
                    toolbarTitle.setText(name);
                    this.name = name;
                });
            }else {
                if(chatRoom.getUsers().get(0).equals(repository.getCurrentUser())){
                    repository.getNameByUserID(chatRoom.getUsers().get(1), (name) -> {
                        toolbarTitle.setText(name);
                        this.name = name;
                    });

                }else {
                    repository.getNameByUserID(chatRoom.getUsers().get(0), (name) -> {
                        toolbarTitle.setText(name);
                        this.name = name;
                    });
                }
            }
        }


        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if(!chatRoom.getUsers().contains("Support")) {
            repository.getRoomById(chatRoom.getRoomId(), (room) -> {
                if (room == null)
                {
                    inflater.inflate(R.menu.menu_chat_room, menu);
                    menu.findItem(R.id.dialog_create_appointment).setVisible(false);
                }else {
                    inflater.inflate(R.menu.menu_chat_room, menu);
                    menu.findItem(R.id.dialog_create_appointment).setVisible(true);
                }
            });
        }else {
            inflater.inflate(R.menu.menu_chat_room, menu);
            menu.findItem(R.id.dialog_create_appointment).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.dialog_create_appointment){
            showDialogListAppointment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showDialogListAppointment();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_chat, container, false);
        setHasOptionsMenu(true);
        chatRecyclerView = view.findViewById(R.id.chat_RecylerView);
        textMsg = view.findViewById(R.id.text_Msg);
        btnSent = view.findViewById(R.id.btn_Sent);
        btnAddImage = view.findViewById(R.id.btn_AddImage);
        imagePreview = view.findViewById(R.id.image_preview);


        repository = new RoomRepository();
        appointments = new ArrayList<>();
        messages = new ArrayList<>();



        textMsg.setHint("Nhập tin nhắn của bạn...");
        if(!chatRoom.getUsers().contains("Support")) {
            repository.getRoomById(chatRoom.getRoomId(), (room) -> {
                if (room == null)
                {
                    btnSent.setEnabled(false);
                }

            });
        }

        if(!chatRoom.getUsers().contains("Support")) {
            loadAppointments();
        }

        loadMessage();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imagePreview.setImageURI(selectedImageUri);
                            imagePreview.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        btnAddImage.setOnClickListener(v -> {
            openImagePicker();
        });

        btnSent.setOnClickListener(v -> {
            if(textMsg.getText().toString() != "" || selectedImageUri != null){
                sendMessage();
            }
        });

        imagePreview.setOnClickListener(v -> {
            selectedImageUri = null;
            imagePreview.setVisibility(View.GONE);
        });

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void loadAppointments() {
        repository.getAppointment(chatRoom.getId(), (appointments) -> {
            this.appointments.clear();
            this.appointments.addAll(appointments);
        });
    }

    private void loadMessage() {
        repository.findChatRoom(chatRoom.getId(), (chatRoom) -> {
            this.chatRoom = chatRoom;
            if(chatRoom.getMessages() != null)
                messages.addAll(chatRoom.getMessages());
            adapter = new ChatAdapter(messages, getContext());
            chatRecyclerView.setAdapter(adapter);
            if(chatRoom.getUsers().get(0).equals(repository.getCurrentUser()))
                repository.setMessagesRead(chatRoom.getId(),chatRoom.getUsers().get(1) ,(d) -> {}, (e) -> {});
            else
                repository.setMessagesRead(chatRoom.getId(),chatRoom.getUsers().get(0) ,(d) -> {}, (e) -> {});
            updateMesage(chatRoom.getId());
        }, (e) -> {
            Log.d("loadMessage", "fail:" + e.toString());
        });
    }

    private void updateMesage(String roomId){
        repository.listenToMessages(roomId, (newMessages) -> {
            if(newMessages != null)
                if(!newMessages.isEmpty()){
                    this.messages.clear();
                    this.messages.addAll(newMessages);
                    adapter.notifyDataSetChanged();
                }
        }, (e) -> {});
    }

    private void sendMessage() {
        String msg = textMsg.getText().toString();
        btnSent.setEnabled(false);
        repository.sendMessage(getContext(), chatRoom.getId(), msg, selectedImageUri, (message) -> {
            textMsg.setText("");
            selectedImageUri = null;
            imagePreview.setVisibility(View.GONE);
            btnSent.setEnabled(true);
        }, (e) -> {btnSent.setEnabled(true);});
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void showDialogListAppointment(){
        Log.d("show", "show " + appointments.size());
        LinearLayout layout = new LinearLayout(requireContext());

        RecyclerView recyclerView = new RecyclerView(requireContext());

        AppointmentAdapter adapter = new AppointmentAdapter(getContext(), role, appointments);
        Log.d("show", "show " + adapter.getItemCount());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(recyclerView);


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        if(role.equals("tenant")){
            builder.setTitle("Lịch hẹn:")
                    .setView(layout)
                    .setPositiveButton("Thêm cuộc hẹn", (dialog, which) -> {
                        showDialogCreateAppointment();
                    })
                    .setNegativeButton("Thoát", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
        if(role.equals("owner")){
            builder.setTitle("Lịch hẹn:")
                    .setView(layout)
                    .setNegativeButton("Thoát", (dialog, which) -> dialog.dismiss());
            builder.show();
        }

    }

    private void showDialogCreateAppointment(){
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_create_appointment, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setView(view);

        EditText date = view.findViewById(R.id.date_app);
        EditText time = view.findViewById(R.id.time_app);

        Calendar celendar = Calendar.getInstance();
        date.setFocusable(false);
        date.setOnClickListener((v) -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        celendar.set(year, month, dayOfMonth);
                        String str = celendar.get(Calendar.DAY_OF_MONTH) + "-" + (celendar.get(Calendar.MONTH) + 1) + "-" + celendar.get(Calendar.YEAR);
                        date.setText(str);
                    },
                    celendar.get(Calendar.YEAR), celendar.get(Calendar.MONTH), celendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        time.setFocusable(false);
        time.setOnClickListener((n)-> {
            time.requestFocus();
            Log.d("EditTextFocus", "Time EditText has focus: " + time.hasFocus());
            TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
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
                .setPositiveButton("Thêm", (dialog, which) -> {
                    if(date.getText().toString() != "" && time.getText().toString() != ""){
                        Appointment appointment = new Appointment(chatRoom.getId(),
                                "Hẹn xem phòng của " + name,
                                "Hẹn xem phòng",
                                date.getText().toString(),
                                time.getText().toString(),
                                "",
                                0);
                        checkPerMission(appointment);
                        repository.addAppointment(appointment,(s) -> {
                            String msg = "Hẹn xem phòng vào lúc " + date.getText().toString() + " " + time.getText().toString();
                            repository.sendMessage(getContext(), chatRoom.getId(), msg, selectedImageUri, (message) -> {
                                textMsg.setText("");
                                selectedImageUri = null;
                                imagePreview.setVisibility(View.GONE);
                                btnSent.setEnabled(true);
                            }, (e) -> {btnSent.setEnabled(true);});
                            Toast.makeText(getContext(),"Đã thêm vào lịch.", Toast.LENGTH_SHORT).show();
                        }, (e) -> {
                            Toast.makeText(getContext(),"Lỗi khi thêm vào lịch.", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNegativeButton("Thoát", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private String checkPerMission(Appointment appointment){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    100);
        } else {
            return addEventToCalendar(appointment);
        }
        return "";
    }

    private String addEventToCalendar(Appointment appointment) {
        try {
            ContentResolver contentResolver = getContext().getContentResolver();
            // Parse date and time
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date eventDate = dateTimeFormat.parse(appointment.getDate() + " " + appointment.getTime());

            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.TITLE, appointment.getTitle());
            values.put(CalendarContract.Events.DESCRIPTION, appointment.getDescription());
            values.put(CalendarContract.Events.DTSTART, eventDate.getTime());
            values.put(CalendarContract.Events.DTEND, eventDate.getTime() + 60 * 60 * 1000);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            // Insert Calendar Event
            Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);

            if (uri != null) {
                return uri.getLastPathSegment();
            } else {
                return "";
            }

        } catch (Exception e) {
            Log.e("Calendar", "Lỗi khi thêm sự kiện vào lịch", e);
            return "";
        }
    }
}
