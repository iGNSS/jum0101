package com.ble_connect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

import java.util.regex.Pattern;

public class InputDoubleFilterMinMax implements InputFilter {
    double min = Double.MIN_VALUE;
    double max = Double.MAX_VALUE;
    Pattern pattern;
    Activity activity;
    public InputDoubleFilterMinMax(double min, double max, int decimalCount, Activity activity) {
        this.min = min;
        this.max = max;
        pattern = Pattern.compile("[0-9]+((\\.[0-9]{0," + decimalCount + "})?)||(\\.)?");
        this.activity = activity;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        Log.d("filter", source + ", " + start + ", " + end + ", " + dest + ", " + dstart + ", " + dend);

        try {
            // 입력된 문자와 기존 문자 조합
            StringBuilder sb = new StringBuilder(dest.toString());
            sb.insert(dstart, source);
            String strVal = sb.toString();

            Log.d("filter", "val = " + strVal);

            // 형식 검사
            if (!pattern.matcher(strVal).matches()) {
                Log.e("minmax1", min + ", " + max + ", " + dest.toString() + "," + source.toString());
                if(!dest.toString().equals("")) {
                    //  Toast.makeText(connect_activity, "1자리 숫자 만 입력 가능합니다 문자를 입력했으므로 지운다음 다시 숫자를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
                    dlg.setMessage("소수 첫째자리까지 입력 가능합니다.");
                    dlg.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // 처리할 코드 작성
                        }
                    });
                    dlg.show();
                }
                return "";
            }
            if ("0".equals(strVal) || "0.".equals(strVal)) {
                Log.e("minmax2", min + ", " + max + ", " + dest.toString() + "," + source.toString());
                return null;
            }
            // 범위 검사
            double val = Double.parseDouble(strVal);
            if (val >= min && val <= max) {
                Log.e("minmax3",min+", "+ max+", "+dest.toString()+"," + source.toString());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("minmax4",min+", "+ max+", "+dest.toString()+"," + source.toString());
        if(!dest.toString().equals("")) {
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
}
