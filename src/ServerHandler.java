import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.JSONObject;

import java.io.StringReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private Gson gson;

    ServerHandler() {
        gson = new GsonBuilder().setLenient().create();
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> handlerAdded -- 채널이 등록되었을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: Handler added");
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelActive -- 채널이 활성화가 되었을 때 호출되는 콜백
     접속한 클라이언트에게 정상접속되었음을 알리는 메세지 보내기
     ---------------------------------------------------------------------------*/
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: "+ctx.channel().remoteAddress() + ", 클라이언트 접속");

        /** 클라이언트에게 채팅 서버 접속 완료되었다는 콜백메세지 보내기 */
        Data_for_netty call_back_data = new Data_for_netty();
        call_back_data.setNetty_type("conn");
        call_back_data.setSubType("call_back");
        // 통신 전송 메소드 호출
        send_to_client(ctx.channel(), call_back_data);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        /** 통신메세지 received - 접속한 클라이언트 */

        /** try 1. */
        // 클라이언트가 보낸 통신메세지 확인하기
        String temp_2 = (String)obj;
        String message = temp_2.replace(" ", "");
        System.out.println("[유저]: " + message);

        // 에러 메세지
        // ==> com.google.gson.stream.MalformedJsonException: Unterminated string at line 1 column 289 path $.type
        JsonReader reader = new JsonReader(new StringReader(message));
        reader.setLenient(true);
        Gson gson = new GsonBuilder().setLenient().create();
        // 받은 통신 메세지 data 객체화
        Data_for_netty data = gson.fromJson(reader, Data_for_netty.class);


        // 서버가 통신 메세지 받은 시간을, chat_log의 Transmission_gmt_time 에 set
        if(data.getChat_log() != null) {
            data.getChat_log().setTransmission_gmt_time(System.currentTimeMillis());
        }


        /************************************************************************
                                        통신 메세지 구분
         ************************************************************************/
        switch (data.getNetty_type()) {
            /** 접속 메시지 일 때 - 접속한 클라이언트의 channel을 ConcurrentHashMap에 저장한다 */
            case "conn":
                String conn_user_no = data.getSender_user_no();
                Channel conn_user_channel = ctx.channel();

                Chat_server.clients.put(conn_user_no, conn_user_channel);
                System.out.println("[SERVER]: 접속중인 클라이언트 수_ " + Chat_server.clients.size());

                System.out.println("== 접속중인 클라이언트 현황 == ");
                for (String key : Chat_server.clients.keySet()) {
                    System.out.print("key=" + key);
                    System.out.println(" value=" + Chat_server.clients.get(key).toString());
                }

                // 위에 for 문이 원래 아래 이 코드 였음
//                Iterator<String> it = Chat_server.clients.keySet().iterator();
//                while(it.hasNext()) {
//                    String key = it.next();
//                    System.out.print("key=" + key);
//                    System.out.println(" value=" + Chat_server.clients.get(key).toString());
//                }
                break;

            /** 채팅방 '진입 or 나가기' 메세지 일때 */
            case "chatroom":
                String user_no_for_chatroom = data.getSender_user_no();
                String chatRoom_no = data.getExtra();

                // 채팅방 '진입' 메세지일 때
                if(data.getSubType().equals("enter")) {
                    System.out.print("[유저_ " + user_no_for_chatroom + "]:");
                    System.out.println(" 채팅방 - " + chatRoom_no + "번방으로 입장");

                    Chat_server.clients_chatroom.put(user_no_for_chatroom, chatRoom_no);
                    System.out.println("[SERVER]: 채팅방에 들어가 있는 유저 수_ " + Chat_server.clients_chatroom.size());

                    for(String key: Chat_server.clients_chatroom.keySet()) {
                        System.out.print("key=" + key);
                        System.out.println(" value= " + Chat_server.clients_chatroom.get(key));
                    }
                }
                // 채팅방 '나가기' 메세지일 때
                else if(data.getSubType().equals("out")) {

                    System.out.print("[유저_ " + user_no_for_chatroom + "]:");
                    System.out.println(" 채팅방 - " + chatRoom_no + "번방에서 나감");

                    // 해쉬맵에서 제거
                    Chat_server.clients_chatroom.remove(user_no_for_chatroom);

                    System.out.println("[SERVER]: 해쉬맵 제거 후, 채팅방에 들어가 있는 유저 수_ " + Chat_server.clients_chatroom.size());

                    System.out.print("== 채팅방 들어가있는 유저의 현황 == ");
                    for (String key: Chat_server.clients_chatroom.keySet()) {
                        System.out.print("key=" + key);
                        System.out.println(" value=" + Chat_server.clients_chatroom.get(key));
                    }
                }
                break;

            /** 클라이언트로 부터 요청이 왔을 때 */
            case "request":
                String request_user_no = data.getSender_user_no();
                String request_chatRoom_no = data.getExtra();
                System.out.println("== request ==");
                System.out.println("request_user_no: " + request_user_no);
                System.out.println("request_chatRoom_no: " + request_chatRoom_no);

                // 채팅방에 있는 다른 상대방이 채팅 액티비티에 들어와서 메세지를 읽었으니,
                // 그 사람이 읽은 메세지 번호 사이에 있는 내 메세지들에 대한 '메세지 읽음 수'를 서버로 부터 받아서 업데이트 해라
                if(data.getSubType().equals("update_chat_log")) {
                    int first_read_msg_no = Integer.parseInt(data.getFirst_read_msg_no());
                    int last_read_msg_no = Integer.parseInt(data.getLast_read_msg_no());

                    // 최종적으로 전달할 값을 담을 변수
                    String unread_msg_count_info_jsonString = "";

                    Connection conn_request = null;
                    PreparedStatement pstmt_request = null;
                    ResultSet rs_request = null;
                    // 현재 해당 채팅방 액티비티를 보고 있는 user_no를 담을 ArrayList
                    ArrayList<String> user_no_list = new ArrayList<>();
                    // key - msg_no
                    // value - msg_unread_count
                    HashMap<String, String> unread_msg_count_info = new HashMap<>();
                    try {
                        conn_request = getConnection();
                        pstmt_request = conn_request.prepareStatement(
                                "select * from message where msg_no>=? and msg_no<=?");
                        pstmt_request.setInt(1, first_read_msg_no);
                        pstmt_request.setInt(2, last_read_msg_no);

                        rs_request = pstmt_request.executeQuery();

                        while(rs_request.next()) {
                            String msg_no = rs_request.getString("msg_no");
                            String msg_unread_count = rs_request.getString("msg_unread_count");
                            unread_msg_count_info.put(msg_no, msg_unread_count);
                        }

                        // 새로 받은 unread_msg_count를 담은 해쉬맵을 jsonString으로 변환
                        JSONObject jsonObject = new JSONObject();
                        for (String key : unread_msg_count_info.keySet()) {
                            jsonObject.put(key, unread_msg_count_info.get(key));
                        }
                        unread_msg_count_info_jsonString = jsonObject.toString();
                        System.out.println("unread_msg_count_info_jsonString: " + unread_msg_count_info_jsonString);

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (conn_request != null) try{conn_request.close();}catch(Exception e){}
                        if (pstmt_request != null) try {pstmt_request.close();}catch (Exception e) {}
                        if (rs_request != null) try {rs_request.close();}catch (Exception e) {}
                    }

                    // 현재 해당 채팅방 액티비티를 보고 있는 user_no를 담기
                    for(String key: Chat_server.clients_chatroom.keySet()) {

                        // 메세지를 보내야 하는 대상에서 나는 제외
                        if(key.equals(request_user_no)) {
                            continue;
                        }

                        if(Chat_server.clients_chatroom.get(key).equals(request_chatRoom_no)) {
                            user_no_list.add(key);
                            System.out.print("현재 " + Chat_server.clients_chatroom.get(key));
                            System.out.println("방에 있는 유저: " + key + "번");
                        }
                    }

                    // 새로 받은 unread_msg_count를 담은 해쉬맵을 jsonString을, 전달할 data 객체에 넣기(set)
                    data.setUnread_msg_count_info_jsonString(unread_msg_count_info_jsonString);

                    // 통신 전송 메소드 호출
                    send_to_clients_plural(user_no_list, data);

                }


                break;

            /** 채팅 메세지일 때 */
            case "msg":
                // 채팅 메세지 보낸 user_no
                String msg_user_no = data.getSender_user_no();
                // 채팅방 번호 get
                String chat_room_no = String.valueOf(data.getChat_log().getChat_room_no());
                // 채팅 메세지의 uuid get - 체크용
                String chat_log_uuid = data.getExtra();
                System.out.println("[유저_" + data.getSender_user_no() + "의 chat_log_uuid]: " + chat_log_uuid);
                // insert 한 msg_no를 저장할 변수
                int insert_msg_no = -1;

                // 텍스트라면
                if(data.getChat_log().getMsg_type().equals("text")) {
                    System.out.println("[유저_" + data.getSender_user_no() + "]: " + data.getChat_log().getMsg_content());

                    // 1. jdbc - 채팅 로그 저장
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    Statement currvalStatement = null;
                    ResultSet currvalResultSet = null;

                    // insert 된 이후에, insert된 message_no 를 가져오기 위한 sql 문
                    String sql_currval = "SELECT currval('message_msg_no_seq')";

                    try {
                        conn = getConnection();
                        pstmt = conn.prepareStatement("insert into message (chat_room_no, msg_type, user_no, transmission_gmt_time, msg_content, attachment, member_count, msg_unread_count)" +
                                "values (?,?,?,?,?,?,?,?)");
                        pstmt.setInt(1, data.getChat_log().getChat_room_no());
                        pstmt.setString(2, data.getChat_log().getMsg_type());
                        pstmt.setInt(3, data.getChat_log().getUser_no());
                        pstmt.setString(4, String.valueOf(data.getChat_log().getTransmission_gmt_time()));
                        System.out.println("String.valueOf(data.getChat_log().getTransmission_gmt_time()): "+
                                String.valueOf(data.getChat_log().getTransmission_gmt_time()));
                        pstmt.setString(5, data.getChat_log().getMsg_content());
                        pstmt.setString(6, "");
                        pstmt.setInt(7, data.getChat_log().getMember_count());
                        pstmt.setInt(8, data.getChat_log().getMember_count()-1);

                        int result_count = pstmt.executeUpdate();

                        if(result_count == 1) {
                            System.out.println("메세지 저장 성공");

                            currvalStatement = conn.createStatement();
                            currvalResultSet = currvalStatement.executeQuery(sql_currval);

                            if (currvalResultSet.next()) {
                                insert_msg_no = currvalResultSet.getInt(1);
                                System.out.println("insert 한 msg_no: " + insert_msg_no);
                                // insert 한 msg_no를 클라이언트에게 돌려줄 data에 set 하기
                                data.getChat_log().setMsg_no(insert_msg_no);
                            }
                            System.out.println("채팅메시지 도달 시간: "  + get_present_time.present_time());

                            // 2. jdbc - 해당 채팅방의 마지막 chat_log_no 업데이트
                            Connection conn_1 = null;
                            PreparedStatement pstmt_1 = null;
                            try {
                                conn_1 = getConnection();
                                pstmt_1 = conn_1.prepareStatement("update chat_room set last_msg_no = ? where chat_room_no = ?");
                                pstmt_1.setInt(1, insert_msg_no);
                                pstmt_1.setInt(2, data.getChat_log().getChat_room_no());

                                int last_chat_log_no = pstmt_1.executeUpdate();
                                if(last_chat_log_no == 1) {
                                    System.out.println("채팅방 마지막 채팅 로그 no 업데이트 성공");

//                                    // 클라이언트에게 콜백메세지 보내기
//                                    Data_for_netty call_back_data = new Data_for_netty
//                                            .Builder("call_back", data.getUser_no())
//                                            .build();
//                                    call_back_data.setSubType("msg");
//                                    // 통신 전송 메소드 호출
//                                    send_to_client(call_back_data, ctx.channel());
                                }
                                else if(last_chat_log_no == 0) {
                                    System.out.println("채팅방 마지막 채팅 로그 no 업데이트 실패");
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                if (conn_1 != null) try{conn_1.close();}catch(Exception e){}
                                if (pstmt_1 != null) try {pstmt_1.close();}catch (Exception e) {}
                            }
                        }
                        else if(result_count == 0) {
                            System.out.println("메세지 저장 실패");
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (conn != null) try{conn.close();}catch(Exception e){}
                        if (pstmt != null) try {pstmt.close();}catch (Exception e) {}
                        if (currvalStatement != null) try {currvalStatement.close();}catch (Exception e) {}
                        if (currvalResultSet != null) try {currvalResultSet.close();}catch (Exception e) {}
                    }
                }

                // 해당 채팅방에 참여하고 있는 유저들에게 메시지를 보낸다
                // * jdbc - 해당 채팅방에 참여하고 있는 유저들의 user_no 가져오기
                Connection conn_2 = null;
                PreparedStatement pstmt_2 = null;
                ResultSet rs_2 = null;
                ArrayList<String> user_no_list = new ArrayList<>();
                int chat_room_last_msg_no = 0;

                try {
                    conn_2 = getConnection();
                    pstmt_2 = conn_2.prepareStatement("select * from chat_room where chat_room_no= ? ");
                    pstmt_2.setInt(1, Integer.parseInt(chat_room_no));

                    rs_2 = pstmt_2.executeQuery();

                    // user_no를 ArrayList에 add 하기
                    while(rs_2.next()) {

                        // 해당 채팅방의 참여자 user_no 리스트 가져오기
                        String temp = rs_2.getString("chat_in_user_no_list");
                        // 해당 채팅방의 마지막 msg_no 가져오기
                        chat_room_last_msg_no = rs_2.getInt("last_msg_no");
                        String[] temp_1 = temp.split(Static.SPLIT);
                        // 해당 채팅방에 참여중인 user_no를 가져와서 넣되, 나 자신은 제외
                        for(int j=0; j<temp_1.length; j++) {
                            if(!temp_1[j].equals(data.getSender_user_no())) {
                                user_no_list.add(temp_1[j]);

                                // * jdbc - 해당 유저가 해당 채팅방에서 읽은 마지막 msg_no 와,
                                //          해당 채팅방의 마지막 msg_no 차이를 계산해서 안 읽은 메세지 개수를
                                //          Data_for_netty 안에 String 객체의 msg_unread_count 에 넣기
                                Connection conn_3 = null;
                                PreparedStatement pstmt_3 = null;
                                ResultSet rs_3 = null;

                                try {
                                    conn_3 = getConnection();
                                    pstmt_3 = conn_3.prepareStatement("select last_read_msg_no from my_chat_room_info where chat_room_no = ?");
                                    pstmt_3.setInt(1, Integer.parseInt(chat_room_no));

                                    rs_3 = pstmt_3.executeQuery();

//                                    // 해당 유저가 읽은 마지막 msg_no 확인해서 Data_for_netty 객체에 set 하기
//                                    while(rs_3.next()) {
//                                        int user_last_read_msg_no = rs_3.getInt("last_read_msg_no");
//                                        int unread_msg_count = chat_room_last_msg_no - user_last_read_msg_no;
//                                        data.setExtra(String.valueOf(unread_msg_count));
//                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                finally {
                                    if (conn_3 != null) try{conn_3.close();}catch(Exception e){}
                                    if (pstmt_3 != null) try {pstmt_3.close();}catch (Exception e) {}
                                    if (rs_3 != null) try {rs_3.close();}catch (Exception e) {}
                                }
                            }
                            System.out.println("user_no: " + temp_1[j]);
                        }
                        System.out.println("메세지를 보내야 되는 유저의 수: " + user_no_list.size() + "명");

                        // 릴레이(중계), 혹은 내메세지 콜백 메세지를 보낼 'data' 객체에 이 메세지를 읽을 수 있는 대상인 user_no_list를 넣고
                        // jdbc를 통해서 message 테이블에도 db를 업데이트 한다
                        //=====================================================================================
                        // user_no 가 담긴 ArrayList를 StringBuilder를 통해 하나의 String으로 변환
                        StringBuilder user_no_list_for_save_DB = new StringBuilder();
                        for(int k=0; k<user_no_list.size(); k++) {
                            if(k == user_no_list.size()-1) {
                                user_no_list_for_save_DB.append(user_no_list.get(k));
                            }
                            else {
                                user_no_list_for_save_DB.append(user_no_list.get(k)).append(Static.SPLIT);
                            }
                        }
                        System.out.println("user_no_list_for_save_DB: " + user_no_list_for_save_DB);

                        Connection conn_4 = null;
                        PreparedStatement pstmt_4 = null;
                        try {
                            conn_4 = getConnection();
                            pstmt_4 = conn_4.prepareStatement("update message set msg_unread_user_no_list = ? where msg_no = ?");
                            pstmt_4.setString(1, String.valueOf(user_no_list_for_save_DB));
                            pstmt_4.setInt(2, insert_msg_no);

                            int update_msg_unread_user_no_list = pstmt_4.executeUpdate();
                            if(update_msg_unread_user_no_list == 1) {
                                System.out.println(String.valueOf(insert_msg_no) + "번 메세지를 읽을 수 있는 대상을 담은 user_no_list_jsonString 업데이트 성공");

                            }
                            else if(update_msg_unread_user_no_list == 0) {
                                System.out.println(String.valueOf(insert_msg_no) + "번 메세지를 읽을 수 있는 대상을 담은 user_no_list_jsonString 업데이트 성공");
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            if (conn_4 != null) try{conn_4.close();}catch(Exception e){}
                            if (pstmt_4 != null) try {pstmt_4.close();}catch (Exception e) {}
                        }
                        //=====================================================================================


                    }
                    //// 1) 채팅방의 다른 사람들에게 메세지 릴레이(중계) 보내기
                    // 다른 채팅메세지를 서버에서 중계하고 있는것임을 알리는 String 값을 subType에 넣기
                    data.setSubType("relay_msg");
                    // user_no를 담은 ArrayList를 매개변수로 하는 통신 전송 메소드 호출
                    // 해당 방에 있는 유저에게 메세지 전송
                    // 인자 1. user_no를 담은 ArrayList
                    // 인자 2. 발송한 클라이언트로 받은 Data_for_netty 객체
                    send_to_clients_plural(user_no_list, data);

                    //// 2) 내가 보낸 채팅메세지가 서버에 잘 도착했음을 알리는 콜백 메세지 보내기
                    data.setSubType("call_back");
                    send_to_client(ctx.channel(), data);

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    if (conn_2 != null) try{conn_2.close();}catch(Exception e){}
                    if (pstmt_2 != null) try {pstmt_2.close();}catch (Exception e) {}
                    if (rs_2 != null) try {rs_2.close();}catch (Exception e) {}
                }

                break;
        }
    }


    /**---------------------------------------------------------------------------
     메소드 ==> 메세지를 보내야 하는 사람들의 user_no가 있는 arrayList를 받아서, 해당 사람들에게만 메세지를 보냄
     ---------------------------------------------------------------------------*/
    public void send_to_clients_plural(final ArrayList<String> list, final Data_for_netty data) {

        System.out.println("Chat_server.clients.size(): "+Chat_server.clients.size());
//        System.out.println("list.get(0): "+list.get(0));

        for(int i=0; i<list.size(); i++) {
            Gson gson = new Gson();
            String data_chatlog = gson.toJson(data);
            Channel channel = Chat_server.clients.get(list.get(i));
            System.out.println("[메세지를 전달 받을 유저 번호: " + list.get(i) + "번]");

            channel.writeAndFlush(data_chatlog);
//            channel.writeAndFlush(messageBuffer);
        }
    }

    /**---------------------------------------------------------------------------
     메소드 ==> Netty 를 통해 연결된 서버로 통신메세지 보내기
     ---------------------------------------------------------------------------*/
    public void send_to_client(final Channel channel, final Data_for_netty data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new Gson();
                String data_chatlog = gson.toJson(data);
                channel.writeAndFlush(data_chatlog);
            }
        }).start();
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("[SERVER]: ====================================== channelReadComplete ====================================== ");
//        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: "+ctx.channel().remoteAddress() + ", 클라이언트 접속 끊어짐");
    }


    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: Handler removed");

        System.out.println("[SERVER]: 접속중인 클라이언트 수_ " + Chat_server.clients.size());

        for (String key : Chat_server.clients.keySet()) {
            System.out.print("key=" + key);
            System.out.println(" value=" + Chat_server.clients.get(key).toString());

            System.out.println("isActive: " + Chat_server.clients.get(key).isActive());
            System.out.println("isOpen: " + Chat_server.clients.get(key).isOpen());
            System.out.println("isRegistered: " + Chat_server.clients.get(key).isRegistered());
            System.out.println("isWritable: " + Chat_server.clients.get(key).isWritable());

            if(!Chat_server.clients.get(key).isActive()) {
                Chat_server.clients.remove(key);
                System.out.println("[SERVER]: 비정상적 클라이언트 해쉬맵 제거 후, 접속중인 클라이언트 수_ "
                        + Chat_server.clients.size());
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("====== exceptionCaught ======");
        cause.printStackTrace();
        ctx.close();
    }


    /**---------------------------------------------------------------------------
     메소드 ==> JDBC Connection 맺기 + Connection 객체 리턴
     ---------------------------------------------------------------------------*/
    public Connection getConnection() {

        try{
            Class.forName("org.postgresql.Driver");

            String url = "jdbc:postgresql://localhost:5432/remotemeeting";
            String user = "greatimon";
            String password = "dydska11";

            Connection conn = DriverManager.getConnection(url, user, password);
//            System.out.println("데이터베이스 db에 성공적으로 접속했습니다");
            return conn;
        }
        catch (ClassNotFoundException cnfe){
            System.out.println("Could not find the JDBC driver");
            System.exit(1);
            return null;
        }
        catch (SQLException sqle) {
            System.out.println("Could not connect");
            System.exit(1);
            return null;
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state().equals(IdleState.READER_IDLE)) {
//                System.out.println("READER_IDLE");
//                if (lastping != 0L) {
//                    long time = (System.currentTimeMillis() - lastping) / 1000;
//                    System.out.println("Time : " + time);
//                    if (time > 3) {
//                        System.err.println("No heart beat received in 3 seconds, close channel.");
////                        channels.remove(ctx.channel());
//                        ctx.close();
//                    }
//                }
//            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
//                System.out.println("WRITER_IDLE");
//            } else if (event.state().equals(IdleState.ALL_IDLE)) {
//                System.out.println("ALL_IDLE");
//                if (lastping == 0L) {
//                    lastping = System.currentTimeMillis();
//                }
//                ctx.channel().write("ping\n");
//            }
//        }
//        super.userEventTriggered(ctx, evt); //To change body of generated methods, choose Tools | Templates.
    }




    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("====== channelWritabilityChanged ======");
        super.channelWritabilityChanged(ctx);
    }
}
