package com.example.picturemap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;

import java.util.List;

public class WalkList extends AppCompatActivity {//걷기 기록들을 리사이클 뷰로 나타내는곳
    private MyDatabase myDatabase;
    private TextView muser_list;
    private RecyclerView rv;
    private WordViewModel mWordViewModel;
    private ListView mListView;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_list);

        ActionBar actionBar = getSupportActionBar();  //제목줄 객체 얻어오기
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("기록");
        final MyAdapter adapter = new MyAdapter(this);//MyAdapter 불러오기
        recyclerView = findViewById(R.id.recyclerview);//리사이클 뷰 연결
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);//리사이클뷰를 MyAdapterdp 연결
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
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                MyDataList mydatalist = adapter.getWordAtPosition(position);
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
                Toast.makeText(WalkList.this, mydatalist.getDay(), Toast.LENGTH_LONG).show();//임의의 토스트메세지
                List<LatLng> coords = mydatalist.getCity();//컨버트를 위해 이동경로 불러오기
                String list = Converters.fromArrayList(coords);//이동경로를 String으로 컨버트
                Intent listClick = new Intent(getApplicationContext(), ListClick.class);//클릭시 ListClick으로 이동 및 정보 전달 
                listClick.putExtra("getday", mydatalist.getDay());
                listClick.putExtra("getTime", mydatalist.getTime());
                listClick.putExtra("getDistance", mydatalist.getDistance());
                listClick.putExtra("getCity", list);

                startActivity(listClick);
            }
        }) ;

        ItemTouchHelper helper = new ItemTouchHelper(//삭제를 위한 이벤트
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {//리스트를 왼쪽 또는 오른쪽으로 밀었을때 발생
                    @Override
                    // We are not implementing onMove() in this app
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {//리사이클 리스트가 움직인다.
                        return false;
                    }
                    @Override
                    // When the use swipes a word,
                    // delete that word from the database.
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {//움직인 리사이클 뷰 삭제
                        int position = viewHolder.getAdapterPosition();
                        MyDataList myWord = adapter.getWordAtPosition(position);
                        //Toast.makeText(TestFile.this, myWord.getId(), Toast.LENGTH_LONG).show();
                        // Delete the word
                        mWordViewModel.delete(myWord);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView);//리사이클 뷰에 반영
    }




}
