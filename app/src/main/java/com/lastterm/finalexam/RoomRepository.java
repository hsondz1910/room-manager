package com.lastterm.finalexam;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.model.RoomFilter;
import com.lastterm.finalexam.model.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class RoomRepository {
    private FirebaseFirestore db;

    public RoomRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void addRoom(Room room, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("rooms").document()
                .set(room)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getAllRooms(Consumer<List<Room>> callback) {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> rooms = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            rooms.add(room);
                        }
                        callback.accept(rooms);
                    } else {
                        callback.accept(new ArrayList<>());
                    }
                });
    }

    public void searchRooms(RoomFilter filter, OnSuccessListener<List<Room>> onSuccess) {
        Query query = db.collection("rooms");

        if (filter.getMaxPrice() > 0) {
            query = query.whereLessThanOrEqualTo("price", filter.getMaxPrice());
        }

        if (filter.getMinArea() > 0) {
            query = query.whereGreaterThanOrEqualTo("area", filter.getMinArea());
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            List<Room> rooms = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot) {
                rooms.add(document.toObject(Room.class));
            }
            onSuccess.onSuccess(rooms);
        });
    }

    public void addToFavorites(String roomId, String userId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Lưu phòng vào danh sách yêu thích của người dùng
        db.collection("users").document(userId)
                .collection("favorites")
                .document(roomId)
                .set(new HashMap<>())  // Lưu một document trống với roomId là ID
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getFavorites(String userId, OnSuccessListener<List<Room>> onSuccess) {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Room> favoriteRooms = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        favoriteRooms.add(document.toObject(Room.class));
                    }
                    onSuccess.onSuccess(favoriteRooms);
                });
    }

    public void removeFromFavorites(String roomId, String userId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId)
                .collection("favorites")
                .document(roomId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

}