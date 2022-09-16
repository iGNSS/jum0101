package com.nineone.dummy_data_send;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

public class Change_setting extends AppCompatActivity {
    private Button cancelButton,okButton;
    private TextView UV_adress_text,RF_adress_text,ID_name_text;
    private EditText UV_adress_edit,RF_adress_edit,ID_name_edit;
    private String UV_adress,RF_adress,ID_name;
    private int Renewal_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_setting);
        Intent newIntent = getIntent();//WalkList에서 정보를 받아오기
        UV_adress= newIntent.getExtras().getString("UV_adress","");
        RF_adress = newIntent.getExtras().getString("RF_adress","");
        ID_name = newIntent.getExtras().getString("ID_name","");
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width=0;
        int height=0;
        width = (int) (dm.widthPixels * 0.9); // Display 사이즈의 70%
        height = (int) (dm.heightPixels * 0.9);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        UV_adress_text = (TextView) findViewById(R.id.name_change_text);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(UV_adress_text, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        UV_adress_edit = (EditText) findViewById(R.id.name_change_edit);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(UV_adress_edit, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        UV_adress_edit.setText(UV_adress);

        RF_adress_text = (TextView) findViewById(R.id.URL_change_text);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(RF_adress_text, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        RF_adress_edit = (EditText) findViewById(R.id.URL_change_edit);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(RF_adress_edit, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        RF_adress_edit.setText(RF_adress);

        ID_name_text= (TextView) findViewById(R.id.Port_change_text);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(ID_name_text, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        ID_name_edit = (EditText) findViewById(R.id.Port_change_edit);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(ID_name_edit, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        ID_name_edit.setText(ID_name);

        cancelButton = (Button) findViewById(R.id.btnCancle);//dialog_end_time
        TextViewCompat.setAutoSizeTextTypeWithDefaults(cancelButton, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        cancelButton.setOnClickListener(mClickListener);
        okButton = (Button) findViewById(R.id.btnSave);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(okButton, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        okButton.setOnClickListener(mClickListener);
    }
    private Button.OnClickListener mClickListener = new View.OnClickListener() {//각 버튼 클릭리스너
        @Override
        public void onClick(View v) {
            int id =v.getId();
            if(id==R.id.btnSave){
                Intent newIntent = new Intent(Change_setting.this, MainActivity.class);
                newIntent.putExtra("UV_adress", UV_adress_edit.getText().toString());
                newIntent.putExtra("RF_adress", RF_adress_edit.getText().toString());
                newIntent.putExtra("ID_name", ID_name_edit.getText().toString());
                setResult(3000, newIntent);
                finish();
            }else if(id==R.id.btnCancle){
                finish();
            }

        }
    };
}