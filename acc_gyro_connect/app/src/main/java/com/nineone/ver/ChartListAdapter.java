package com.nineone.ver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class ChartListAdapter extends RecyclerView.Adapter<ChartListAdapter.ViewHolder> {//걷기기록 리사이클뷰 어댑터
    // private final LayoutInflater mInflater;
    Context mContext;
    Activity activity;
    ArrayList<ChartListData> chartListData = new ArrayList<>();
    boolean isEnable = false;
    boolean isSelectall = false;
    ArrayList<ChartListData> checkboxclickList = new ArrayList<ChartListData>();
    CheckBox checkBox;
    private boolean activate2=false;

    public ChartListAdapter(Activity activity, ArrayList<ChartListData> ListData,CheckBox checkBox) {
        this.activity = activity;
        this.chartListData = ListData;
        this.checkBox=checkBox;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {//ViewHolder 객체를 생성
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_list_data, parent, false);//list data를 받아온다
           return new ViewHolder(itemView);//뷰홀더에 담아 리턴
    }

    boolean aaa = false;

    @Override
    public void onBindViewHolder(@NonNull ChartListAdapter.ViewHolder viewHolder, int i) {// ViewHolder 에 data 를 넣는 작업 수행
        ChartListData charts = chartListData.get(i);
        if (chartListData != null) {
            final int pos = i;
            viewHolder.txtday.setText(charts.getName());
            viewHolder.txtcount.setText(charts.getNumber() + "");
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = i;
                    if (position != RecyclerView.NO_POSITION) {

                        if (mListener != null) {
                            mListener.onItemClick(v, position);

                        }
                    }
                }
            });
            if(activate2) {
                viewHolder.checkdelet.setVisibility(View.VISIBLE);
                //notifyDataSetChanged();
            }else{
                viewHolder.checkdelet.setVisibility(View.GONE);
                //notifyDataSetChanged();
            }
            viewHolder.checkdelet.setChecked(chartListData.get(i).getlistckeck());
            viewHolder.checkdelet.setTag(chartListData.get(i));
            viewHolder.checkdelet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isChecked = viewHolder.checkdelet.isChecked();
                    CheckBox cb = (CheckBox) v;
                    currentTeacher = chartListData.get(pos);
                    if (cb.isChecked()) {
                        currentTeacher.setlistckeck(true);
                        checkboxclickList.add(currentTeacher);
                    } else if (!cb.isChecked()) {
                        currentTeacher.setlistckeck(false);
                        checkBox.setChecked(false);
                        checkboxclickList.remove(currentTeacher);
                    }

                    notifyDataSetChanged();
                }
            });
        } else {
            // Covers the case of data not being ready yet.
            viewHolder.txtday.setText("No Word");

        }
    }
    public void activateButtons(boolean activate) {
        this.activate2 = activate;
        notifyDataSetChanged(); //need to call it for the child views to be re-created with buttons.
    }
    ChartListData currentTeacher;
    public boolean checked = false;

    public void ClickItem() {
        if (!checked) {
            for (int i = 0; i < chartListData.size(); i++) {
                currentTeacher = chartListData.get(i);
                currentTeacher.setlistckeck(false);
                checkboxclickList.remove(currentTeacher);
            }
            for (int i = 0; i < chartListData.size(); i++) {
                currentTeacher = chartListData.get(i);
                currentTeacher.setlistckeck(true);
                checkboxclickList.add(currentTeacher);
            }
            checked = true;
        } else {
            for (int i = 0; i < chartListData.size(); i++) {
                currentTeacher = chartListData.get(i);
                currentTeacher.setlistckeck(false);
                checkboxclickList.remove(currentTeacher);
            }
            checked = false;
        }


    }

    @Override
    public int getItemCount() {//data의 갯수를 ViewHolder에 반환 해준다
        if (chartListData != null)
            return chartListData.size();
        else return 0;
    }

    private OnItemClickListener mListener = null; //어뎁터 클릭기능 추가 리스너

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtday;
        private TextView txtcount;
         CheckBox checkdelet;
        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            txtday=(TextView)itemView.findViewById(R.id.txt_time);
            txtcount=(TextView)itemView.findViewById(R.id.txt_count);
            checkdelet=(CheckBox)itemView.findViewById(R.id.deletcheck);
           // checkdelet.setVisibility(View.INVISIBLE);
// 아이템 클릭 이벤트 처리.
           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION){

                        if(mListener !=null){
                            mListener.onItemClick(v,position);

                        }
                    }

                }
            });*/

        }
        void onBind() {

        }
    }
    public List<ChartListData> getStudentist() {
        return chartListData;
    }
}
