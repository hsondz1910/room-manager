package com.lastterm.finalexam.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.MainActivity;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.repositories.RoomRepository;
import com.lastterm.finalexam.ui.account.EditProfileFragment;
import com.lastterm.finalexam.ui.account.LoginActivity;
import com.lastterm.finalexam.ui.fragments.appointment.AppointmentFrament;
import com.lastterm.finalexam.ui.fragments.contact.ChatRoomFragment;
import com.lastterm.finalexam.ui.fragments.contract.ContractFragment;

public class SettingFragment extends Fragment {
    private TextView tvUsername, tvEmail, tvPhone, tvRole;
    private Button btnLogout, btnEditProfile, btnSupport, btnAppointment, btnContract;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView img;
    private RoomRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        repository = new RoomRepository();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvRole = view.findViewById(R.id.tvRole);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnSupport = view.findViewById(R.id.btnSupport);
        btnAppointment = view.findViewById(R.id.btnAppointment);
        btnContract = view.findViewById(R.id.btnContract);
        img = view.findViewById(R.id.profileImage);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                tvUsername.setText(documentSnapshot.getString("username"));
                tvEmail.setText(documentSnapshot.getString("email"));
                tvPhone.setText(documentSnapshot.getString("phone"));
                tvRole.setText(documentSnapshot.getString("role"));
                if(documentSnapshot.getString("urlAvatar") != null){
                    try {
                        Glide.with(this).load(documentSnapshot.getString("urlAvatar")).into(img);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                btnSupport.setOnClickListener(v -> {
                    repository.findChatRoomWithAdmin(repository.getCurrentUser(), (room) -> {
                        Fragment defaultFragment = new ChatRoomFragment(documentSnapshot.getString("role"),room);
                        if (defaultFragment != null) {
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, defaultFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }, e -> {});
                });

                btnAppointment.setOnClickListener(v -> {
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new AppointmentFrament(documentSnapshot.getString("role")))
                            .addToBackStack(null)
                            .commit();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        btnEditProfile.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });



        repository.getRole((role) ->{
            if(role.contains("owner")) {
                btnContract.setVisibility(View.GONE);
            }
            if(role.contains("tenant") || role.contains("owner")){
                btnAppointment.setVisibility(View.VISIBLE);
            }else {
                btnAppointment.setVisibility(View.GONE);
            }

        }, e -> {});

        btnContract.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ContractFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
