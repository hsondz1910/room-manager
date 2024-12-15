package com.lastterm.finalexam.data.entities;

import java.util.List;
import java.util.Map;

public class Room {
    private String id;
    private String title;
    private String address;
    private double price;
    private double area;
    private List<String> imgUrls;
    private String description;
    private Map<String, String> utilities;
    private boolean isFavorite;
    private boolean isSelected;
    private String ownerId;

    public Room() {}

    public Room(String id, String title, String address, double price, double area, List<String> imgUrls, String description, Map<String, String> utilities, boolean isFavorite, boolean isSelected, String ownerId, String imageUrl) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.imgUrls = imgUrls;
        this.description = description;
        this.utilities = utilities;
        this.isFavorite = isFavorite;
        this.isSelected = isSelected;
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
