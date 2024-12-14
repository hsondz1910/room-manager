package com.lastterm.finalexam.ui.contract;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.R;
import com.lastterm.finalexam.data.entities.Contract;

import java.util.Date;

public class AddContractFragment extends Fragment {

    private EditText etTenantId, etOwnerId, etRoomId, etRentAmount, etContractTerms;
    private Button btnSaveContract;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_contract, container, false);

        // Initialize Firestore and get current user ID
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        etTenantId = view.findViewById(R.id.etTenantId);
        etOwnerId = view.findViewById(R.id.etOwnerId);
        etRoomId = view.findViewById(R.id.etRoomId);
        etRentAmount = view.findViewById(R.id.etRentAmount);
        etContractTerms = view.findViewById(R.id.etContractTerms);
        btnSaveContract = view.findViewById(R.id.btnSaveContract);

        // Set up Save button click listener
        btnSaveContract.setOnClickListener(v -> saveContract());

        return view;
    }

    // Save contract to Firestore
    private void saveContract() {
        String tenantId = etTenantId.getText().toString().trim();
        String ownerId = etOwnerId.getText().toString().trim();
        String roomId = etRoomId.getText().toString().trim();
        String rentAmountStr = etRentAmount.getText().toString().trim();
        String contractTerms = etContractTerms.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(tenantId) || TextUtils.isEmpty(ownerId) || TextUtils.isEmpty(roomId) ||
                TextUtils.isEmpty(rentAmountStr) || TextUtils.isEmpty(contractTerms)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double rentAmount = Double.parseDouble(rentAmountStr);

        // Create contract object
        Contract contract = new Contract(tenantId, ownerId, roomId, new Date(), new Date(), rentAmount, true, contractTerms);

        // Save contract to Firestore
        db.collection("contracts")
                .add(contract)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Contract added successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); // Go back to previous fragment
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add contract: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
