package com.example.picturemap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.overlay.InfoWindow;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestFile extends AppCompatActivity {//각종 테스트 클래스로 만드는 곳
    private MyDatabase myDatabase;
    private TextView muser_list;
    private RecyclerView rv;
    private WordViewModel mWordViewModel;
    private ListView mListView;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_file);
        recyclerView = findViewById(R.id.recyclerview);
        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("기록");
        final MyAdapter adapter = new MyAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        mWordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);
        mWordViewModel.getAllWords().observe(this, new Observer<List<MyDataList>>() {
            @Override
            public void onChanged(@Nullable final List<MyDataList> words) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(words);
            }
        });
        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                MyDataList myWord = adapter.getWordAtPosition(position);
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
                Toast.makeText(TestFile.this, myWord.getDay(), Toast.LENGTH_LONG).show();
                List<LatLng> coords = myWord.getCity();
                String list = Converters.fromArrayList(coords);
                Intent listClick = new Intent(getApplicationContext(), ListClick.class);
                listClick.putExtra("getday", myWord.getDay());
                listClick.putExtra("getTime", myWord.getTime());
                listClick.putExtra("getDistance", myWord.getDistance());
                listClick.putExtra("getCity", list);

                startActivity(listClick);

            }
        }) ;

      ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    // We are not implementing onMove() in this app
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    // When the use swipes a word,
                    // delete that word from the database.
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        MyDataList myWord = adapter.getWordAtPosition(position);
                        //Toast.makeText(TestFile.this, myWord.getId(), Toast.LENGTH_LONG).show();
                        // Delete the word
                        mWordViewModel.delete(myWord);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView);
    }




}
