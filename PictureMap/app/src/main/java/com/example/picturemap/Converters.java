package com.example.picturemap;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.maps.geometry.LatLng;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {//위치 이동 값인 List<LatLng> 값을 데이터 베이스에 저장하기 위해 List를 스트링 함수로 변환하기 위한 클래스

    @TypeConverter
    public static String fromArrayList(List<LatLng> list) {//List<LatLng>을 스트링 함수로 변환
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
    @TypeConverter
    public static List<LatLng> fromString(String str) {//String으로 변환된 List를 다시 List<LatLng>으로 변환
        Type listType = new TypeToken<ArrayList<LatLng>>() {}.getType();
        return new Gson().fromJson(str, listType);
    }
}
