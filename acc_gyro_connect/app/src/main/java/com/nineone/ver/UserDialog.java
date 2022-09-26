package com.nineone.ver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserDialog extends AppCompatActivity {

    private UserDialogListener dialogListener;

    private static final int LAYOUT = R.layout.user_dialog;

    private Context context;

    private EditText nameEt;

    String name=null;
    private Button cancelButton;
    private Button okButton;
    private Button age;
    InputMethodManager keyboard;
    InputMethodManager ime = null;
    final int[] words = {0, 1, 2, 3, 4, 5};
    int ageNumber;
    int la;
    Button mgender_m,mgender_w;
    int chice_gender=0;
    public void setDialogListener(UserDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }


   // final ArrayList<String> versionArray=new ArrayList<String>();
    final String[] versionArray = new String[100];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(LAYOUT);
        //타이틀 바 삭제
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        nameEt = findViewById(R.id.put_text);
        cancelButton = (Button) findViewById(R.id.btnCancle);
        cancelButton.setOnClickListener(mClickListener);
        okButton = (Button) findViewById(R.id.btnSave);
        okButton.setOnClickListener(mClickListener);
        age=findViewById(R.id.Age_button);
        age.setOnClickListener(mClickListener);
        mgender_m = findViewById(R.id.gender_m);
        mgender_m.setOnClickListener(mClickListener);
        mgender_m.setSelected(false);
        mgender_w = findViewById(R.id.gender_w);
        mgender_w.setOnClickListener(mClickListener);
        mgender_w.setSelected(false);
        versionArray[0]="선택안함";
       // Intent listClick = getIntent();//WalkList에서 정보를 받아오기
       // name = listClick.getExtras().getString("setname",null);//csv파일 경로
        for(int i=1;i<=99;i++) {
            versionArray[i]=String.valueOf(i);
        }
        if(name==null) {

        }else{
            // nameEt.setText(name);
            la=name.length()+1;
            nameEt.setText(name.substring(0, name.length()-1));
        }


    }
    public void adit(){
        nameEt.setText(name);
    }
    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.Age_button:
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserDialog.this, R.style.MyDialogTheme);
                    builder.setTitle("나이"); //제목
                    builder.setSingleChoiceItems(versionArray, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //age.setText(versionArray[which]+"");
                            ageNumber=which;
                        }
                    });
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which) {
                            if (versionArray[0] == versionArray[ageNumber]) {
                                age.setText("");

                            } else {
                                age.setText(versionArray[ageNumber] + "");
                            }
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    builder.show();
                    break;
                case R.id.gender_m:
                    if(chice_gender==0) {
                        chice_gender = 1;
                        mgender_m.setSelected(true);
                        mgender_w.setSelected(false);
                    }else if(chice_gender==1){
                        chice_gender = 0;
                        mgender_m.setSelected(false);
                        mgender_w.setSelected(false);
                    }else{
                        chice_gender = 1;
                        mgender_m.setSelected(true);
                        mgender_w.setSelected(false);
                    }
                    break;
                case R.id.gender_w:
                    if(chice_gender==0) {
                        chice_gender=2;
                        mgender_m.setSelected(false);
                        mgender_w.setSelected(true);
                    }else if(chice_gender==2){
                        chice_gender = 0;
                        mgender_m.setSelected(false);
                        mgender_w.setSelected(false);
                    }else{
                        chice_gender=2;
                        mgender_m.setSelected(false);
                        mgender_w.setSelected(true);
                    }

                    break;
                case R.id.btnCancle:
                    finish();
                    break;
                case R.id.btnSave:
                    String name = nameEt.getText().toString();
                    if (name == "" || name == " " || name.length() == 0 || name.trim().isEmpty()) {
                        Toast.makeText(UserDialog.this, "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent DiaglogClick = new Intent(getApplicationContext(), UserListActivity.class);//클릭시 ListClick으로 이동 및 정보 전달
                        DiaglogClick.putExtra("dialogname", name);
                        DiaglogClick.putExtra("dialogage", versionArray[ageNumber]);
                        DiaglogClick.putExtra("dialoggender", chice_gender);
                        //Log.e("dee",mydatalist.getName()+"  "+mydatalist.getAge()+"   "+mydatalist.getGender());
                        // c.putString(mydatalist.getDay(), mydatalist.getTime());
                        //Intent listClick = new Intent();
                        //listClick.putExtras(c);
                        setResult(Activity.RESULT_OK, DiaglogClick);

                        finish();
                        //dialogListener.onPositiveClicked(name, versionArray[ageNumber],chice_gender);
                       // finish();
                    }
                    break;
            }
        }
    };
}