package com.lastterm.finalexam.data.entities;

public class RoomFilter {
    private double maxPrice;
    private double minArea;

    public RoomFilter() {
    }

    public RoomFilter(double maxPrice, double minArea) {
        this.maxPrice = maxPrice;
        this.minArea = minArea;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getMinArea() {
        return minArea;
    }

    public void setMinArea(double minArea) {
        this.minArea = minArea;
    }
}
