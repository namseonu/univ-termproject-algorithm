package com.example.midpoint_finder;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class Main extends AppCompatActivity {

    static final int INF = 10000000;
    static final int MAX = 281;
    static String[] result = new String[3];
    static int userNum;
    static String[] tempInformation;
    static user[] users;
    static Station_info[] stations;
    static Station_distance[] station_distances;
    static Transfer_info[] transfer_infos;
    static Sum[] distance_sum;

    static boolean Cafe;
    static boolean Store;
    // static boolean Both;
    static boolean Dept_store;
    static boolean Cinema;

    InputStream stationsFIS;
    InputStream distancesFIS;
    InputStream transfersFIS;

    public Main(String[] information, Station_info[] stations, Station_distance[] station_distances, Transfer_info[] transfer_infos) throws Exception {
        this.userNum = Integer.parseInt(information[0]);
        tempInformation = new String[userNum];
        System.out.println(information[0]);
        Log.d("MAIN/USER_NUM", information[0]);

        for(int i = 0; i < userNum; i++) {
            tempInformation[i] = information[i + 1];
            System.out.println(tempInformation[i]);
            Log.d("MAIN/INFO", tempInformation[i]);
        }

        Cafe = Boolean.parseBoolean(information[userNum + 1]);
        Store = Boolean.parseBoolean(information[userNum + 2]);
        // if(Cafe == true && Store == true) Main.Both = true;
        Dept_store = Boolean.parseBoolean(information[userNum + 3]);
        Cinema = Boolean.parseBoolean(information[userNum + 4]);

        Log.d("MAIN/CAFE", Boolean.toString(Cafe));
        Log.d("MAIN/STORE", Boolean.toString(Store));
        Log.d("MAIN/DEPT", Boolean.toString(Dept_store));
        Log.d("MAIN/CINEMA", Boolean.toString(Cinema));

        this.stations = stations;
        this.station_distances = station_distances;
        this.transfer_infos = transfer_infos;

        // readFile();
        // readJSONFile();

        // Main.stations = stations;
        // Main.station_distances = station_distances;
        // Main.transfer_infos = transfer_infos;

        start();
    }

//    public void readFile() throws IOException, ClassNotFoundException {
//        // InputStream stationsFIS = getResources().openRawResource(R.raw.stations);
//        // InputStream distancesFIS = getResources().openRawResource(R.raw.station_distances);
//        // InputStream transfersFIS = getResources().openRawResource(R.raw.transfer);
//
//        ObjectInputStream stationsOIS = new ObjectInputStream(stationsFIS);
//        ObjectInputStream distancesOIS = new ObjectInputStream(distancesFIS);
//        ObjectInputStream transfersOIS = new ObjectInputStream(transfersFIS);
//
//        int data = 0;
//        int i = 0;
//        try {
//            while(true) {
//                Main.stations[i] = (Station_info) stationsOIS.readObject();
//                i++;
//            }
//        } catch(EOFException e) { }
//
//        data = 0;
//        i = 0;
//        try {
//            while(true) {
//                Main.station_distances[i] = (Station_distance) distancesOIS.readObject();
//                i++;
//            }
//        } catch(EOFException e) { }
//
//        data = 0;
//        i = 0;
//        try {
//            while(true) {
//                Main.transfer_infos[i] = (Transfer_info) transfersOIS.readObject();
//                i++;
//            }
//        } catch(EOFException e) { }
//
//        stationsFIS.close();
//        distancesFIS.close();
//        transfersFIS.close();
//
//        stationsOIS.close();
//        distancesOIS.close();
//        transfersOIS.close();
//    }

    public static void start() {
        users = new user[userNum];

        for(int i = 0; i < userNum; i++) {
            Station_info init_station = null;

            do {
                init_station = checker(tempInformation[i]);
            } while(init_station == null);

            users[i] = new user(init_station);
            Log.d("MAIN/STATION", users[i].Current_station.Station_name);
        }

        // members information & lines information
        Member[][] members = new Member[MAX][MAX];
        String[][] lines = new String[MAX][MAX];
        distance_sum = new Sum[MAX];
        Station_info Pivot = null, Changes = null;

        for(int i = 0; i < MAX; i++) {
            for(int k = 0; k < MAX; k++) {
                members[i][k] = new Member();
                if(i == k)
                    members[i][k].dist = 0;
                else {
                    members[i][k].Line_info = "INF";
                    members[i][k].dist = INF;
                }
                lines[i][k] = "INF";
            }
        }

        Line_init(lines);

        for(int i = 0; i < MAX; i++) {
            Pivot = stations[i];    // 인덱스로 접근해서 역 정보 할당
            for(int k = 0; k < MAX; k++) {
                // members[i][k] = new Member();
                // 방향성이 없기 때문에 역으로도 저장이 되어야 한다. 따라서 초기화를 이렇게 따로따로 해주면 안된다.
                Changes = stations[k];
                Find_dist(members, Pivot, Changes, i, k, lines);
            }
        }

        // floydAlgorithm은 환승을 고려하지 않습니다.
        // floydAlgorithm(members, lines);
        // floydAlgorithm2는 환승을 고려합니다.
        floydAlgorithm2(members, lines);
    }

    static void Find_dist(Member[][] members, Station_info Pivot, Station_info Changes,
                          int i, int k, String[][] lines) {
        String From, To; // 출발역, 도착역 이름으로 해당 객체 찾아서 환승값 추가
        float dist;

        for(int j = 0; j < station_distances.length; j++) {
            From = station_distances[j].Dpt_Station;
            To = station_distances[j].Dest_Station;
            dist = station_distances[j].Distance;

            if(Pivot.Station_name.equals(Changes.Station_name)) {
                members[i][k].dist = 0;
                members[k][i].dist = 0;
                // lines[i][k] = Pivot.Line_Info.substring(0,1);
                break;
            }
            else if((From.equals(Pivot.Station_name) && To.equals(Changes.Station_name)) ||
                    ((From.equals(Changes.Station_name)) && (To.equals(Pivot.Station_name)))){
                members[i][k].Line_info = Pivot.Line_Info;
                members[i][k].dist = dist;
                members[k][i].Line_info = Pivot.Line_Info;
                members[k][i].dist = dist;
                // lines[i][k] = Pivot.Line_Info.substring(0,1);
                break;
            }
        }
    }

    // 메인 알고리즘 파트입니다.
    static void floydAlgorithm2 (Member[][] w, String[][] line_num) {
        boolean flag = false;

        for(int k = 0; k < MAX; k++)
        {
            for(int i = 0; i < MAX; i++)
            {
                for(int j = 0; j < MAX; j++)
                {
                    if(w[i][j].dist > w[i][k].dist + w[k][j].dist && line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist;
                    else if(w[i][j].dist > w[i][k].dist + w[k][j].dist &&
                            !line_num[j][k].equals(line_num[i][j])) {
                        // 환승 지점은 k 입니다.
                        float tw = 0;
                        // 환승 테이블에서 정보를 찾는 부분입니다.
                        flag = false;
                        for (int p = 0; p < 90; p++) {
                            if(transfer_infos[p].Station_name.equals(stations[k].Station_name)
                                    && transfer_infos[p].Transfer_line.equals(stations[j].Line_Info) &&
                                    transfer_infos[p].Line_info.equals(stations[i].Line_Info)) {
                                // 해당하는 값을 환승 테이블에서 찾았다면 가져와서 더해줍니다.
                                tw = transfer_infos[p].Transfer_value;
                                w[i][j].dist = w[i][k].dist + w[k][j].dist + tw;
                                flag = true;
                                break;
                            }
                        }
                        if(!flag){
                            if(w[i][j].dist > w[i][k].dist + w[k][j].dist)
                                w[i][j].dist = w[i][k].dist + w[k][j].dist;
                        }
                    }
                }
            }
        }

        // 이제 2차원 배열이 준비되었습니다.
        calc_fair(w);
    }

    static void floydAlgorithm(Member[][] w, String[][] line_num) {
        int tw = 0;

        for(int k = 0; k < MAX; k++)
        {
            for(int i = 0; i < MAX; i++)
            {
                for(int j = 0; j < MAX; j++)
                {
                    if(w[i][j].dist > w[i][k].dist + w[k][j].dist && line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist;
                    else if(w[i][j].dist > w[i][k].dist + w[k][j].dist + tw && !line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist + tw;
                }
            }
        }

        calc_fair(w);
    }

    // 호선의 정보만을 담는 배열을 초기화하는 함수입니다.
    static void Line_init(String[][] line_num) {
        for(int x = 0; x < MAX; x++) {
            for(int y = 0; y < MAX; y++) {
                line_num[x][y] = stations[y].Line_Info;
            }
        }
    }

    // 채워진 테이블을 바탕으로 최적의 역을 찾아내는 함수입니다.
    static void calc_fair(Member[][] members) {
        int index = 0;
        float distSum;
        int[] start_indexes = new int[userNum];
        int dest_index = 0;

        for(int i = 0; i < MAX; i++) {
            distSum = 0;

            for(int k = 0; k < userNum; k++) {
                index = users[k].Current_station.station_num;
                // 각 유저의 출발지로부터 한 역까지 걸리는 모든 가중치를 더합니다.
                // k번째 유저의 출발지는 index입니다.
                start_indexes[k] = index;
                // k번째 유저의 도착지는 i
                dest_index = i;
                distSum += members[index][i].dist;
                distSum += around_operator(i);
                distSum = Math.round(distSum * 100 / 100.0);
            }
            // i역에 대해서 모든 유저들의 가중치 총 합을 저장.
            distance_sum[i] = new Sum(start_indexes, dest_index, distSum);
        }

        // 총합 값의 저장이 끝났으면 정렬 후 분산을 구한다.
        // 먼저 정렬을 수행한다.
        Arrays.sort(distance_sum, new Comparator<Sum>() {
            @Override
            public int compare(Sum o1, Sum o2) {
                if(o1.dist_sum < o2.dist_sum)
                    return -1;
                else
                    return 1;
            }
        });

        // 그리고 (일단은) 상위 n개의 값 중에서 분산을 비교한다.
        // (값 - 값들의 평균)^2의 평균
        float tempSum;
        float tempAVG;
        float min = INF;
        float resultX, resultY;
        resultX = resultY = 0;

        // 거리와 분산 모두 공평하게 고려하기 최적의 값은 80 근처로 추정됩니다.
        for(int i = 0; i < 80; i++) {
            tempSum = 0;
            tempAVG = 0;

            for(int k = 0; k < userNum; k++) {
                tempSum += members[distance_sum[i].start_pos[k]][distance_sum[i].dest_pos].dist;
            }
            tempSum = Math.round(tempSum * 100 / 100.0);

            // 합을 구했으니 이를 바탕으로 평균을 구하고 나아가 분산을 구하겠습니다.
            tempAVG = tempSum / userNum;
            tempSum = 0;
            for(int j = 0; j < userNum; j++) {
                tempSum += Math.pow
                        (Math.round((tempAVG - members[distance_sum[i].start_pos[j]][distance_sum[i].dest_pos].dist)
                                * 100) / 100.0,2);  // 소수점 아래 둘째 자리까지만 고려합니다.
            }

            tempAVG = tempSum / userNum;

            // 분산을 구했습니다. 이 정보들을 최소값과 비교하고 저장해주겠습니다.
            tempAVG = Math.round(tempAVG * 100 / 100.0);

            // 분산이 0이라는 것은 무언가 잘못됐음을 의미합니다. 과감하게 제외하도록 하겠습니다.
            if(tempAVG == 0)
                continue;
            if(tempAVG < min) {
                min = tempAVG;
                resultX = stations[distance_sum[i].dest_pos].lat;
                resultY = stations[distance_sum[i].dest_pos].lon;
            }
        }
        Log.d("MAIN/RESULT", get_station_by_pos(resultX, resultY).Station_name);

        result[0] = Float.toString(resultX);
        result[1] = Float.toString(resultY);
        result[2] = get_station_by_pos(resultX, resultY).Station_name;
    }

    public static String[] getResult() {
        return result;
    }

    // 체크된 옵션에 따라 가중치를 달리 할당해주는 메서드입니다.
    static float around_operator(int index) {
        float around_value = 0;
        if(Cafe)
            around_value += stations[index].cafe * 0.1;
        if(Store)
            around_value += stations[index].store * 0.1;
//        if(Cafe && Store)
//            around_value += stations[index].sum_around * 0.1;
        if(Dept_store)
            around_value += stations[index].dptstore ? 0 : INF;
        if(Cinema)
            around_value += stations[index].cinema ? 0 : INF;
        return around_value;
    }

    // 매개변수로 넘긴 역의 이름이 존재하는 역인지 확인하는 함수입니다.
    static Station_info checker(String tempName) {
        for(Station_info temp : stations) {
            if(temp.Station_name.equals(tempName)) {
                return temp;
            }
        }
        Log.d("MAIN/CHECKER", "Invalid Station Name");
        return null;
    }

    // 최종 결과로 얻은 좌표값을 역 이름으로 바꿔서 반환하는 함수입니다.
    static Station_info get_station_by_pos(float x, float y) {
        for(Station_info temp : stations) {
            if(temp.lat == x && temp.lon == y)
                return temp;
        }
        return null;
    }

    // getter
    public Station_info[] getStations() {
        return stations;
    }

    public static Station_distance[] getStation_distances() {
        return station_distances;
    }

    public static Transfer_info[] getTransfer_infos() {
        return transfer_infos;
    }
}

// 사용자의 정보를 담을 class 입니다.
class user {
    Station_info Current_station;

    public user(Station_info Current_station) {
        this.Current_station = Current_station;
    }
}

// Floyd-warshall 테이블의 tuple 하나하나를 이룰 데이터의 class입니다.
class Member {
    String Line_info;
    float dist;

    public Member() {
        Line_info = null;
        dist = 0.0f;
    }
}

// 거리의 합 값과 더불어 출발지와 도착지의 정보를 함께 가지고있어야 하기 때문에 별도의 class로 정의하였습니다.
class Sum {
    int[] start_pos;
    int dest_pos;
    // 정렬은 이 dist_sum을 기준으로 이루어집니다.
    float dist_sum;

    public Sum(int[] start_pos, int dest_pos, float dist_sum) {
        this.start_pos = start_pos;
        this.dist_sum = dist_sum;
        this.dest_pos = dest_pos;
    }
}

class Station_distance implements Serializable {
    String Line_info;
    String Dpt_Station, Dest_Station;
    float Distance;

    public Station_distance() {}

    public Station_distance(String Line_info, String Dpt_Station, String Dest_Station, float Distance) {
        this.Line_info = Line_info;
        this.Dpt_Station = Dpt_Station;
        this.Dest_Station = Dest_Station;
        this.Distance = Distance;
    }

    public void setLine_info(String Line_info) {
        this.Line_info = Line_info;
    }

    public void setDpt_Station(String Dpt_Station) {
        this.Dpt_Station = Dpt_Station;
    }

    public void setDest_Station(String Dest_Station) {
        this.Dest_Station = Dest_Station;
    }

    public void setDistance(float Distance) {
        this.Distance = Distance;
    }
}

// 역 정보를 담을 class 입니다.
class Station_info implements Serializable {
    String Line_Info;
    String Station_name;
    float lat, lon;
    int cafe, store, sum_around;
    int station_num;
    boolean dptstore, cinema, Is_Final;

    public Station_info() {}

    public Station_info(String Line_Info, String Station_name, float x, float y, int cafe, int store,
                        int sum_around, boolean dptstore, boolean cinema, boolean Is_Final, int station_num) {
        this.Line_Info = Line_Info;
        this.Station_name = Station_name;
        this.lat = x;
        this.lon = y;
        this.cafe = cafe;
        this.store = store;
        this.sum_around = sum_around;
        this.dptstore = dptstore;
        this.cinema = cinema;
        this.Is_Final = Is_Final;
        this.station_num = station_num;
    }

    public void setLineInfo(String Line_Info) {
        this.Line_Info = Line_Info;
    }

    public void setStationName(String Station_name) {
        this.Station_name = Station_name;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public void setCafe(int cafe) {
        this.cafe = cafe;
    }

    public void setStore(int store) {
        this.store = store;
    }

    public void setSumAround(int sum_around) {
        this.sum_around = sum_around;
    }

    public void setStationNum(int station_num) {
        this.station_num = station_num;
    }

    public void setDptstore(Boolean dptstore) {
        this.dptstore = dptstore;
    }

    public void setCinema(Boolean cinema) {
        this.cinema = cinema;
    }

    public void setIsFinal(Boolean Is_Final) {
        this.Is_Final = Is_Final;
    }
}

// 환승 정보를 담을 class 입니다.
class Transfer_info implements Serializable {
    String Line_info;
    String Station_name;
    String Transfer_line;
    float Transfer_value;

    public Transfer_info() {}

    public Transfer_info(String Line_info, String Station_name, String Transfer_line, float Transfer_value) {
        this.Line_info = Line_info;
        this.Station_name = Station_name;
        this.Transfer_line = Transfer_line;
        this.Transfer_value = Transfer_value;
    }

    public void setLine_info(String Line_info) {
        this.Line_info = Line_info;
    }

    public void setStation_name(String Station_name) {
        this.Station_name = Station_name;
    }

    public void setTransfer_line(String Transfer_line) {
        this.Transfer_line = Transfer_line;
    }

    public void setTransfer_value(float Transfer_value) {
        this.Transfer_value = Transfer_value;
    }
}
