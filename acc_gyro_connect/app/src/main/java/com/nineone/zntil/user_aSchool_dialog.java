package com.nineone.zntil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.nineone.ver.MainActivity;
import com.nineone.ver.R;
import com.nineone.ver.UserDialog;
import com.nineone.ver.UserMyDataList;
import com.nineone.ver.UserUpdateDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class user_aSchool_dialog  extends AppCompatActivity implements ViewModelStoreOwner {
    private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";
    private Handler mHandler,mHandler2;
    private boolean mScanning;
    public ShoolnameDatabase myDatabase;
    private TextView muser_list;
    public RecyclerView rv;
    public ShoolnameViewModel mWordViewModel;
    public ListView mListView;
    public RecyclerView recyclerView1;
    public ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    public ViewModelStore viewModelStore = new ViewModelStore();
    public GridLayoutManager gridLayoutManager;
    public ShoolnameAdapter userMyadapter;
    public RecyclerAdapter recyclerAdapter;
    Button cancelButton;
    Button SaveButton;
    private static final int InsetDialog = 50;
    private static final int UpddateDialog = 51;
    private ArrayList<String> Shoolname = new ArrayList<String>();
    private List<ShoolnameList> mWords= new ArrayList<>(); // Cached copy of words
    int id2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_aschool_dialog);
        mEmptyList = (TextView) findViewById(R.id.empty);
        cancelButton = (Button) findViewById(R.id.Shool_list_Cancle);
        SaveButton = (Button) findViewById(R.id.Shool_list_Save);
        cancelButton.setOnClickListener(mClickListener);
        SaveButton.setOnClickListener(mClickListener);

        Shoolname.add(0,"경동초");
        Shoolname.add(1,"침산초");
        Shoolname.add(2,"팔공초");
        Shoolname.add(3,"용계초");

        MainList();

    }
    @Override

    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect dialogBounds = new Rect();
        getWindow().getDecorView().getHitRect(dialogBounds);
        if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
            // 영역외 터치시 닫히지 않도
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }
    public static Comparator<ShoolnameList> cmpAsc3 = new Comparator<ShoolnameList>() {
        @Override
        public int compare(ShoolnameList o1, ShoolnameList o2) {
            return o1.getShoolName().compareTo(o2.getShoolName());
        }

    };
    private ArrayList<String> allMovieList = new ArrayList();
    public void MainList() {
     //   myDatabase= Room.databaseBuilder(getApplicationContext(),ShoolnameDatabase.class,"Shool_name_data").allowMainThreadQueries().build();

  /*      userMyadapter = new ShoolnameAdapter(this);//MyAdapter 불러오기
        recyclerView1 = findViewById(R.id.Shool_list);//리사이클 뷰 연결
        gridLayoutManager = new GridLayoutManager(this, 1);
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setLayoutManager(gridLayoutManager);
        recyclerView1.setAdapter(userMyadapter);//리사이클뷰를 MyAdapterdp 연결 */
        recyclerAdapter = new RecyclerAdapter();//MyAdapter 불러오기
        recyclerView1 = findViewById(R.id.Shool_list);//리사이클 뷰 연결
        gridLayoutManager = new GridLayoutManager(this, 1);
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setLayoutManager(gridLayoutManager);
        recyclerView1.setAdapter(recyclerAdapter);
        List<String> listTitle = Arrays.asList("경동초", "팔공초", "침산초");
        for (int i = 0; i < listTitle.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            ShoolnameList data = new ShoolnameList();
            data.setShoolName(listTitle.get(i));
            // 각 값이 들어간 data를 adapter에 추가합니다.
            recyclerAdapter.addItem(data);
        }
        //  mWordViewModel = new ViewModelProvider(this).get(UserWordViewModel.class);


    //    mWordViewModel = ViewModelProviders.of(this).get(ShoolnameViewModel.class);
     /*  ShoolnameList userMyDataList=new ShoolnameList();
        for(int i=0;i<Shoolname.size();i++) {
            Toast.makeText(user_aSchool_dialog.this, Shoolname+" 선택", Toast.LENGTH_SHORT).show();//임의의 토스트메세지
            userMyDataList.ShoolnameList(Shoolname.get(i).toString());
            mWordViewModel.insert(userMyDataList);

        //    userMyDataList.setId(id2);
           // userMyDataList.setShoolName(Shoolname.get(i));

        }*/

    /*    mWordViewModel.getAllWords().observe((LifecycleOwner) this, new Observer<List<ShoolnameList>>() {
            @Override
            public void onChanged(@Nullable final List<ShoolnameList> words) {
                // Update the cached copy of the words in the adapter.
                Log.e("arr0",words.toString());
                Collections.sort(words, cmpAsc3);
                userMyadapter.setWords(words);
            }
        });*/
      /*  recyclerAdapter.notifyDataSetChanged();
        recyclerAdapter.setOnItemClickListener(new ShoolnameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                ShoolnameList mydatalist = recyclerAdapter.getWordAtPosition(position);
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
                Toast.makeText(user_aSchool_dialog.this, mydatalist.getShoolName()+" 선택", Toast.LENGTH_SHORT).show();//임의의 토스트메세지
                Log.e("see",mydatalist.toString()+", "+position);
                Intent listClick = new Intent(getApplicationContext(), MainActivity.class);//클릭시 ListClick으로 이동 및 정보 전달


                listClick.putExtra("getshoolname", mydatalist.getShoolName());

                //Log.e("dee",mydatalist.getName()+"  "+mydatalist.getAge()+"   "+mydatalist.getGender());
                // c.putString(mydatalist.getDay(), mydatalist.getTime());
                //Intent listClick = new Intent();
                //listClick.putExtras(c);
                setResult(Activity.RESULT_OK, listClick);

                finish();
            }
        });*/

      /*  ItemTouchHelper helper = new ItemTouchHelper(//삭제를 위한 이벤트
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
                        ShoolnameList myWord = userMyadapter.getWordAtPosition(position);
                        //Toast.makeText(TestFile.this, myWord.getId(), Toast.LENGTH_LONG).show();
                        // Delete the word
                        mWordViewModel.delete(myWord);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView1);//리사이클 뷰에 반영*/
    }

    private Button.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Shool_list_Cancle:
                    //  setResult(Activity.RESULT_OK,listClick);
                    finish();
                    break;
                case R.id.Shool_list_Save:
                    //UserDialog();
                    Intent newIntent = new Intent(user_aSchool_dialog.this, UserDialog.class);
                    startActivityForResult(newIntent, InsetDialog);
                    break;
            }
        }
    };
    String user_age=null;
    int user_gender=0;
    String user_name55=null;//사용자이름 저장 함수
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case InsetDialog:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    user_name55 = data.getStringExtra("dialogname");
                    // Log.e("see1", getname1+"  "+getage1+"   "+getgender1);
                    //  getname1 =  data.getStringExtra(mydatalist.getDay());
                    //  getage1 = data.getStringExtra("getTime");//걸은시간
                    ShoolnameList userMyDataList=new ShoolnameList();
                    userMyDataList.setShoolName(user_name55);
                    mWordViewModel.insert(userMyDataList);

                    //mWordViewModel.insert(new UserMyDataList(user_name55, user_age, user_gender));
                    // Log.e("see2", user_name+"  "+user_age+"   "+user_gender);

                }
                break;


            default:
                //Log.e(TAG, "wrong request code");
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModelStore.clear();
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return viewModelStore;
    }

    boolean chice_gender=true;
   /* public void UserDialog(){

        UserDialog dialog = new UserDialog(UserListActivity.this);

        dialog.setDialogListener(new UserDialogListener() {  // MyDialogListener 를 구현
            @Override
            public void onPositiveClicked(String name,String age,int gender ) {

                user_name55 = name;
                if(age=="선택안함"){
                    user_age = "선택안함";
                }else {
                    user_age = age;
                }
                if(gender==0){
                    user_gender = "0";
                }else if(gender==1) {
                    user_gender = "남";
                }else if(gender==2){
                    user_gender="여";
                }else{
                    user_gender = "0";
                }
                Log.e("dff",user_name55+", "+user_age+", "+user_gender);
               // myDatabase.myDao().insert(new UserMyDataList(user_name55));
                mHandler2 = new Handler();
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        mHandler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                userMyadapter.notifyDataSetChanged();
                                recyclerView1.invalidate();
                                mWordViewModel.insert(new UserMyDataList(user_name55,age,gender));
                               // Intent intent = getIntent();
                              //  finish();
                               // startActivity(intent);
                            }
                        }, 1000);
                    }
                } );

            }
            @Override
            public void onNegativeClicked() {
                //  Log.d("MyDialogListener", "onNegativeClicked");
            }
        });
        dialog.show();
    }*/
}
