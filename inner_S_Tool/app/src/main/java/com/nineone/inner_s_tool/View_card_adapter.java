package com.nineone.inner_s_tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class View_card_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<View_card_model> models;
    private LayoutInflater layoutInflater;
    private Context context;


    public View_card_adapter(List<View_card_model> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return models.size();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_main_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final View_card_model item = models.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        int get_position = viewHolder.getAdapterPosition();
        layoutInflater = LayoutInflater.from(context);


        viewHolder.imageButton.setImageResource(item.getImage());
        viewHolder.tagname.setText(item.getTitle());
        viewHolder.tagdata.setText(item.getDesc());


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(get_position==0) {
                    Intent intent = new Intent(context, MainSectorActivity.class);
                    //intent.putExtra("param", models.get(position).getTitle());
                    context.startActivity(intent);

                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setTitle("테스트 중");
                    builder.setMessage("아직 테스트 중입니다.");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //  finish();
                        }
                    });
                    builder.show();
                }

            }

        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageButton;
        private final TextView tagname;
        private final TextView tagdata;
        public ViewHolder(View itemView) {//ViewHolder에 띄울 텍스트
            super(itemView);
            //txtid=(TextView)itemView.findViewById(R.id.txt_id);
            imageButton= (ImageView) itemView.findViewById(R.id.card_image);
            tagname=(TextView)itemView.findViewById(R.id.title);
            tagdata=(TextView)itemView.findViewById(R.id.desc);

        }

        /*void onBind(Z_list_item z_list_item) {
            tagname.setText(z_list_item.getItem_tag_name());
        }*/
    }
}
