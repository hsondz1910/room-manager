package com.lastterm.finalexam.data.repositories;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lastterm.finalexam.data.entities.ChatRoom;
import com.lastterm.finalexam.data.entities.MessageClass;
import com.lastterm.finalexam.data.entities.RoomFilter;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.uComment;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class RoomRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public RoomRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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

    public void getNameByUserID(String userId, Consumer<String> callback) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String name = documentSnapshot.getString("name");
            callback.accept(name);
        });
    }

    public void getCommentByRoomId(String roomId, String rate, OnSuccessListener<List<uComment>> onSuccess) {
        db.collection("comments").document(roomId).collection(rate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(docs -> {
                    if (docs.isSuccessful()) {
                        List<uComment> comments = new ArrayList<>();
                        List<Task<Void>> tasks = new ArrayList<>(); // Track asynchronous tasks

                        for (QueryDocumentSnapshot doc : docs.getResult()) {
                            uComment comment = doc.toObject(uComment.class);

                            // Task for fetching user names
                            Task<Void> task = db.collection("users")
                                    .document(comment.getUserId())
                                    .get()
                                    .continueWith(taskSnapshot -> {
                                        if (taskSnapshot.isSuccessful() && taskSnapshot.getResult() != null) {
                                            String name = taskSnapshot.getResult().getString("fullName");
                                            comment.setName(name); // Set the name
                                        }
                                        comments.add(comment); // Add comment to the list after setting the name
                                        return null;
                                    });
                            tasks.add(task); // Add the task to the list
                        }

                        // Wait for all tasks to complete
                        Tasks.whenAll(tasks)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "All tasks completed. Returning comments.");
                                    onSuccess.onSuccess(comments); // Pass the completed list
                                })
                                .addOnFailureListener(e -> Log.d("Firestore", "Error resolving names.", e));
                    } else {
                        Log.d("Firestore", "Error getting documents.", docs.getException());
                    }
                });
    }

    public void addComment(String roomId, String comment, String rate, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {

        String userID = auth.getUid();
        if(userID != null){
            Log.d("TAG", "addComment: ");
            db.collection("users").document(userID).get().addOnCompleteListener((snap) ->{

                if(snap.isSuccessful()){
                    String userName = snap.getResult().getString("name");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    uComment cm = new uComment("", userID, userName, rate, date, comment);
                    Log.d("TAG1", "addComment: ");
                    db.collection("comments").document(roomId).collection(rate)
                            .add(cm.toJson())
                            .addOnSuccessListener(doc -> {
                                onSuccess.onSuccess(doc.getId());
                            })
                            .addOnFailureListener(onFailure);
                }
            });

        }else {
            Log.e("TAG", "User ID is null");
            onFailure.onFailure(new Exception("User ID is null"));
        }

    }

    public boolean isCurrentUser(String id){
        return auth.getUid().equals(id);
    }

    public void creatChatRoom(String userIDSent, String userIDReceiver, OnSuccessListener<ChatRoom> onSuccess, OnFailureListener onFailure){
        ChatRoom chatRoom = new ChatRoom(userIDSent, userIDReceiver);
        try {
            db.collection("chatRooms").add(chatRoom).addOnSuccessListener(documentReference -> {
                chatRoom.setId(documentReference.getId());
                onSuccess.onSuccess(chatRoom);
            }).addOnFailureListener(onFailure);
        } catch (Exception e) {
            Log.d("Create chat room: ", e.getMessage());
        }
    }

    public void  getChatRoomWithID(String roomId, OnSuccessListener<ChatRoom> onSuccess){
        try {
            db.collection("chatRooms").document(roomId).get().addOnCompleteListener(documentSnapshot -> {
                    DocumentSnapshot doc = documentSnapshot.getResult();
                    ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                    chatRoom.setId(doc.getId());
                    onSuccess.onSuccess(chatRoom);
            });
        }catch (Exception e) {
            Log.d("Get Room by id: ", e.getMessage());
        }

    }

    public void findChatRoom(String userIDSent, String userIDReceiver, OnSuccessListener<ChatRoom> onSuccess, OnFailureListener onFailure){
        db.collection("chatRooms")
                .whereArrayContains("users", userIDSent)
                .whereArrayContains("users", userIDReceiver)
                .get()
                .addOnCompleteListener(snapshot -> {
                    if (snapshot.isSuccessful() && snapshot.getResult().size() > 0) {
                        QuerySnapshot doc = snapshot.getResult();
                        ChatRoom chatRoom = doc.getDocuments().get(0).toObject(ChatRoom.class);
                        onSuccess.onSuccess(chatRoom);
                    } else {
                        creatChatRoom(userIDSent, userIDReceiver, (chatRoom) -> {onSuccess.onSuccess(chatRoom);}, (e) -> {onFailure.onFailure(e);});
                    }
                });
    }
    public void getAllChatRoomSupport(OnSuccessListener<List<ChatRoom>> onSuccess, OnFailureListener onFailure){
        try {
            db.collection("chatRooms").whereEqualTo("users", "Support").get().addOnCompleteListener(snapshot -> {
                if(snapshot.isSuccessful()){
                    List<ChatRoom> chatRooms = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : snapshot.getResult()){
                        ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                        chatRoom.setId(doc.getId());
                        chatRooms.add(chatRoom);
                    }
                    onSuccess.onSuccess(chatRooms);
                }
            });
        } catch (Exception e) {
            Log.d("getAllRoomSupport", "getAllChatRoomSupport: " + e.getMessage());
        }
    }

    public void getAllChatRoomOfUser(String userID, OnSuccessListener<List<ChatRoom>> onSuccess, OnFailureListener onFailure){
        try {
            db.collection("chatRooms").whereArrayContains("users", userID).get().addOnCompleteListener(snapshot -> {
                if(snapshot.isSuccessful()){
                    List<ChatRoom> chatRooms = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : snapshot.getResult()){
                        ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                        chatRoom.setId(doc.getId());
                        chatRooms.add(chatRoom);
                    }
                    onSuccess.onSuccess(chatRooms);
                }
            });
        } catch (Exception e) {
            Log.d("getAllChatRoomOfUser", "getAllChatRoomOfUser: " + e.getMessage());
        }
    }

    public void sentMessage(String roomId, MessageClass message, OnSuccessListener<MessageClass> onSuccess, OnFailureListener onFailure){
        try {
            db.collection("chatRooms").document(roomId).collection("messages").add(message).addOnCompleteListener((doc) -> {
                if(doc.isSuccessful()){
                    message.setId(doc.getResult().getId());
                    onSuccess.onSuccess(message);
                }
            });
        } catch (Exception e) {
            Log.d("sentMessage", "sentMessage: " + e.getMessage());
        }
    }

    public void listenToMessages(String roomId, OnSuccessListener<List<MessageClass>> onSuccess, OnFailureListener onFailure) {
        db.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .orderBy("date")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        onFailure.onFailure(e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<MessageClass> messageList = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            MessageClass message = document.toObject(MessageClass.class);
                            if (message != null) {
                                messageList.add(message);
                            }
                        }
                        onSuccess.onSuccess(messageList);
                    }
                });
    }

}