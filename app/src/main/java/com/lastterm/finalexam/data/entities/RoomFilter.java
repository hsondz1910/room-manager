package com.lastterm.finalexam.data.entities;

public class RoomFilter {
    private String search;
    private long maxPrice;
    private long minPrice;
    private long area;
    private String location;


    public RoomFilter() {
        this.search = "";
        this.minPrice = 0;
        this.maxPrice = 0;
        this.area = 0;
        this.location = "";
    }

    public RoomFilter(String search, long minPrice, long maxPrice, long area, String location) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.area = area;
        this.location = location;
    }

    public long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public long getArea() {
        return area;
    }

    public void setArea(long area) {
        this.area = area;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
