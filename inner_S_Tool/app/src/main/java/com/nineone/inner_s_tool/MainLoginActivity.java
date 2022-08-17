package com.nineone.inner_s_tool;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class MainLoginActivity extends AppCompatActivity {
    private EditText editText;
    private TextView textView;
    private Button loginbutton_ok_no_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        editText = findViewById(R.id.LoginEdit);
        editText.setSelected(false);
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!Character.isLetterOrDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        }});
        loginbutton_ok_no_text = findViewById(R.id.LoginButton_ok_no_text);
        loginbutton_ok_no_text.setEnabled(false);
        textView = findViewById(R.id.LoginEdit_ok_no_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText.getText().toString().trim().length()<=1) {
                    textView.setTextColor(Color.parseColor("#FF0000"));
                    textView.setText("최소 2글자 이상 입력 바랍니다.");
                    editText.setSelected(false);
                    loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.gray));
                    loginbutton_ok_no_text.setEnabled(false);
                } else {

                    textView.setTextColor(Color.parseColor("#FFFFFF"));
                    textView.setText("");
                    editText.setSelected(true);
                    loginbutton_ok_no_text.getBackground().setTint(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    loginbutton_ok_no_text.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        loginbutton_ok_no_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //저장을 하기위해 Editor를 불러온다.
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("startname", editText.getText().toString());
                edit.apply();
                Intent newIntent = new Intent(getApplicationContext(), Main_Card_Activity.class);
                startActivity(newIntent);
                finish();
            }
        });
    }
    protected InputFilter filterId = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");

            if (!ps.matcher(source).matches()) {
                return source.toString().replaceAll("[^a-zA-Z\\s]", " ").trim();
            }
            return null;
        }
    };
}
