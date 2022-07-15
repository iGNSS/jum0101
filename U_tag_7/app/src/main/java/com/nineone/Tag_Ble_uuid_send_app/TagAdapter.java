package com.nineone.Tag_Ble_uuid_send_app;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TagAdapter {
    Context context;
    public TagAdapter(Context context) {
        this.context = context;
    }
   private SimpleDateFormat aftertime;
    ArrayList<Tag_item> tag_items = new ArrayList<>();
    public String addItem(String tag_name,int tag_Rssi,byte tag_byte_num1,byte tag_byte_num2,byte tag_byte_rssi) {
        boolean contains = false;
        aftertime = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        Date nowDay = new Date(System.currentTimeMillis());//찍은시간
        String mtime = aftertime.format(nowDay);
        for (Tag_item z_tag_items : tag_items) {
            if (tag_name.equals(z_tag_items.getItem_Name())&&z_tag_items.getItem_Name()!=null) {
                contains = true;
                z_tag_items.setItem_Name(tag_name);
                z_tag_items.setItem_Rssi(tag_Rssi);
                z_tag_items.setItem_byte_num1(tag_byte_num1);
                z_tag_items.setItem_byte_num2(tag_byte_num2);
                z_tag_items.setItem_byte_rssi(tag_byte_rssi);
                z_tag_items.setItem_time(mtime);
                break;
            }
        }
        if (!contains) {
            tag_items.add(new Tag_item(tag_name, tag_Rssi, tag_byte_num1, tag_byte_num2, tag_byte_rssi, mtime));

        }
        Collections.sort(tag_items, cmpAsc1);
        if (tag_items.size()>5) {
            int size = tag_items.size();
            tag_items.subList(5, size).clear();
        }

        String summary = "";            // 사용 안함
        return summary;
    }
    public static Comparator<Tag_item> cmpAsc1 = new Comparator<Tag_item>() {
        @Override
        public int compare(Tag_item o1, Tag_item o2) {
            return o1.getItem_Rssi()-o2.getItem_Rssi();
            //  return o1.getItem_tag_name().compareTo(o2.getItem_tag_name());
        }
    };
    public int getItemCount() {
        if (tag_items != null)
            return tag_items.size();
        else return 0;
    }
    public ArrayList<Tag_item> getItemInmlist() {//data의 갯수를 ViewHolder에 반환 해준다

        return tag_items;
    }
    public List<String> getItemInName() {//data의 갯수를 ViewHolder에 반환 해준다
        List<String> mlist = new ArrayList<>();
        for (Tag_item z_tag_items : tag_items) {
            mlist.add(z_tag_items.getItem_Name());
        }
        return mlist;
    }
    public List<Integer> getItemInRssi() {//data의 갯수를 ViewHolder에 반환 해준다
        List<Integer> mlist = new ArrayList<>();
        for (Tag_item z_tag_items : tag_items) {
            mlist.add(z_tag_items.getItem_Rssi());
        }
        return mlist;
    }
    public String getItemTop5() {//data의 갯수를 ViewHolder에 반환 해준다
        StringBuilder top5 = new StringBuilder();
        if(tag_items.size()>5) {
            for (Tag_item z_tag_items : tag_items) {

                String[] array = z_tag_items.getItem_Name().split("-");
                if (array.length >= 3) {
                    top5.append(array[2]).append("_").append(z_tag_items.getItem_Rssi()).append(" , ");
                }
            }
        }else{
            for (int a=0; a<tag_items.size(); a++) {
                String[] array = tag_items.get(a).getItem_Name().split("-");
                if (array.length >= 3) {
                    top5.append(array[2]).append("_").append(tag_items.get(a).getItem_Rssi()).append(" , ");
                }
            }
        }
        return top5.toString();
    }
}
