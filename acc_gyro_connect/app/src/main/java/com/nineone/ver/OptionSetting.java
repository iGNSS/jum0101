package com.nineone.ver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class OptionSetting extends AppCompatActivity {//옵션페이지제어

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_option_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new Settings()).commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static class Settings extends PreferenceFragment {
        SharedPreferences pref;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.optionsetting);

            pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            CheckSetting();

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        public void CheckSetting(){
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean send = sp.getBoolean("snedcheck",false);
            boolean senser= sp.getBoolean("essential_sendser", false);
            boolean user= sp.getBoolean("essential_user", false);

            if (send) {
                Toast.makeText(getActivity(),"센서필수 기능 ON",Toast.LENGTH_SHORT);

            }else{
                Toast.makeText(getActivity(),"센서필수 기능 OFF",Toast.LENGTH_SHORT);
            }
            if (senser) {
                Toast.makeText(getActivity(),"센서필수 기능 ON",Toast.LENGTH_SHORT);

            }else{
                Toast.makeText(getActivity(),"센서필수 기능 OFF",Toast.LENGTH_SHORT);
            }
            if (user) {
                Toast.makeText(getActivity(),"이름필수 기능 ON",Toast.LENGTH_SHORT);

            }else{
                Toast.makeText(getActivity(),"이름필수 기능 OFF",Toast.LENGTH_SHORT);
            }
            ListPreference LP = (ListPreference)findPreference("measurement_time");
            String timer = sp.getString("measurement_time","false");
            if("30".equals(timer)){
                Toast.makeText(getActivity(),"30 초",Toast.LENGTH_SHORT);
            }else if("60".equals(timer)){
                Toast.makeText(getActivity(),"60 초",Toast.LENGTH_SHORT);
            }else if("90".equals(timer)){
                Toast.makeText(getActivity(),"90 초",Toast.LENGTH_SHORT);
            }else if("120".equals(timer)){
                Toast.makeText(getActivity(),"120 초",Toast.LENGTH_SHORT);
            }else if("150".equals(timer)){
                Toast.makeText(getActivity(),"150 초",Toast.LENGTH_SHORT);
            }else if("180".equals(timer)){
                Toast.makeText(getActivity(),"180 초",Toast.LENGTH_SHORT);
            }
        }
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Preference preference = findPreference(key);
                if (key.equals("snedcheck")) {
                    boolean b = pref.getBoolean("snedcheck", false);
                    if(b) {
                        Toast.makeText(getActivity(), "측정완료 후 데이터 전송 ON" , Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getActivity(), "측정완료 후 데이터 전송 OFF" , Toast.LENGTH_SHORT).show();
                    }
                }if (key.equals("measurement_time")) {
                    preference.setSummary(((ListPreference) preference).getEntry());
                }
            }
        };
        @Override
        public void onResume() {
            super.onResume();

//설정값 변경리스너..등록
            pref.registerOnSharedPreferenceChangeListener(listener);
        }//onResume() ..

        @Override
        public void onPause() {
            super.onPause();

            pref.unregisterOnSharedPreferenceChangeListener(listener);

        }

    }

}