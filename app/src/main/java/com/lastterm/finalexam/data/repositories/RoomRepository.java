package com.lastterm.finalexam.data.repositories;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lastterm.finalexam.data.entities.RoomFilter;
import com.lastterm.finalexam.data.entities.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
                            room.setId(document.getId());


                            rooms.add(room);
                        }

                        //Set favorite for room which is in user's favorite list
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        db.collection("users").document(userId).collection("favorites")
                                .get()
                                .addOnCompleteListener(com -> {
                                    if (com.isSuccessful() && com.getResult() != null) {
                                        for (QueryDocumentSnapshot document : com.getResult()) {
                                            String roomId = document.getId();
                                            for (Room room : rooms) {
                                                if (room.getId().equals(roomId)) {
                                                    room.setFavorite(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    callback.accept(rooms);
                                });
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
                Room room = document.toObject(Room.class);

                room.setId(document.getId());
                Log.d("Room: ", document.getId());
                rooms.add(room);
            }
            onSuccess.onSuccess(rooms);
        });
    }

    public void addToFavorites(String roomId, String userId, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId)
                .collection("favorites")
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        onSuccess.onSuccess(false);
                    } else {
                        // Lưu phòng vào danh sách yêu thích của người dùng
                        db.collection("users").document(userId)
                                .collection("favorites")
                                .document(roomId)
                                .set(new HashMap<>());  // Lưu một document trống với roomId là ID
                        onSuccess.onSuccess(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching document", e);
                });



    }
    public void isFavorite(String userId, String roomId, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId)
                .collection("favorites")
                .document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean exists = documentSnapshot.exists(); // True if the document exists
                    onSuccess.onSuccess(exists);
                })
                .addOnFailureListener(onFailure);
    }

    public void getFavorites(String userId, OnSuccessListener<List<Room>> onSuccess) {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Room> favoriteRooms = new ArrayList<>();

                    if(querySnapshot.size() == 0) {
                        onSuccess.onSuccess(favoriteRooms);
                        return;
                    }

                    AtomicInteger roomsLoaded = new AtomicInteger(0);
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String roomId = document.getId();
                        getRoomById(roomId, room -> {
                            if(room.getId() == null) {
                            }
                            if (room != null) {
                                room.setFavorite(true);
                                favoriteRooms.add(room);
                            }

                            // Kiểm tra xem đã tải xong tất cả phòng chưa
                            if (roomsLoaded.incrementAndGet() == querySnapshot.size()) {
                                onSuccess.onSuccess(favoriteRooms);
                            }
                        });
                    }

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

    public void getRoomById(String roomId, OnSuccessListener<Room> onSuccess) {
        db.collection("rooms").document(roomId).get().addOnSuccessListener(documentSnapshot -> {
            Room room = documentSnapshot.toObject(Room.class);
            room.setId(roomId);
            onSuccess.onSuccess(room);
        });
    }

}