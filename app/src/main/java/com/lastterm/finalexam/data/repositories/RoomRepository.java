package com.lastterm.finalexam.data.repositories;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lastterm.finalexam.data.entities.Appointment;
import com.lastterm.finalexam.data.entities.ChatRoom;
import com.lastterm.finalexam.data.entities.MessageClass;
import com.lastterm.finalexam.data.entities.RoomFilter;
import com.lastterm.finalexam.data.entities.Room;
import com.lastterm.finalexam.data.entities.uComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RoomRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StorageReference storage;

    public RoomRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
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
            Log.d("TAG", "searchRooms: " + filter.getMaxPrice());
            query = query.whereLessThanOrEqualTo("price", filter.getMaxPrice());
        }

        if (filter.getMinPrice() > 0) {
            Log.d("TAG", "searchRoomsmin: " + filter.getMinPrice());
            query = query.whereGreaterThanOrEqualTo("price", filter.getMinPrice());
        }

        query.get().addOnSuccessListener(querySnapshot -> {
            List<Room> rooms = new ArrayList<>();
            Log.d("TAG", "searchRooms: " + querySnapshot.size());
            for (QueryDocumentSnapshot document : querySnapshot) {
                Room room = document.toObject(Room.class);
                if(filter.getArea() > 0){
                    Log.d("TAG", "getArea: " );
                    if(room.getArea() >= filter.getArea()){
                        room.setId(document.getId());
                        rooms.add(room);
                    }
                }

                if(!filter.getSearch().isEmpty()){
                    Log.d("TAG", "getSearch: " );
                    if(!room.getTitle().toLowerCase().contains(filter.getSearch().toLowerCase())){
                        if(!room.getDescription().toLowerCase().contains(filter.getSearch().toLowerCase())){
                            continue;
                        }
                    }
                    room.setId(document.getId());
                    rooms.add(room);
                }
                if(!filter.getLocation().isEmpty()){
                    Log.d("TAG", "getLocation: " );
                    if(!room.getAddress().toLowerCase().contains(filter.getLocation().toLowerCase())){
                        continue;
                    }
                    room.setId(document.getId());
                    rooms.add(room);
                }
                if(filter.getArea() == 0 && filter.getSearch().isEmpty() && filter.getLocation().isEmpty()){
                    room.setId(document.getId());
                    rooms.add(room);
                }

            }
            rooms = new ArrayList<>(new HashSet<Room>(rooms));
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
                        db.collection("users").document(userId)
                                .collection("favorites")
                                .document(roomId)
                                .set(new HashMap<>());
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

                    if (querySnapshot.size() == 0) {
                        onSuccess.onSuccess(favoriteRooms);
                        return;
                    }

                    AtomicInteger roomsLoaded = new AtomicInteger(0);
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String roomId = document.getId();
                        getRoomById(roomId, room -> {
                            if (room != null) {
                                if (room.getId() == null) {
                                }
                                if (room != null) {
                                    room.setFavorite(true);
                                    favoriteRooms.add(room);
                                }
                            }else {
                                Room r = new Room();
                                r.setId(roomId);
                                favoriteRooms.add(r);
                            }

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
        try{
            db.collection("rooms").document(roomId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Room room = documentSnapshot.toObject(Room.class);
                    room.setId(roomId);
                    onSuccess.onSuccess(room);
                }else {
                    onSuccess.onSuccess(null);
                }
            });
        } catch (Exception e) {
            onSuccess.onSuccess(null);
        }
    }

    public void getNameByUserID(String userId, Consumer<String> callback) {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String name = documentSnapshot.getString("fullName");
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
        if (userID != null) {
            db.collection("users").document(userID).get().addOnCompleteListener((snap) -> {

                if (snap.isSuccessful()) {
                    String userName = snap.getResult().getString("name");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    uComment cm = new uComment("", userID, userName, rate, date, comment);
                    db.collection("comments").document(roomId).collection(rate)
                            .add(cm.toJson())
                            .addOnSuccessListener(doc -> {
                                onSuccess.onSuccess(doc.getId());
                            })
                            .addOnFailureListener(onFailure);
                }
            });

        } else {
            Log.e("TAG", "User ID is null");
            onFailure.onFailure(new Exception("User ID is null"));
        }

    }

    public boolean isCurrentUser(String id) {
        return auth.getUid().equals(id);
    }

    public String getCurrentUser(){
        return auth.getUid();
    }

    public void creatChatRoom(String roomID,String userIDSent, String userIDReceiver, OnSuccessListener<ChatRoom> onSuccess, OnFailureListener onFailure) {
        ChatRoom chatRoom = new ChatRoom(roomID, userIDSent, userIDReceiver);
        try {
            db.collection("chatRooms").add(chatRoom).addOnSuccessListener(documentReference -> {
                chatRoom.setId(documentReference.getId());
                onSuccess.onSuccess(chatRoom);
            }).addOnFailureListener(onFailure);
        } catch (Exception e) {
            Log.d("Create chat room: ", e.getMessage());
        }
    }

    public void getChatRoomWithID(String roomId, OnSuccessListener<ChatRoom> onSuccess) {
        try {
            db.collection("chatRooms").document(roomId).get().addOnCompleteListener(documentSnapshot -> {
                DocumentSnapshot doc = documentSnapshot.getResult();
                ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                chatRoom.setId(doc.getId());
                onSuccess.onSuccess(chatRoom);
            });
        } catch (Exception e) {
            Log.d("Get Room by id: ", e.getMessage());
        }

    }

    public void findChatRoom(String chatRoomID, OnSuccessListener<ChatRoom> onSuccess, OnFailureListener onFailure) {
        Log.d("findChatRoom", "findChatRoom: " + chatRoomID);
        db.collection("chatRooms")
                .document(chatRoomID)
                .get()
                .addOnCompleteListener(snapshot -> {
                    Log.d("findChatRoom", "findChatRoom: com" );
                    if(snapshot.isSuccessful()) {
                        if (snapshot.getResult().exists()) {
                            DocumentSnapshot doc = snapshot.getResult();
                            ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                            chatRoom.setId(doc.getId());
                            db.collection("chatRooms")
                                    .document(chatRoomID)
                                    .collection("messages")
                                    .orderBy("date")
                                    .get()
                                    .addOnCompleteListener(messageSnapshot -> {
                                        if (messageSnapshot.isSuccessful()) {
                                            List<MessageClass> messages = new ArrayList<>();
                                            for (DocumentSnapshot messageDoc : messageSnapshot.getResult()) {
                                                MessageClass message = messageDoc.toObject(MessageClass.class);
                                                message.setId(messageDoc.getId());
                                                messages.add(message);
                                            }
                                            Log.d("findChatRoom", "findChatRoom: " + messages.size());
                                            chatRoom.setMessages(messages);
                                            onSuccess.onSuccess(chatRoom);
                                        }
                                    });
                        }
                    }
                    else {
                        Exception exception = snapshot.getException();
                        Log.e("TAG", "Query failed: " + exception.getMessage(), exception);
                    }

                });
    }

    public void getAllChatRoomSupport(OnSuccessListener<List<ChatRoom>> onSuccess, OnFailureListener onFailure) {
        try {
            db.collection("chatRooms").whereEqualTo("users", "Support").get().addOnCompleteListener(snapshot -> {
                if (snapshot.isSuccessful()) {
                    List<ChatRoom> chatRooms = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot.getResult()) {
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
    public void isContainRoom(String roomID, OnSuccessListener<Boolean> onSuccess, OnFailureListener onFailure){
        try {
            db.collection("chatRooms").whereEqualTo("roomId", roomID).get().addOnCompleteListener(snapshot -> {
                if (snapshot.isSuccessful()) {
                    onSuccess.onSuccess(snapshot.getResult().size() > 0);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void getAllChatRoomOfUser(String userID, OnSuccessListener<List<ChatRoom>> onSuccess, OnFailureListener onFailure) {
        try {
            db.collection("chatRooms").whereArrayContains("users", userID).get().addOnCompleteListener(snapshot -> {
                if (snapshot.isSuccessful()) {
                    List<ChatRoom> chatRooms = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot.getResult()) {
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

    public void sendMessage(Context context,String roomId, String msg, Uri img, OnSuccessListener<MessageClass> onSuccess, OnFailureListener onFailure) {
        try {
            MessageClass message = new MessageClass(msg, auth.getUid(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "", false);
            if (img != null) {
                uploadImageImgBBForChat(context,roomId, img, (url) -> {
                    message.setImg(url);
                    db.collection("chatRooms").document(roomId).collection("messages").add(message).addOnCompleteListener((doc) -> {
                        if (doc.isSuccessful()) {
                            message.setId(doc.getResult().getId());
                            onSuccess.onSuccess(message);
                        }
                    });
                });
            }
            else {
                db.collection("chatRooms").document(roomId).collection("messages").add(message).addOnCompleteListener((doc) -> {
                    if (doc.isSuccessful()) {
                        message.setId(doc.getResult().getId());
                        onSuccess.onSuccess(message);
                    }
                });
            }
        } catch (Exception e) {
            Log.d("sentMessage", "sentMessage: " + e.getMessage());
        }
    }

    public void setMessagesRead(String roomId, String senderID, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Log.d("TAG", "setMessagesRead: " + senderID);
        db.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .whereEqualTo("senderID", senderID)
                .get()
                .addOnCompleteListener(querySnapshot -> {
                    Log.d("TAG", "setMessagesRead: " + querySnapshot.getResult().isEmpty());
                    if (!querySnapshot.getResult().isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getResult()) {
                            document.getReference().update("read", true)
                                    .addOnCompleteListener((c) -> {Log.d("TAG", "setMessagesRead: " + c.getResult());});
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting documents", e);
                });

    }

    public void listenToMessages(String roomId, OnSuccessListener<List<MessageClass>> onSuccess, OnFailureListener onFailure) {
        db.collection("chatRooms")
                .document(roomId)
                .collection("messages")
                .orderBy("date")
                .addSnapshotListener((querySnapshot, e) -> {

                    // Handle errors
                    if (e != null) {
                        onFailure.onFailure(e);
                        return;
                    }

                    if(!querySnapshot.getDocumentChanges().isEmpty()){
                        // Check if querySnapshot is not null and contains documents
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            List<MessageClass> messageList = new ArrayList<>();
                            // Loop through documents and convert to MessageClass
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                MessageClass message = doc.toObject(MessageClass.class);
                                if (message != null) {
//                                    Boolean isRead = doc.get("read", Boolean.class);
//                                    message.setIsRead(isRead.booleanValue());
                                    Log.d("TAG", "listenToMessages: " + message.isRead());
                                    message.setId(doc.getId());
                                    messageList.add(message);
                                }


                            }
                            // If messages are found, pass them to the success listener
                            onSuccess.onSuccess(messageList);
                        } else {
                            // In case there's no data, pass an empty list or handle as needed
                            onSuccess.onSuccess(null);
                        }
                    }else {
                        onSuccess.onSuccess(new ArrayList<>());
                    }

                });
    }


    public void removeChatRoom(String roomId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure){
        db.collection("chatRooms").document(roomId).delete().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }


    //upload image to storage in firebase
    public void uploadImageImgBBForChat(Context context,String roomID, Uri imageUri, OnSuccessListener<String> onSuccess) {
        if (imageUri != null) {
            try {
                // Convert URI to byte array
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                byte[] imageData = getBytes(inputStream);

                // Prepare the request body for the POST request
                MultipartBody.Builder requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", roomID + ".jpg", RequestBody.create(imageData, MediaType.parse("image/*")));

                // Create OkHttpClient to send the request
                OkHttpClient client = new OkHttpClient();

                // Build the request
                Request request = new Request.Builder()
                        .url("https://api.imgbb.com/1/upload?key=4c802577ba0af2478ab9a4d0079df219") // ImgBB API URL
                        .post(requestBody.build())
                        .build();

                // Send the request asynchronously
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("ImageUploader", "Failed to upload image", e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // Parse the response JSON to get the image URL
                            String responseBody = response.body().string();
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                String imageUrl = jsonResponse.getJSONObject("data").getString("url");

                                // Log and return the image URL to onSuccess
                                Log.d("ImageUploader", "Image uploaded to ImgBB. URL: " + imageUrl);
                                onSuccess.onSuccess(imageUrl); // Return the image URL
                            } catch (JSONException e) {
                                Log.e("ImageUploader", "Error parsing ImgBB response", e);
                            }
                        } else {
                            Log.e("ImageUploader", "Upload failed. Response: " + response.message());
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("ImageUploader", "Error uploading image: " + e.getMessage());
            }
        } else {
            Log.e("ImageUploader", "Image URI is null");
        }
    }

    // Helper method to convert InputStream to byte array
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }
    public void getAppointment(String chatRoomID, OnSuccessListener<ArrayList<Appointment>> onSuccess) {
        Log.d("TAG", "getAppointment: " + chatRoomID);
        db.collection("appointments")
                .whereEqualTo("chatID", chatRoomID)
                .get()
                .addOnCompleteListener(snapshot -> {
                    if(snapshot.isSuccessful()){
                        if(!snapshot.getResult().isEmpty()) {
                            QuerySnapshot doc = snapshot.getResult();
                            ArrayList<Appointment> appointments = new ArrayList<>();
                            for (DocumentSnapshot document : doc.getDocuments()) {
                                Appointment appointment = document.toObject(Appointment.class);
                                appointment.setId(document.getId());
                                appointments.add(appointment);
                            }
                            onSuccess.onSuccess(appointments);
                        }else onSuccess.onSuccess(new ArrayList<>());
                    }
                    else onSuccess.onSuccess(new ArrayList<>());

                });
    }

    public void addAppointment(Appointment appointment, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        db.collection("appointments").add(appointment.toMap())
                .addOnCompleteListener((c) -> onSuccess.onSuccess("success"))
                .addOnFailureListener(onFailure);
    }

    public void getAllAppointments(OnSuccessListener<List<Appointment>> onSuccess, OnFailureListener onFailure) {
        try {
            db.collection("chatRooms").whereArrayContains("users", getCurrentUser()).get().addOnCompleteListener(snapshot -> {
                if (snapshot.isSuccessful()) {
                    List<Appointment> appointments = new ArrayList<>();

                    List<Task<Void>> tasks = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot.getResult()) {
                        Task<Void> task = db.collection("appointments")
                                .whereEqualTo("chatID", doc.getId())
                                .get()
                                .continueWith(taskSnapshot -> {
                                    if (taskSnapshot.isSuccessful() && !taskSnapshot.getResult().isEmpty()) {
                                        for (DocumentSnapshot document : taskSnapshot.getResult()) {
                                            Appointment appointment = document.toObject(Appointment.class);
                                            appointment.setId(document.getId());
                                            appointments.add(appointment);
                                        }
                                    }
                                    return null;
                                });
                        tasks.add(task);
                    }

                    Tasks.whenAll(tasks)
                            .addOnSuccessListener(aVoid -> {
                                onSuccess.onSuccess(appointments);
                            })
                            .addOnFailureListener(e -> Log.d("Firestore", "Error :", e));
                }
            });
        } catch (Exception e) {
            Log.d("Firestore", "Error: " + e.getMessage());
        }
    }

    public void updateAppointment(Appointment appointment, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("appointments").document(appointment.getId()).set(appointment.toMap())
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
    public void deleteAppointment(Appointment appointment, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("appointments").document(appointment.getId()).delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}