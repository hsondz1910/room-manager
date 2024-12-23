package com.lastterm.finalexam.ui.fragments.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.ChatRoom;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.adapter.ContactAdapter;

import java.util.List;

public class ContactFrament extends Fragment {
    String role;
    RecyclerView contacts_view;
    List<ChatRoom> chatRooms;
    ContactAdapter adapter;
    RoomRepository repository;
    public ContactFrament(String role) {
        this.role = role;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        contacts_view = view.findViewById(R.id.contract_recylerview);
        repository = new RoomRepository();
        contacts_view.setLayoutManager(new LinearLayoutManager(getContext()));
        loadContacts();
        return view;
    }

    private void loadContacts(){
        if(!role.contains("admin")){
            repository.getAllChatRoomOfUser(repository.getCurrentUser(), chatRooms -> {
                this.chatRooms = chatRooms;
                adapter = new ContactAdapter(getContext(), role ,chatRooms);
                contacts_view.setAdapter(adapter);
            }, e -> {});
        }else {
            repository.getChatRoomSupport(chatRooms -> {
                this.chatRooms = chatRooms;
                adapter = new ContactAdapter(getContext(), role ,chatRooms);
                contacts_view.setAdapter(adapter);
            }, e -> {});
        }

    }
}
