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

    public void fetchDepositRequests(Callback callback) {
        db.collection("depositRequests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DepositRequest> depositRequests = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            DepositRequest depositRequest = document.toObject(DepositRequest.class);
                            depositRequests.add(depositRequest);
                        }
                        callback.onSuccess(depositRequests);
                    } else {
                        callback.onError("Failed to fetch deposit requests.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error fetching deposit requests", e);
                    callback.onError(e.getMessage());
                });
    }

    public interface Callback {
        void onSuccess(List<DepositRequest> depositRequests);
        void onError(String errorMessage);
    }
}
