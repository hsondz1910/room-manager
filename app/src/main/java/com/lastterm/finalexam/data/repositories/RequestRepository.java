package com.lastterm.finalexam.data.repositories;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.lastterm.finalexam.data.entities.DepositRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestRepository {
    private FirebaseFirestore db;

    public RequestRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchDepositRequests(Callback callback) {
        db.collection("depositRequests")  // Collection depositRequests
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        List<DepositRequest> depositRequests = new ArrayList<>();
                        int totalRequests = querySnapshot.size(); // Biến đếm số yêu cầu đã xử lý
                        final int[] processedRequests = {0}; // Biến đếm các yêu cầu đã hoàn thành

                        for (DocumentSnapshot document : querySnapshot) {
                            DepositRequest depositRequest = document.toObject(DepositRequest.class);
                            String roomId = depositRequest.getRoomId();

                            // Fetch image URL của room từ collection "rooms"
                            fetchRoomImageUrl(roomId, depositRequest, depositRequests, processedRequests, totalRequests, callback);
                        }
                    } else {
                        callback.onError("Error fetching deposit requests.");
                    }
                });
    }

    private void fetchRoomImageUrl(String roomId, DepositRequest depositRequest, List<DepositRequest> depositRequests,
                                   int[] processedRequests, int totalRequests, Callback callback) {
        db.collection("rooms")  // Collection rooms chứa ảnh phòng
                .whereEqualTo("roomId", roomId)
                .get()
                .addOnCompleteListener(roomTask -> {
                    if (roomTask.isSuccessful() && !roomTask.getResult().isEmpty()) {
                        DocumentSnapshot roomDoc = roomTask.getResult().getDocuments().get(0);
                        List<String> imageUrls = (List<String>) roomDoc.get("imgUrls"); // Lấy danh sách URL ảnh của phòng

                        // Set list of image URLs vào DepositRequest
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            depositRequest.setRoomImageUrls(imageUrls);
                        }

                        depositRequests.add(depositRequest);

                        // Đếm số lượng yêu cầu đã xử lý
                        processedRequests[0]++;

                        // Nếu tất cả yêu cầu đã được xử lý, gọi callback
                        if (processedRequests[0] == totalRequests) {
                            callback.onSuccess(depositRequests);
                        }
                    } else {
                        callback.onError("Error fetching room image.");
                    }
                });
    }

    // Callback interface để truyền dữ liệu về UI
    public interface Callback {
        void onSuccess(List<DepositRequest> depositRequests);
        void onError(String errorMessage);
    }
}
