package com.nineone.ver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserMyAdapter extends RecyclerView.Adapter<UserMyAdapter.ViewHolder> {//걷기기록 리사이클뷰 어댑터
    private final LayoutInflater mInflater;
    private List<UserMyDataList> mWords; // Cached copy of words
    Context mContext;

    UserMyAdapter(Context context) { mInflater = LayoutInflater.from(context); }
    //ViewHolder : data의 수만큼만 데이터를 생성하고 데이터를 저장해 데이터가
    @Override    //            추가 되어도 반복적으로 조회하지 않고도 즉시 액세스 할 수 있다.
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {//ViewHolder 객체를 생성
     //   Log.e("arr1",parent.toString());
        View itemView = mInflater.inflate(R.layout.user_list_data_save, parent, false);//list data를 받아온다

        return new ViewHolder(itemView);//뷰홀더에 담아 리턴
    }



    public static Comparator<UserMyDataList> cmpAsc1 = new Comparator<UserMyDataList>() {
        @Override
        public int compare(UserMyDataList o1, UserMyDataList o2) {
            return o1.getName().compareTo(o2.getName());
        }

    };
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {// ViewHolder 에 data 를 넣는 작업 수행
        if (mWords != null) {

            UserMyDataList md = mWords.get(i);

            viewHolder.txtday.setText(md.getName());//
          //  Log.e("arr2",md.getName());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = i;
                    if (position != RecyclerView.NO_POSITION) {
                      //  Log.e("see4", String.valueOf(position));
                        if (mListener != null) {
                       //     Log.e("see5", String.valueOf(position));
                            mListener.onItemClick(v, position);

                        }
                    }
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View a_view) {
                    final int position = i;
                    if (position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            //     Log.e("see5", String.valueOf(position));
                            mListener2.onItemLongClick(a_view, position);

                        }
                    }

                    return true;
                }
            });
          //  viewHolder.txttime.setText(md.getTime());//시간
          //  viewHolder.txtdistance.setText(md.getDistance());//거리
        }else {
            // Covers the case of data not being ready yet.
          //  viewHolder.txttime.setText("No Word");
          //  viewHolder.txtdistance.setText("");
        }
        //viewHolder.txtcity.setText(md.getCity());
    }

    void setWords(List<UserMyDataList> words){ //데이터 추가시 갱신
        mWords = words;
      //  Log.e("arr3",mWords.toString());
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {//data의 갯수를 ViewHolder에 반환 해준다
        if (mWords != null)
            return mWords.size();
        else return 0;
    }

    private OnItemClickListener mListener = null; //어뎁터 클릭기능 추가 리스너
    private OnItemLongClickListener mListener2= null;
    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }
    public interface OnItemLongClickListener
    {
        void onItemLongClick(View v, int pos);
    }
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener2) {
        this.mListener2 = listener2 ;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView txtid,txtday,txttime,txtdistance,txtcity;

        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            txtday=(TextView)itemView.findViewById(R.id.txt_day);
           // txttime=(TextView)itemView.findViewById(R.id.txt_time);
           // txtdistance=(TextView)itemView.findViewById(R.id.txt_distance);
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
        void onBind(UserMyDataList listItem) {
            txtday.setText(listItem.getName());
          //  txttime.setText(listItem.getTime());
          //  txtdistance.setText(listItem.getDistance());
        }
    }
    public UserWordViewModel mWordViewModel;
    public UserMyDataList getWordAtPosition (int position) {

        return mWords.get(position);
    }


}