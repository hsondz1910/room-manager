package com.lastterm.finalexam.model;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;
import java.util.Map;
import com.bumptech.glide.Glide;
import com.lastterm.finalexam.R;

public class Room {
    private String id;
    private String title;
    private String address;
    private double price;
    private double area;
    private List<String> images;
    private String description;
    private Map<String, String> utilities;
    private boolean isFavorite;
    private boolean isSelected;

    public Room(String id, String title, String address, double price, double area, List<String> images, String description, Map<String, String> utilities, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.images = images;
        this.description = description;
        this.utilities = utilities;
        this.isFavorite = isFavorite;
    }

    public Room(String id, String title, String address, double price, double area, List<String> images, String description, Map<String, String> utilities, boolean isFavorite, boolean isSelected) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.images = images;
        this.description = description;
        this.utilities = utilities;
        this.isFavorite = isFavorite;
        this.isSelected = false;
    }

    public Room() {
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getUtilities() {
        return utilities;
    }

    public void setUtilities(Map<String, String> utilities) {
        this.utilities = utilities;
    }

    public void loadImageWithGlide(Context context, ImageView imageView) {
        if (images != null && !images.isEmpty()) {
            String imageUrl = images.get(0);  // Lấy URL của hình ảnh đầu tiên

            // Dùng Glide để tải ảnh và hiển thị vào ImageView
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)  // Hình ảnh placeholder trong khi tải
                    .error(R.drawable.error_image)  // Hình ảnh hiển thị khi có lỗi
                    .into(imageView);
        }
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getFirstImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);  // Lấy URL của hình ảnh đầu tiên
        }
        return null;
    }

}
