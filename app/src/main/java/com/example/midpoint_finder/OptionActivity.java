package com.example.midpoint_finder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class OptionActivity extends AppCompatActivity {

    ImageView cafeOn, cafeOff;
    ImageView storeOn, storeOff;
    ImageView departmentOn, departmentOff;
    ImageView cinemaOn, cinemaOff;

    static int size = 0;
    static String[] information;
    static String[] station;
    static Boolean cafe = false, store = false, department = false, cinema = false;
    static Boolean[] bool = new Boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        Intent intent = getIntent();
        size = Integer.parseInt(intent.getStringExtra("SIZE"));
        Log.d("OPTION/SIZE", Integer.toString(size));

        station = new String[size];
        information = new String[size + 5]; // 1(역 개수) + 역명 + 4(boolean)
        information[0] = Integer.toString(size);

        for(int i = 0; i < size; i++) {
            station[i] = intent.getStringExtra(Integer.toString(i));
            information[i + 1] = station[i];
            Log.d("OPTION/INFO", information[i]);
        }

        cafeOn = findViewById(R.id.option_cafe_on_iv);
        cafeOff = findViewById(R.id.option_cafe_off_iv);
        storeOn = findViewById(R.id.option_store_on_iv);
        storeOff = findViewById(R.id.option_store_off_iv);
        departmentOn = findViewById(R.id.option_department_store_on_iv);
        departmentOff = findViewById(R.id.option_department_store_off_iv);
        cinemaOn = findViewById(R.id.option_cinema_on_iv);
        cinemaOff = findViewById(R.id.option_cinema_off_iv);

        onClickListener();

        Button startBtn = findViewById(R.id.option_start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOptionInformation();

                Intent intent = new Intent(OptionActivity.this, FoundActivity.class);
                intent.putExtra("SIZE", Integer.toString(size));

                for(int i = 0; i < size + 5; i++) {
                    intent.putExtra(Integer.toString(i), information[i]);
                }
                startActivity(intent);
            }
        });
    }

    private void getOptionInformation() {
        bool[0] = cafe;
        bool[1] = store;
        bool[2] = department;
        bool[3] = cinema;

        for(int i = size + 1; i < size + 5; i++) {
            information[i] = Boolean.toString(bool[i - size - 1]);
        }
    }

    private void onClickListener() {
        cafeOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cafe = false;
                cafeOn.setVisibility(View.GONE);
                cafeOff.setVisibility(View.VISIBLE);
            }
        });

        cafeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cafe = true;
                cafeOn.setVisibility(View.VISIBLE);
                cafeOff.setVisibility(View.GONE);
            }
        });

        storeOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store = false;
                storeOn.setVisibility(View.GONE);
                storeOff.setVisibility(View.VISIBLE);
            }
        });

        storeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                store = true;
                storeOn.setVisibility(View.VISIBLE);
                storeOff.setVisibility(View.GONE);
            }
        });

        departmentOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                department = false;
                departmentOn.setVisibility(View.GONE);
                departmentOff.setVisibility(View.VISIBLE);
            }
        });

        departmentOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                department = true;
                departmentOn.setVisibility(View.VISIBLE);
                departmentOff.setVisibility(View.GONE);
            }
        });

        cinemaOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cinema = false;
                cinemaOn.setVisibility(View.GONE);
                cinemaOff.setVisibility(View.VISIBLE);
            }
        });

        cinemaOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cinema = true;
                cinemaOn.setVisibility(View.VISIBLE);
                cinemaOff.setVisibility(View.GONE);
            }
        });
    }
}
