package com.example.midpoint_finder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class FoundActivity extends AppCompatActivity {

    String midpoint = null;
    int[] midpointXY = new int[2];  // 역에 대한 좌표 (x, y) 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        Button homeBtn = findViewById(R.id.found_icon_home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 카카오 맵 띄우기
        MapView mapView = new MapView(this);
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