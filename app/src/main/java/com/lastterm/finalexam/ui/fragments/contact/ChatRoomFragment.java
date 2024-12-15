package com.lastterm.finalexam.ui.fragments.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.ChatRoom;

import com.lastterm.finalexam.data.entities.MessageClass;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

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

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ChatRoomFragment(String roomID, String sender, String receiver) {
        chatRoom = new ChatRoom(roomID, sender, receiver);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar = view.findViewById(R.id.toolBarChat);
        toolbarTitle = view.findViewById(R.id.toolbar_title);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if(chatRoom.getUsers().get(0).equals(repository.getCurrentUser())){
            repository.getNameByUserID(chatRoom.getUsers().get(1), (name) -> {toolbarTitle.setText(name);});

        }else {
            repository.getNameByUserID(chatRoom.getUsers().get(0), (name) -> {toolbarTitle.setText(name);});
        }

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_chat, container, false);
        // Đặt Toolbar làm Action Bar


        chatRecyclerView = view.findViewById(R.id.chat_RecylerView);
        textMsg = view.findViewById(R.id.text_Msg);
        btnSent = view.findViewById(R.id.btn_Sent);
        btnAddImage = view.findViewById(R.id.btn_AddImage);
        imagePreview = view.findViewById(R.id.image_preview);


        repository = new RoomRepository();

        messages = new ArrayList<>();



        textMsg.setHint("Nhập tin nhắn của bạn...");




        repository.getRoomById(chatRoom.getRoomId(), (room)->{
            if(room == null) btnSent.setEnabled(false);
        });

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

    private void loadMessage() {
        repository.findChatRoom(chatRoom.getRoomId(),chatRoom.getUsers().get(0), chatRoom.getUsers().get(1), (chatRoom) -> {
            this.chatRoom = chatRoom;
            if(chatRoom.getMessages() != null)
                messages.addAll(chatRoom.getMessages());
            adapter = new ChatAdapter(messages, getContext());
            chatRecyclerView.setAdapter(adapter);
            Log.d("TAG", "loadMessage===: " + chatRoom.getUsers().get(0).equals(repository.getCurrentUser()));
            if(chatRoom.getUsers().get(0).equals(repository.getCurrentUser()))
                repository.setMessagesRead(chatRoom.getId(),chatRoom.getUsers().get(1) ,(d) -> {}, (e) -> {});
            else
                repository.setMessagesRead(chatRoom.getId(),chatRoom.getUsers().get(0) ,(d) -> {}, (e) -> {});
            updateMesage(chatRoom.getId());
        }, (e) -> {});
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
}
