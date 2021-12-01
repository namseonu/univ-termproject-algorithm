package com.example.midpoint_finder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    LinearLayout ll;
    EditText et;
    List<EditText> etList = new ArrayList<EditText>();

    static String[] station = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getHashKey();

        ll = findViewById(R.id.main_input_ll);
        et = findViewById(R.id.main_number_of_people_et);
        Button inputBtn = findViewById(R.id.main_input_btn);
        Button resetBtn = findViewById(R.id.main_reset_btn);
        Button startBtn = findViewById(R.id.main_start_btn);

        // 숫자 개수만큼 editText View 추가 생성
        inputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = 0;
                if(checkNull(et) != 0) {
                    num = Integer.parseInt(et.getText().toString());

                    for(int i = 0; i < num; i++) {
                        EditText newEt = new EditText(getApplicationContext());
                        newEt.setPrivateImeOptions("defaultInputmode=korean;");
                        newEt.setHint("역명");
                        newEt.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        etList.add(newEt);

                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        newEt.setLayoutParams(p);
                        newEt.setId(num);
                        ll.addView(newEt);
                    }
                }
            }
        });

        // 리셋 버튼 누름으로써 모든 editTex View 삭제
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.setText("");
                ll.removeAllViews();
            }
        });

        // 시작 버튼 누름으로써 프로그램 실행 (
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStationInformation();

                Intent intent = new Intent(MainActivity.this, OptionActivity.class);
                int size = etList.size();
                intent.putExtra("SIZE", Integer.toString(size));
                Log.d("MAIN/SIZE", Integer.toString(size));

                for(int i = 0; i < size; i++) {
                    intent.putExtra(Integer.toString(i), station[i]);
                    Log.d("MAIN/STATION", station[i]);
                }
                startActivity(intent);
            }
        });
    }

    private void getStationInformation() {
        int size = etList.size();
        if(size == 0) return;

        station = new String[size];

        for(int i = 0; i < size; i++) {
            if(checkNull(etList.get(i)) != 0) {
                station[i] = etList.get(i).getText().toString();
                Log.d("STATION", station[i]);
            }
        }
    }

    private void getHashKey() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    private int checkNull(EditText et) {
        return et.getText().toString().length();
    }
}