package com.example.midpoint_finder;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class FoundActivity extends AppCompatActivity {

    String[] information;
    String[] station;

    String midpoint = null; // 중간 지점 역명
    float[] midpointXY = new float[2];  // 역에 대한 좌표 (x, y) 저장
    BackgroundTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        Intent intent = getIntent();
        int size = Integer.parseInt(intent.getStringExtra("size"));
        Log.d("FOUND_SIZE", Integer.toString(size));

        station = new String[size];
        information = new String[size + 1];
        information[0] = Integer.toString(size);

        for(int i = 0; i < size; i++) {
            station[i] = intent.getStringExtra(Integer.toString(i));
            information[i + 1] = station[i];
            Log.d("FOUND_STATION", station[i]);
        }

        // 백그라운드에서 알고리즘 실행 및 결과 도출
        try {
            task = new BackgroundTask();
            task.execute(information);
        } catch(Exception e) {
            e.printStackTrace();
        }

        Button homeBtn = findViewById(R.id.found_icon_home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel(true);  // 종료
    }

    private class BackgroundTask extends AsyncTask<String[], String[], String[]> {
        private String[] result = new String[3];

        // 초기화 (해당 프로그램에서는 사용 X)
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // 스레드의 백그라운드 작업 구현
        @Override
        protected String[] doInBackground(String[]... strings) {
            try {
                new Main(information);
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = Main.getResult();
            Log.d("FOUND", result[0] + ", " + result[1] + ", " + result[2]);
            return result;
        }

        // 스레드 취소되었을 때 호출
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            midpointXY[0] = Float.parseFloat(result[0]);
            midpointXY[1] = Float.parseFloat(result[1]);
            midpoint = result[2];

            // doInBackground()에서 리턴받은 result 값 사용
            // 카카오 맵 띄우기
            MapView mapView = new MapView(FoundActivity.this);
            ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.found_map_view);
            mapViewContainer.addView(mapView);

            // 중심점 변경
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]), true);

            // 줌 레벨 변경
            mapView.setZoomLevel(3, true);

            MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]);
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("Midpoint");
            marker.setTag(1);
            marker.setMapPoint(MARKER_POINT);
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.icon_marker);
            marker.setCustomImageAutoscale(false);
            marker.setCustomImageAnchor(0.5f, 1.0f);
            mapView.addPOIItem(marker);
        }
    }
}