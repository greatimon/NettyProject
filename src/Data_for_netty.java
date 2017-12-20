/**
 * 이 클래스는 netty를 이용해서 서버와 통신할 때, 주고 받는 데이터 클래스
 * */
public class Data_for_netty {

    // builder 타입
    private String netty_type ="";                              // 서버 통신 분류
    private String subType ="";                                 // 소분류
    private String sender_user_no ="";                          // 내 user_no
    private String target_user_no ="";                          // 타켓 user_no
    private Chat_log chat_log = new Chat_log();                              // 채팅 메세지라면, 그 객체
    private String attachment ="";                              // 첨부파일 정보
    private String extra ="";                                   // 스페어용 변수
    private String first_read_msg_no ="";                       // 실시간 읽음 표시를 처리하기 위해 필요한 변수1
    private String last_read_msg_no ="";                        // 실시간 읽음 표시를 처리하기 위해 필요한 변수2
    private String unread_msg_count_info_jsonString ="";        // 실시간 읽음 표시를 처리하기 위해 필요한 변수3

    public String getNetty_type() {
        return netty_type;
    }

    public void setNetty_type(String netty_type) {
        this.netty_type = netty_type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getSender_user_no() {
        return sender_user_no;
    }

    public void setSender_user_no(String sender_user_no) {
        this.sender_user_no = sender_user_no;
    }

    public String getTarget_user_no() {
        return target_user_no;
    }

    public void setTarget_user_no(String target_user_no) {
        this.target_user_no = target_user_no;
    }

    public Chat_log getChat_log() {
        return chat_log;
    }

    public void setChat_log(Chat_log chat_log) {
        this.chat_log = chat_log;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getFirst_read_msg_no() {
        return first_read_msg_no;
    }

    public void setFirst_read_msg_no(String first_read_msg_no) {
        this.first_read_msg_no = first_read_msg_no;
    }

    public String getLast_read_msg_no() {
        return last_read_msg_no;
    }

    public void setLast_read_msg_no(String last_read_msg_no) {
        this.last_read_msg_no = last_read_msg_no;
    }

    public String getUnread_msg_count_info_jsonString() {
        return unread_msg_count_info_jsonString;
    }

    public void setUnread_msg_count_info_jsonString(String unread_msg_count_info_jsonString) {
        this.unread_msg_count_info_jsonString = unread_msg_count_info_jsonString;
    }
}