package com.nineone.inner_s_tool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Main_Card_Activity extends AppCompatActivity {
    ViewPager2 viewPager;
    View_card_adapter view_card_adapter;
    List<View_card_model> models;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_card);
        models = new ArrayList<>();
        models.add(new View_card_model(R.drawable.ss_image, "삼성중공업", "거제조선소"));
        models.add(new View_card_model(R.drawable.ss_image, "나인원", "테스트"));
    
        view_card_adapter = new View_card_adapter(models,this);

        viewPager = findViewById(R.id.view_pager2);

        int dpValue = 70;
        float d = getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d);
        viewPager.setPadding(margin, 0, margin, 0);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);

        viewPager.setOffscreenPageLimit(3);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });

        viewPager.setPageTransformer(compositePageTransformer);
        viewPager.setAdapter(view_card_adapter);




    }
}