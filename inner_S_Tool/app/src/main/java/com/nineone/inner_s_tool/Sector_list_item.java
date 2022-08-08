package com.nineone.inner_s_tool;

public class Sector_list_item {
    private String item_tag_name;
    private String item_tag_data;
    private boolean item_location_t_f;
    public Sector_list_item(String item_tag_name, String item_tag_data,boolean item_location_t_f){
        this.item_tag_name = item_tag_name;
        this.item_tag_data = item_tag_data;
        this.item_location_t_f =item_location_t_f;
    }

    public String getItem_tag_name() { return item_tag_name; }
    public void setItem_tag_name(String item_tag_name) { this.item_tag_name = item_tag_name; }

    public String getItem_tag_data() { return item_tag_data; }
    public void setItem_tag_data(String item_tag_data) { this.item_tag_data = item_tag_data; }

    public boolean getItem_location_t_f() { return item_location_t_f; }

    public void setItem_location_t_f(boolean item_location_t_f) { this.item_location_t_f = item_location_t_f; }
}
