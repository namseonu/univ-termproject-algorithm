package com.example.midpoint_finder;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FoundActivity extends AppCompatActivity {

    String[] information;
    String midpoint = null; // 중간 지점 역명
    float[] midpointXY = new float[2];  // 역에 대한 좌표 (x, y) 저장
    Background background = null;
    MapView mapView;

    static Station_info[] stations;
    static Station_distance[] station_distances;
    static Transfer_info[] transfer_infos;
    private String[] result = new String[3];

    InputStream stationsFIS;
    InputStream distancesFIS;
    InputStream transfersFIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found);

        Intent intent = getIntent();
        int size = Integer.parseInt(intent.getStringExtra("SIZE"));
        Log.d("FOUND/SIZE", Integer.toString(size));

        information = new String[size + 5];

        for(int i = 0; i < size + 5; i++) {
            information[i] = intent.getStringExtra(Integer.toString(i));
            Log.d("FOUND/INFO", information[i]);
        }

        MapView mapView = new MapView(FoundActivity.this);
        ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.found_map_view);
        mapViewContainer.addView(mapView);

        String[] json = new String[3];
        json = getJSONString();

        stationsJSONParsing(json[0]);
        distancesJSONParsing(json[1]);
        transferJSONParsing(json[2]);

        // background = new Background();
        // background.execute(information);
        try {
            Main main = new Main(information, stations, station_distances, transfer_infos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        result = Main.getResult();

        if(result[0] != null && result[1] != null && result[2] != null) {
            midpointXY[0] = Float.parseFloat(result[0]);
            midpointXY[1] = Float.parseFloat(result[1]);
            midpoint = result[2];

            // doInBackground()에서 리턴받은 result 값 사용
            // 카카오 맵 띄우기
            Log.d("FOUND/API", "ENTERED");

            // 중심점 변경
            mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]), true);

            // 줌 레벨 변경
            mapView.setZoomLevel(1, true);

            MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]);
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName("Midpoint Station");
            marker.setTag(1);
            marker.setMapPoint(MARKER_POINT);
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.icon_mini_marker);
            marker.setCustomImageAutoscale(true);
            marker.setCustomImageAnchor(0.5f, 1.0f);
            mapView.addPOIItem(marker);

            TextView station = findViewById(R.id.found_midpoint_station_tv);
            station.setText(midpoint);
        } else {
            Log.d("FOUND/RESULT", "null");
        }

        Button homeBtn = findViewById(R.id.found_icon_home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(FoundActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String[] getJSONString() {
        String[] json = new String[3];
        AssetManager assetManager = getAssets();

        try {
            stationsFIS = assetManager.open("jsons/stations.json");
            distancesFIS = assetManager.open("jsons/station_distances.json");
            transfersFIS = assetManager.open("jsons/transfer.json");

            int stationSize = stationsFIS.available();
            int distancesSize = distancesFIS.available();
            int transfersSize = transfersFIS.available();

            byte[] stationBuffer = new byte[stationSize];
            stationsFIS.read(stationBuffer);
            stationsFIS.close();

            byte[] distancesBuffer = new byte[distancesSize];
            distancesFIS.read(distancesBuffer);
            distancesFIS.close();

            byte[] transfersBuffer = new byte[transfersSize];
            transfersFIS.read(transfersBuffer);
            transfersFIS.close();

            String stationJSON = new String(stationBuffer, "UTF-8");
            String distancesJSON = new String(distancesBuffer, "UTF-8");
            String transferJSON = new String(transfersBuffer, "UTF-8");

            json[0] = stationJSON;
            json[1] = distancesJSON;
            json[2] = transferJSON;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void stationsJSONParsing(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray stationArray = jsonObject.getJSONArray("Stations");

            stations = new Station_info[stationArray.length()];

            for(int i = 0; i < stationArray.length(); i++) {
                JSONObject stationObj = stationArray.getJSONObject(i);

                Station_info temp = new Station_info();

                temp.setLineInfo(stationObj.getString("Line_Info"));
                temp.setStationName(stationObj.getString("Station_name"));

                temp.setLat(Float.parseFloat(stationObj.getString("lat")));
                temp.setLon(Float.parseFloat(stationObj.getString("lon")));

                temp.setCafe(Integer.parseInt(stationObj.getString("cafe")));
                temp.setStore(Integer.parseInt(stationObj.getString("store")));
                temp.setSumAround(Integer.parseInt(stationObj.getString("sum_around")));
                temp.setStationNum(Integer.parseInt(stationObj.getString("station_num")));

                temp.setDptstore(Boolean.parseBoolean(stationObj.getString("dptstore")));
                temp.setCinema(Boolean.parseBoolean(stationObj.getString("cinema")));
                temp.setIsFinal(Boolean.parseBoolean(stationObj.getString("Is_Final")));

                stations[i] = temp;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void distancesJSONParsing(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray distanceArray = jsonObject.getJSONArray("Stations_Distances");

            station_distances = new Station_distance[distanceArray.length()];

            for(int i = 0; i < distanceArray.length(); i++) {
                JSONObject distanceObj = distanceArray.getJSONObject(i);

                Station_distance temp = new Station_distance();

                temp.setLine_info(distanceObj.getString("Line_info"));
                temp.setDpt_Station(distanceObj.getString("Dpt_Station"));
                temp.setDest_Station(distanceObj.getString("Dest_Station"));
                temp.setDistance(Float.parseFloat(distanceObj.getString("Distance")));

                station_distances[i] = temp;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void transferJSONParsing(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray transferArray = jsonObject.getJSONArray("Transfer");

            transfer_infos = new Transfer_info[transferArray.length()];

            for(int i = 0; i < transferArray.length(); i++) {
                JSONObject transferObj = transferArray.getJSONObject(i);

                Transfer_info temp = new Transfer_info();

                temp.setLine_info(transferObj.getString("Line_info"));
                temp.setStation_name(transferObj.getString("Station_name"));
                temp.setTransfer_line(transferObj.getString("Transfer_line"));
                temp.setTransfer_value(Float.parseFloat(transferObj.getString("Transfer_value")));

                transfer_infos[i] = temp;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    class Background extends AsyncTask<String, String[], String[]> {
        private String[] result = new String[3];
        Main main;

        // 스레드의 백그라운드 작업 구현
        @Override
        protected String[] doInBackground(String... strings) {
            try {
                // main = new Main(information);
                result = Main.getResult();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("FOUND/RESULT", result[0] + ", " + result[1] + ", " + result[2]);
            return result;
        }

        // 스레드 취소되었을 때 호출
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            MapView mapView = new MapView(FoundActivity.this);
            ViewGroup mapViewContainer = (ViewGroup)findViewById(R.id.found_map_view);
            mapViewContainer.addView(mapView);

            if(result[0] != null && result[1] != null && result[2] != null) {
                midpointXY[0] = Float.parseFloat(result[0]);
                midpointXY[1] = Float.parseFloat(result[1]);
                midpoint = result[2];

                // doInBackground()에서 리턴받은 result 값 사용
                // 카카오 맵 띄우기
                Log.d("FOUND/API", "ENTERED");

                // 중심점 변경
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]), true);

                MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(midpointXY[0], midpointXY[1]);
                MapPOIItem marker = new MapPOIItem();

                marker.setItemName("Midpoint Station");
                marker.setTag(0);
                marker.setMapPoint(MARKER_POINT);
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                mapView.addPOIItem(marker);

                TextView station = findViewById(R.id.found_midpoint_station_tv);
                station.setText(midpoint);
            }
        }
    }
}