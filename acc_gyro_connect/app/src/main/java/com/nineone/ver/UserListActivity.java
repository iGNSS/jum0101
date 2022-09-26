package com.nineone.ver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nineone.ver.DateUtil.get_nowCsvFilename;

public class UserListActivity extends AppCompatActivity  implements ViewModelStoreOwner {
    private TextView mEmptyList;
    public static final String TAG = "DeviceListActivity";
    private Handler mHandler,mHandler2;
    private boolean mScanning;
    public UserMyDatabase myDatabase;
    private TextView muser_list;
    public RecyclerView rv;
    public UserWordViewModel mWordViewModel;
    public ListView mListView;
    public RecyclerView recyclerView1;
    public ViewModelProvider.AndroidViewModelFactory viewModelFactory;
    public ViewModelStore viewModelStore = new ViewModelStore();
    public GridLayoutManager gridLayoutManager;
    public UserMyAdapter userMyadapter;
    Button cancelButton;
    Button SaveButton;
    private static final int InsetDialog = 50;
    private static final int UpddateDialog = 51;
    private List<UserMyDataList> mWords= new ArrayList<>(); // Cached copy of words
    int id2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.user_information_dialog);
        View decorView = getWindow().getDecorView(); decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        layoutParams.gravity= Gravity.TOP;
        layoutParams.y = 200;
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
         //   Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.


        // Checks if Bluetooth is supported on the device.

        mEmptyList = (TextView) findViewById(R.id.empty);
         cancelButton = (Button) findViewById(R.id.User_list_Cancle);
         SaveButton = (Button) findViewById(R.id.User_list_Save);
        cancelButton.setOnClickListener(mClickListener);
        SaveButton.setOnClickListener(mClickListener);
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
    public static Comparator<UserMyDataList> cmpAsc3 = new Comparator<UserMyDataList>() {
        @Override
        public int compare(UserMyDataList o1, UserMyDataList o2) {
            return o1.getName().compareTo(o2.getName());
        }

    };
    public void MainList() {
        myDatabase= Room.databaseBuilder(getApplicationContext(),UserMyDatabase.class,"note_data").allowMainThreadQueries().build();

        userMyadapter = new UserMyAdapter(this);//MyAdapter 불러오기
        recyclerView1 = findViewById(R.id.User_list);//리사이클 뷰 연결
        gridLayoutManager = new GridLayoutManager(this, 3);
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView1.setLayoutManager(gridLayoutManager);
        recyclerView1.setAdapter(userMyadapter);//리사이클뷰를 MyAdapterdp 연결
        //  mWordViewModel = new ViewModelProvider(this).get(UserWordViewModel.class);

        mWordViewModel = ViewModelProviders.of(this).get(UserWordViewModel.class);
        mWordViewModel.getAllWords().observe((LifecycleOwner) this, new Observer<List<UserMyDataList>>() {
            @Override
            public void onChanged(@Nullable final List<UserMyDataList> words) {
                // Update the cached copy of the words in the adapter.
                Log.e("arr0",words.toString());
                Collections.sort(words, cmpAsc3);
                userMyadapter.setWords(words);
            }
        });
        userMyadapter.notifyDataSetChanged();
        userMyadapter.setOnItemClickListener(new UserMyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {//리스트를 클릭했을때 이벤트
                UserMyDataList mydatalist = userMyadapter.getWordAtPosition(position);
                // TODO : 아이템 클릭 이벤트를 MainActivity에서 처리.
                Toast.makeText(UserListActivity.this, mydatalist.getName()+" 선택", Toast.LENGTH_SHORT).show();//임의의 토스트메세지
                Log.e("see",mydatalist.toString()+", "+position);
                Intent listClick = new Intent(getApplicationContext(), MainActivity.class);//클릭시 ListClick으로 이동 및 정보 전달


                listClick.putExtra("getday", mydatalist.getName());

                listClick.putExtra("getTime", mydatalist.getAge());
                listClick.putExtra("getDistance", mydatalist.getGender());
                //Log.e("dee",mydatalist.getName()+"  "+mydatalist.getAge()+"   "+mydatalist.getGender());
                    // c.putString(mydatalist.getDay(), mydatalist.getTime());
                    //Intent listClick = new Intent();
                    //listClick.putExtras(c);
                setResult(Activity.RESULT_OK, listClick);

                finish();
            }
        });
        userMyadapter.setOnItemLongClickListener(new UserMyAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View v, int position) {//리스트를 클릭했을때 이벤트
                UserMyDataList mydatalist2 = userMyadapter.getWordAtPosition(position);
                Intent newIntent2 = new Intent(UserListActivity.this, UserUpdateDialog.class);

                newIntent2.putExtra("setname", mydatalist2.getName());
                id2= mydatalist2.getId();
                startActivityForResult(newIntent2, UpddateDialog);
              //  Log.e("aaa",user_name55);

            }
        });
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
                        UserMyDataList myWord = userMyadapter.getWordAtPosition(position);
                        //Toast.makeText(TestFile.this, myWord.getId(), Toast.LENGTH_LONG).show();
                        // Delete the word
                        mWordViewModel.delete(myWord);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView1);//리사이클 뷰에 반영
    }

    private Button.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.User_list_Cancle:
                  //  setResult(Activity.RESULT_OK,listClick);
                    finish();

                    break;
                case R.id.User_list_Save:
                    //UserDialog();
                    Intent newIntent = new Intent(UserListActivity.this, UserDialog.class);
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
                    user_age = data.getStringExtra("dialogage");
                    user_gender = data.getIntExtra("dialoggender", 0);//
                    // Log.e("see1", getname1+"  "+getage1+"   "+getgender1);
                    //  getname1 =  data.getStringExtra(mydatalist.getDay());
                    //  getage1 = data.getStringExtra("getTime");//걸은시간
                    UserMyDataList userMyDataList=new UserMyDataList();
                    userMyDataList.setName(user_name55);
                    userMyDataList.setAge(user_age);
                    userMyDataList.setGender(user_gender);
                    mWordViewModel.insert(userMyDataList);

                    //mWordViewModel.insert(new UserMyDataList(user_name55, user_age, user_gender));
                    // Log.e("see2", user_name+"  "+user_age+"   "+user_gender);

                }
                break;

            case UpddateDialog:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    user_name55 = data.getStringExtra("dialogname1");
                    user_age = data.getStringExtra("dialogage1");
                    user_gender = data.getIntExtra("dialoggender1", 0);//
                    // Log.e("see1", getname1+"  "+getage1+"   "+getgender1);
                    //  getname1 =  data.getStringExtra(mydatalist.getDay());
                    //  getage1 = data.getStringExtra("getTime");//걸은시간
                    Log.e("see1", id2+"   "+user_name55+"  "+user_age+"   "+user_gender);
                    UserMyDataList userMyDataList=new UserMyDataList();
                    userMyDataList.setId(id2);
                    userMyDataList.setName(user_name55);
                    userMyDataList.setAge(user_age);
                    userMyDataList.setGender(user_gender);
                    mWordViewModel.update(userMyDataList);
                    // myDatabase  = UserMyDatabase.getDatabase(getApplication());
                 //   UserMyDataList userMyDataList =new UserMyDataList();
                   // myDatabase.myDao().update(new UserMyDataList(user_name55, user_age, user_gender));
                   // mWordViewModel.update(new UserMyDataList(user_name55, user_age, user_gender));
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

