package com.lastterm.finalexam.data.repositories;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.data.entities.DepositRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestRepository {
    private final FirebaseFirestore db;

    public RequestRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchDepositRequests(Callback callback) {
        db.collection("depositRequests")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DepositRequest> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DepositRequest request = document.toObject(DepositRequest.class);
                        requests.add(request);
                    }
                    callback.onSuccess(requests);
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error fetching deposit requests: ", e);
                    callback.onError("Error fetching deposit requests");
                });
    }

    public void fetchRoomTitle(String roomId, RoomCallback callback) {
        db.collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getString("title"));
                    } else {
                        callback.onError("Room not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error fetching room title: ", e);
                    callback.onError(e.getMessage());
                });
    }

    public void fetchUserName(String userId, UserCallback callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onSuccess(documentSnapshot.getString("username"));
                    } else {
                        callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error fetching user name: ", e);
                    callback.onError(e.getMessage());
                });
    }

    public void updateRequestStatus(String requestId, String status, UpdateCallback callback) {
        db.collection("depositRequests")
                .document(requestId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    Log.e("RequestRepository", "Error updating request status: ", e);
                    callback.onError(e.getMessage());
                });
    }

    public interface RoomCallback {
        void onSuccess(String roomTitle);

        void onError(String errorMessage);
    }

    public interface UserCallback {
        void onSuccess(String userName);

        void onError(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public interface Callback {
        void onSuccess(List<DepositRequest> depositRequests);
        void onError(String errorMessage);
    }
}
