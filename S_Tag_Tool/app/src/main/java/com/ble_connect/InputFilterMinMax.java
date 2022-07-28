package com.ble_connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import com.nineone.s_tag_tool.MainActivity;

public class InputFilterMinMax implements InputFilter {

    private final int min;
    private final int max;
    Activity activity;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputFilterMinMax(String min, String max, Activity activity) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
        this.activity = activity;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());

            if (isInRange(min, max, input)) {
                return null;
            }

        } catch (NumberFormatException ignored) {

        }
        Log.e("minmax", min + ", " + max + ", " + dest.toString() + "," + source.toString());
        if (!dest.toString().equals("")) {
            //  Toast.makeText(connect_activity, "1자리 숫자 만 입력 가능합니다 문자를 입력했으므로 지운다음 다시 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
            dlg.setMessage("값이 너무 높습니다\n최대 값 : " + max);
            dlg.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 처리할 코드 작성
                }
            });
            dlg.show();
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
