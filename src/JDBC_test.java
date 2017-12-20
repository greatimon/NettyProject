import com.google.gson.Gson;
import org.json.JSONObject;

import java.sql.*;

public class JDBC_test extends Thread implements Runnable{

    private Connection conn;

    private JDBC_test() {}

    @Override
    public void run() {
        try{
            Class.forName("org.postgresql.Driver");

            String password = "dydska11";
            String user = "greatimon";
            String url = "jdbc:postgresql://localhost:5432/remotemeeting";

            conn = DriverManager.getConnection(url, user, password);

            System.out.println("데이터베이스 db에 성공적으로 접속했습니다");

            // select 테스트
            select();

        } catch (ClassNotFoundException cnfe){
            System.out.println("Could not find the JDBC drive");
            System.exit(1);
        }
        catch (SQLException sqle) {
            System.out.println("Could not connect");
            System.exit(1);
        }
    }

    public void select() {
        try {
            Statement stmt = conn.createStatement();
            String sql = "select * from users";
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()) {
                // gson 객체 생성
                Gson gson = new Gson();
                // resultSet 값을 담을 JSONObject 객체 생성
                JSONObject jsonObject = new JSONObject();
                // resultSet 의 메타데이터 값을 가져옴
                ResultSetMetaData rmd = rs.getMetaData();

                // 메타데이터로 칼럼의 이름, 그 칼럼의 이름으로 resultSet의 벨류 값을 가져와서
                // JSONObject 에 하나씩 쌓음
                for(int i=1; i<=rmd.getColumnCount(); i++) {
                    jsonObject.put(rmd.getColumnName(i),rs.getString(rmd.getColumnName(i)));
                }

                // 해당 JSONObject 를 Gson 과 String 객체를 이용해서 user 객체로 변환
                String jsonChatlog = jsonObject.toString();

                Users user = gson.fromJson(jsonChatlog, Users.class);

                // 결과 값 확인하기
                System.out.println(user.getUser_no());
                System.out.println(user.getJoin_path());
                System.out.println(user.getJoin_dt());
                System.out.println(user.getUser_email());
                System.out.println(user.getUser_pw());
                System.out.println(user.getUser_nickname());
                System.out.println(user.getPresent_meeting_in_ornot());
                System.out.println(user.getUser_img_filename());
                System.out.println(user.getExtra());
                System.out.println(user.getAndroid_id());
                System.out.println("=============================");

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e);
        }
        finally {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception: " + e);
            }
        }


    }

    public static void main(String[] args) {
        JDBC_test jdbc_test = new JDBC_test();
        jdbc_test.start();
    }
}
