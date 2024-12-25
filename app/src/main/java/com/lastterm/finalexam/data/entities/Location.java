package com.lastterm.finalexam.data.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Location{
    private String city;
    private String district;

    public Location(String city, String district) {
        this.city = city;
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public boolean isEmpty(){
        if(city == "" && district == ""){
            return true;
        }
        return false;
    }

    public static Map<String, List<String>> ListCityanDistrict() {
        final Map<String, List<String>> locations = new HashMap<String, List<String>>() {{
            put("", Arrays.asList());
            put("Hà Nội", Arrays.asList(
                    "", "Ba Đình", "Hoàn Kiếm", "Tây Hồ", "Long Biên", "Cầu Giấy",
                    "Đống Đa", "Hai Bà Trưng", "Hoàng Mai", "Thanh Xuân",
                    "Sóc Sơn", "Đông Anh", "Gia Lâm", "Nam Từ Liêm",
                    "Thanh Trì", "Bắc Từ Liêm", "Mê Linh",
                    "Hà Đông", "Sơn Tây", "Ba Vì", "Phúc Thọ",
                    "Đan Phượng", "Hoài Đức", "Quốc Oai", "Thạch Thất",
                    "Chương Mỹ", "Thanh Oai", "Thường Tín", "Phú Xuyên",
                    "Ứng Hòa", "Mỹ Đức"
            ));
            put("Hồ Chí Minh", Arrays.asList(
                    "", "Quận 1", "Quận 3", "Quận 4", "Quận 5", "Quận 6",
                    "Quận 7", "Quận 8", "Quận 10", "Quận 11",
                    "Quận 12", "Bình Thạnh", "Gò Vấp", "Phú Nhuận",
                    "Tân Bình", "Tân Phú", "Bình Tân", "Thủ Đức",
                    "Bình Chánh", "Hóc Môn", "Củ Chi", "Nhà Bè",
                    "Cần Giờ"
            ));
            put("Đà Nẵng", Arrays.asList(
                    "", "Hải Châu", "Thanh Khê", "Sơn Trà", "Ngũ Hành Sơn",
                    "Liên Chiểu", "Cẩm Lệ", "Hòa Vang", "Hoàng Sa"
            ));
        }};
        return locations;

    }
}
