package com.nineone.ver;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OptionDialog extends Dialog {// 다이얼로그형식 옵션 지금은 사용중지

    private OptionDialogListener dialogListener;

    private static final int LAYOUT = R.layout.option_dialog;

    private Context context;

    private EditText nameEt;

    private boolean sensoryes;
    private boolean nameyes;
    private Button cancelButton;
    private Button okButton;
    InputMethodManager keyboard;
    InputMethodManager ime = null;
    public OptionDialog(Context context, boolean sensoryes,boolean nameyes) {
        super(context);
        this.context = context;
        this.sensoryes = sensoryes;
        this.nameyes =nameyes;
    }

    public void setDialogListener(OptionDialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(LAYOUT);
        //타이틀 바 삭제
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        nameEt = findViewById(R.id.put_text);
        cancelButton = (Button) findViewById(R.id.btnCancle);
        okButton = (Button) findViewById(R.id.btnSave);
        cancelButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();
            }
        });

        okButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                if (name == "" || name == " " || name.length() == 0 || name.trim().isEmpty()) {
                    Toast.makeText(getContext(), "이름을 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else {
              //      dialogListener.onyesNO(sensoryes,nameyes);
                    dismiss();
                }
            }

        });


    }
}