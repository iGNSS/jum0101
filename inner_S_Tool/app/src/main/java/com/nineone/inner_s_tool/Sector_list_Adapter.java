package com.nineone.inner_s_tool;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Sector_list_Adapter extends RecyclerView.Adapter<Sector_list_Adapter.ViewHolder> {
    private final Context mContext;
    private final ArrayList<Sector_list_item> listData = new ArrayList<>();
    public Sector_list_Adapter(Context mContext) {
        //this.recyclerView = recyclerView;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//ViewHolder 객체를 생성

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        listData.add(new Sector_list_item("asd","asd",false,false));
        return new ViewHolder(view);

    }
    public void item_noti(){
        //listData.clear(); //here items is an ArrayList populating the RecyclerView
        if (!listData.isEmpty()) {

            listData.clear(); //The list for update recycle view
        }
        listData.size();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {// ViewHolder 에 data 를 넣는 작업 수행
        final Sector_list_item item = listData.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tagname.setText(item.getItem_tag_name());
        viewHolder.tagdata.setText(item.getItem_tag_data());
    }
    boolean testboolean = false;
    public String update(String mtagname, String mtagdata) {
        listData.add(new Sector_list_item(mtagname, mtagdata,false,false));
        if(!testboolean){
            testboolean=true;
            list_notifyDataSetChanged();

        }
        return "";
    };
    public int item_Count(){
        return listData.size();

    }
    private void list_notifyDataSetChanged(){
        Runnable runnable10 = new Runnable() {
            @Override
            public void run() {
                //notifyDataSetChanged();
                testboolean=false;
            notifyDataSetChanged();
            }
        };
        listcange_handler.postDelayed(runnable10, 0);
    }
    private final MyHandler listcange_handler = new MyHandler(this);
    private static class MyHandler extends Handler {
        private final WeakReference<Sector_list_Adapter> mActivity;

        public MyHandler(Sector_list_Adapter activity) {
            mActivity = new WeakReference<Sector_list_Adapter>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            Sector_list_Adapter activity = mActivity.get();
            // ...
        }
    }
    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tagname;
        private final TextView tagdata;
        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            tagname=(TextView)itemView.findViewById(R.id.device_name);
            tagdata=(TextView)itemView.findViewById(R.id.device_data);
        }

        /*void onBind(Z_list_item z_list_item) {
            tagname.setText(z_list_item.getItem_tag_name());
        }*/
    }
}