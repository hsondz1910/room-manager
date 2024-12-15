package com.lastterm.finalexam.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lastterm.finalexam.data.entities.DepositRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestRepository {
    private FirebaseFirestore db;

    public RequestRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchDepositRequests(final Callback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("depositRequests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null) {
                        List<DepositRequest> requests = queryDocumentSnapshots.toObjects(DepositRequest.class);
                        callback.onSuccess(requests);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error fetching deposit requests: ", e);
                    callback.onError("Failed to fetch data");
                });
    }

    public interface Callback {
        void onSuccess(List<DepositRequest> depositRequests);
        void onError(String errorMessage);
    }
}
