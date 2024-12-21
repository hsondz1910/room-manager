package com.lastterm.finalexam.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.data.entities.Contract;

import java.util.ArrayList;
import java.util.List;

public class ContractRepository {

    private final FirebaseFirestore db;
    private final CollectionReference contractsRef;

    public ContractRepository() {
        db = FirebaseFirestore.getInstance();
        contractsRef = db.collection("contracts");
    }

    // Fetch contracts for the current user (based on ownerId)
    public void fetchContractsByUserId(String userId, final Callback callback) {
        contractsRef.whereEqualTo("ownerId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Contract> contractList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Contract contract = document.toObject(Contract.class);
                            contract.setContractId(document.getId()); // Set the document ID as the contract ID
                            contractList.add(contract);
                        }
                        callback.onSuccess(contractList);
                    } else {
                        callback.onError("Error fetching contracts: " + task.getException().getMessage());
                    }
                });
    }

    // Delete a contract from Firestore
    public void deleteContract(String contractId, final Callback callback) {
        contractsRef.document(contractId).delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Error deleting contract: " + e.getMessage()));
    }

    // Interface for callback handling success and error
    public interface Callback {
        void onSuccess(List<Contract> contracts);
        void onError(String errorMessage);
    }
}
