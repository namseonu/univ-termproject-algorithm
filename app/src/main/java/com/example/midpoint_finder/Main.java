package com.example.midpoint_finder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Arrays;
import java.util.Comparator;

public class Main {
    static String url = "jdbc:mysql://localhost:3306/algorithm_termproject?useUnicode=true&characterEncoding=utf8";
    static String userName = "root";
    static String password = "12345";
    static final int INF = 10000000;
    static int userNum;
    static user[] users;
    static Station_info[] stations;
    static Station_distance[] station_distances;
    static Transfer_info[] transfer_infos;
    static Sum[] distance_sum;
    static boolean Cafe;
    static boolean Store;
    static boolean Both;
    static boolean Dept_store;
    static boolean Cinema;
    static String[] result = new String[3];

    public Main(String[] information) throws Exception {
        this.userNum = Integer.parseInt(information[0]);

        // mysql 서버와 연결. 이 connection을 통해 쿼리 보내고 결과 받음
        // Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, userName, password);

        users = new user[userNum];
        Station_Info_setter(connection);
        Station_distance_setter(connection);
        Transfer_info_setter(connection);

        for(int i = 1; i <= userNum; i++) {
            String tempName = information[i];
            Station_info init_station = null;
            do {
                init_station = checker(tempName);
            }while(init_station == null);
            users[i] = new user(init_station);
            Log.d("MAIN_JAVA", users[i].Current_station.Station_name);
        }

        Member[][] members = new Member[281][281];
        String [][] lines = new String[281][281];
        distance_sum = new Sum[281];
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_distance");
        ResultSet resultSet = statement.executeQuery();
        Station_info Pivot = null, Changes = null;

        for(int i=0; i<281; i++) {
            for(int k=0; k<281; k++) {
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

        for(int i=0; i<281; i++) {
            Pivot = stations[i];    //인덱스로 접근해서 역 정보 할당
            for(int k=0; k<281; k++) {
                // members[i][k] = new Member();
                // 방향성이 없기 때문에 역으로도 저장이 되어야 한다. 따라서 초기화를 이렇게 따로따로 해주면 안된다.
                Changes = stations[k];
                Find_dist(statement.executeQuery(), members, Pivot, Changes,i, k, lines);
            }
        }
        floydalgorithm(members, lines);
    }

    static void Find_dist(ResultSet RS, Member[][] members, Station_info Pivot, Station_info Changes,
                          int i, int k, String[][] lines) throws SQLException{
        String From, To;
        float dist;
        // 전체 쿼리문에서 출발역, 도착역 이름으로 tuple 찾아서 환승값 추가
        while(RS.next()) {
            From = RS.getString(2);
            To = RS.getString(3);
            dist = RS.getFloat(4);
            if(Pivot.Station_name.equals(Changes.Station_name)) {
                members[i][k].dist = 0;
                members[k][i].dist = 0;
                lines[i][k] = Pivot.Line_Info.substring(0,1);
                break;
            }
            else if((From.equals(Pivot.Station_name) && To.equals(Changes.Station_name)) ||
                    ((From.equals(Changes.Station_name)) && (To.equals(Pivot.Station_name)))){
                members[i][k].Line_info = Pivot.Line_Info;
                members[i][k].dist = dist;
                members[k][i].Line_info = Pivot.Line_Info;
                members[k][i].dist = dist;
                lines[i][k] = Pivot.Line_Info.substring(0,1);
                break;
            }
        }
    }

    static void Station_Info_setter(Connection connection) throws SQLException {
        PreparedStatement TogetSize = connection.prepareStatement("SELECT count(*) FROM station_info");
        ResultSet Size =  TogetSize.executeQuery();
        while(Size.next())
            stations = new Station_info[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_info");
        ResultSet resultSet = statement.executeQuery();

        String tempLine, name;
        float lat, lon;
        int cafe, store, sum_around;
        boolean dptstore, cinema, is_final;
        int iter = 0;
        while(resultSet.next()) {
            tempLine = resultSet.getString(1);
            name = resultSet.getString(2);
            lat = resultSet.getFloat(3);
            lon = resultSet.getFloat(4);
            cafe = resultSet.getInt(5);
            store = resultSet.getInt(6);
            sum_around = resultSet.getInt(7);
            dptstore = resultSet.getBoolean(8);
            cinema = resultSet.getBoolean(9);
            is_final = resultSet.getBoolean(10);
            stations[iter++] = new Station_info(tempLine, name, lat, lon, cafe,
                    store, sum_around, dptstore, cinema, is_final, iter-1);
        }
    }

    static void Station_distance_setter(Connection connection) throws SQLException{
        PreparedStatement TogetSize = connection.prepareStatement("SELECT count(*) FROM station_distance");
        ResultSet Size = TogetSize.executeQuery();
        while(Size.next())
            station_distances = new Station_distance[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM station_distance");
        ResultSet resultSet = statement.executeQuery();

        String Line_info;
        String Dpt_Station, Dest_Station;
        float Distance;
        int iter = 0;
        while(resultSet.next()) {
            Line_info = resultSet.getString(1);
            Dpt_Station = resultSet.getString(2);
            Dest_Station = resultSet.getString(3);
            Distance = resultSet.getFloat(4);
            station_distances[iter++] = new Station_distance(Line_info, Dpt_Station, Dest_Station, Distance);
        }
    }

    static void Transfer_info_setter(Connection connection) throws SQLException{
        PreparedStatement togetSize = connection.prepareStatement("SELECT count(*) FROM transfer_info");
        ResultSet Size = togetSize.executeQuery();
        while(Size.next())
            transfer_infos = new Transfer_info[Size.getInt(1)];

        PreparedStatement statement = connection.prepareStatement("SELECT * FROM transfer_info");
        ResultSet resultSet = statement.executeQuery();
        String Line_info, Station_name, Transfer_line;
        float Transfer_value;
        int iter = 0;
        while(resultSet.next()) {
            Line_info = resultSet.getString(1);
            Station_name = resultSet.getString(2);
            Transfer_line = resultSet.getString(3);
            Transfer_value = resultSet.getFloat(4);
            transfer_infos[iter++] = new Transfer_info(Line_info, Station_name, Transfer_line, Transfer_value);
        }
    }

    static void floydalgorithm(Member[][] w, String[][] line_num) {
        int maxnum = 281;
        int tw = 0;
        for(int k=0; k<maxnum; k++)
        {
            for(int i=0; i<maxnum; i++)
            {
                for(int j=0; j<maxnum; j++)
                {
                    if(w[i][j].dist > w[i][k].dist + w[k][j].dist && line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist;
                    else if(w[i][j].dist > w[i][k].dist + w[k][j].dist + tw && !line_num[j][k].equals(line_num[i][j]))
                        w[i][j].dist = w[i][k].dist + w[k][j].dist + tw;
                }
            }
        }

        // 이제 2차원 배열이 준비되었습니다.
        calc_fair(w);
    }

    static void print(Member[][] w) {
        for(int i=0; i<281; i++) {
            for(int k=0; k<281; k++) {
                System.out.println(stations[i].Station_name + " -> " + stations[k].Station_name +
                        " : " + w[i][k].dist);
            }
        }
    }

    static void calc_fair(Member[][] members) {
        int index = 0;
        float distSum;
        int[] start_indexes = new int[userNum];
        int dest_index = 0;
        for(int i=0; i<281; i++) {
            distSum = 0;
            for(int k=0; k<userNum; k++) {
                index = users[k].Current_station.station_num;
                // 각 유저의 출발지로부터 한 역까지 걸리는 모든 가중치를 더한다.
                // k번째 유저의 출발지는 index
                start_indexes[k] = index;
                // k번째 유저의 도착지는 i
                dest_index = i;
                distSum += members[index][i].dist;
                distSum += around_operator(i);
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

        // 그리고 (일단은) 상위 10개의 값 중에서 분산을 비교한다.
        // (값 - 값들의 평균)^2의 평균
        // 메서드화 해야할 것 같다.
        float tempSum;
        float tempAVG;
        float min = INF;
        float resultX, resultY;
        resultX = resultY = 0;
        for(int i=0; i<40; i++) {
            tempSum = 0;
            tempAVG = 0;
            for(int k=0; k<userNum; k++) {
                tempSum += members[distance_sum[i].start_pos[k]][distance_sum[i].dest_pos].dist;
            }
            // 합을 구했으니 이를 바탕으로 평균을 구하고 나아가 분산을 구하겠습니다.
            tempAVG = tempSum / userNum;
            tempSum = 0;
            for(int j=0; j<userNum; j++) {
                tempSum += Math.pow
                        (Math.round(tempAVG - members[distance_sum[i].start_pos[j]][distance_sum[i].dest_pos].dist
                        * 100) / 100.0,2);  //소수점 아래 둘째 자리까지만. 그렇지 않을 경우
                // around_operator 호출 예정
            }
            tempAVG = tempSum/userNum;
            // 분산을 구했습니다. 이 정보들을 최소값과 비교하고 저장해주겠습니다.
            if(tempAVG < min) {
                min = tempAVG;
                resultX = stations[distance_sum[i].dest_pos].lat;
                resultY = stations[distance_sum[i].dest_pos].lon;
                // System.out.println(get_station_by_pos(resultX, resultY).Station_name + " "
                //       + members[get_station_by_pos(resultX, resultY).station_num][distance_sum[i].start_pos[0]].dist
                // + " " + members[get_station_by_pos(resultX, resultY).station_num][distance_sum[i].start_pos[1]].dist
                // + " " + tempAVG);
                // 일단 resultX와 resultY가 유효한 값들로 어느 정도 비교가 되는 것을 확인하였습니다.
            }
        }
        // System.out.println(get_station_by_pos(resultX, resultY).Station_name);
        Log.d("MAIN_JAVA", get_station_by_pos(resultX, resultY).Station_name);

        result[0] = Float.toString(resultX);
        result[1] = Float.toString(resultY);
        result[2] = get_station_by_pos(resultX, resultY).Station_name;
    }

    //체크된 옵션에 따라 가중치를 달리 할당해주는 메서드.
    static float around_operator(int index) {
        float around_value = 0;
        if(Cafe)
            around_value += stations[index].cafe * 0.1;
        if(Store)
            around_value += stations[index].store * 0.1;
        if(!Cafe && !Store && Both)
            around_value += stations[index].sum_around * 0.1;
        if(Dept_store)
            around_value += stations[index].dptstore ? 0 : INF;
        if(Cinema)
            around_value += stations[index].cinema ? 0 : INF;
        return around_value;
    }

    static Station_info checker(String tempName) {
        for(Station_info temp : stations) {
            if(temp.Station_name.equals(tempName)) {
                return temp;
            }
        }

        Log.d("MAIN_JAVA", ":: Invalid station name ::");
        // System.out.println(":: Invalid station name :: ");
        return null;
    }

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

    public static String[] getResult() {
        return result;
    }
}

class Station_distance {
    String Line_info;
    String Dpt_Station, Dest_Station;
    float Distance;
    public Station_distance(String Line_info, String Dpt_Station, String Dest_Station, float Distance) {
        this.Line_info = Line_info;
        this.Dpt_Station = Dpt_Station;
        this.Dest_Station = Dest_Station;
        this.Distance = Distance;
    }
}

class Station_info {
    String Line_Info;
    String Station_name;
    float lat, lon;
    int cafe, store, sum_around;
    int station_num;
    boolean dptstore, cinema, Is_Final;
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
}

class Transfer_info {
    String Line_info;
    String Station_name;
    String Transfer_line;
    float Transfer_value;
    public Transfer_info(String Line_info, String Station_name, String Transfer_line, float Transfer_value) {
        this.Line_info = Line_info;
        this.Station_name = Station_name;
        this.Transfer_line = Transfer_line;
        this.Transfer_value = Transfer_value;
    }
}

// user 클래스. 처음 출발하는 역 노드, 누적 거리값, 여태 지나온 역들을 담을 queue를 가진다.
class user {
    // 일단은 최초에 찾은 하나의 역만을 갖는다. 후에 문제가 생기면 리스트로 바꾸던가 해야겠다.
    Station_info Current_station;
    public user(Station_info Current_station) {
        this.Current_station = Current_station;
    }
}

class Member {
    String Line_info;
    float dist;
    public Member() {
        Line_info = null;
        dist = 0.0f;
    }
}

class Sum {
    // 유저의 리스트를 가지고 있어야 정렬 후 분산을 판단할 수 있습니다.
    // 정정 : 굳이 유저의 리스트가 아니더라도 int의 배열로 이를 똑같이 구현할 수 있습니다.
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