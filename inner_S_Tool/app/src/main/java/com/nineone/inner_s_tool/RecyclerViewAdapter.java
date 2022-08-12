package com.nineone.inner_s_tool;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Sector_list_item> listData = new ArrayList<>();

    @Override
    public int getItemCount() {
        return listData.size();
    }
    // Item의 클릭 상태를 저장할 array 객체
    private final SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;
    private final Context mContext;
    private final boolean mScanmode;
    private SoundPool soundPool;
    private int soundPlay;

    public RecyclerViewAdapter( Context mContext, boolean mScanmode) {
        //this.recyclerView = recyclerView;
        this.mContext = mContext;
        this.mScanmode=mScanmode;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }
    SimpleDateFormat aftertime;
    private boolean alarm_ON_OFF = false;
    private String BGW_adress_add = "";

    public void BGW_adress_add_reset() {
        BGW_adress_add = "";
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Sector_list_item item = listData.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.tagname.setText(item.getItem_tag_name());

        if(!item.getItem_list_in_information()) {
            viewHolder.tagdata.setVisibility(View.GONE);
            viewHolder.tagbutton.setVisibility(View.VISIBLE);
            if(item.getItem_location_t_f()){
                viewHolder.linearbackcolorlayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue));
            }else{
                viewHolder.linearbackcolorlayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            }
        }else{
            viewHolder.tagdata.setVisibility(View.VISIBLE);
            viewHolder.tagbutton.setVisibility(View.GONE);
            viewHolder.tagdata.setText(item.getItem_tag_data());
        }
    }

    public String update(String mname, String mdata, boolean m_location_boolean, boolean list_t_f) {

        boolean contains = false;
        for (Sector_list_item device : listData) {
            if (mname.equals(device.getItem_tag_name())) {
                contains = true;
                device.setItem_tag_name(mname);
                device.setItem_tag_data(mdata);
                device.setItem_location_t_f(m_location_boolean);
                device.setItem_list_in_information(list_t_f);
                // update
                break;
            }
        }
        if (!contains) {
            listData.add(new Sector_list_item(mname, mdata, m_location_boolean, list_t_f));
        }
       if(!testboolean){
           testboolean=true;
            list_notifyDataSetChanged();

       }
        return "";
    }
    boolean testboolean = false;
    private void list_notifyDataSetChanged(){
        Runnable runnable10 = new Runnable() {
            @Override
            public void run() {
               //notifyDataSetChanged();
                testboolean=false;
                notifyItemRangeChanged(0, item_Count(), null);
            }
        };
        listcange_handler.postDelayed(runnable10, 0);
    }
    private final MyHandler listcange_handler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<RecyclerViewAdapter> mActivity;

        public MyHandler(RecyclerViewAdapter activity) {
            mActivity = new WeakReference<RecyclerViewAdapter>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            RecyclerViewAdapter activity = mActivity.get();
            // ...
        }
    }

    public void item_Clear(){
        listData.clear();
    }
    public int item_Count(){
        return listData.size();

    }
    private OnItemClickListener mListener = null; //어뎁터 클릭기능 추가 리스너
    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public ArrayList<Sector_list_item> sector_list_items(){
        return listData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tagname;
        private final TextView tagdata;
        private final Button tagbutton;
        private LinearLayout linearbackcolorlayout;
        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            tagname=(TextView)itemView.findViewById(R.id.device_name);
            tagdata=(TextView)itemView.findViewById(R.id.device_data);
            tagbutton=(Button)itemView.findViewById(R.id.device_button);
            linearbackcolorlayout =(LinearLayout) itemView.findViewById(R.id.linearbackcolorlayout);
            tagbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        if(mListener !=null){
                            mListener.onItemClick(view,pos);
                        }
                    }
                }
            });
        }

        /*void onBind(Z_list_item z_list_item) {
            tagname.setText(z_list_item.getItem_tag_name());
        }*/
    }
    public void item_noti(){
        //listData.clear(); //here items is an ArrayList populating the RecyclerView
        if (!listData.isEmpty()) {

            listData.clear(); //The list for update recycle view
        }
        listData.size();
        notifyDataSetChanged();
    }



}
