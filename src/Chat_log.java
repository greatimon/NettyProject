public class Chat_log {
    private int msg_no;
    private int chat_room_no;
    private String msg_type;
    private int user_no;
    private long transmission_gmt_time;
    private String msg_content;
    private String attachment;
    private int member_count;
    private int msg_unread_count;
    private String msg_unread_user_no_list;

    public Chat_log() {}

    public int getMsg_no() {
        return msg_no;
    }

    public void setMsg_no(int msg_no) {
        this.msg_no = msg_no;
    }

    public int getChat_room_no() {
        return chat_room_no;
    }

    public void setChat_room_no(int chat_room_no) {
        this.chat_room_no = chat_room_no;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public int getUser_no() {
        return user_no;
    }

    public void setUser_no(int user_no) {
        this.user_no = user_no;
    }

    public long getTransmission_gmt_time() {
        return transmission_gmt_time;
    }

    public void setTransmission_gmt_time(long transmission_gmt_time) {
        this.transmission_gmt_time = transmission_gmt_time;
    }

    public String getMsg_content() {
        return msg_content;
    }

    public void setMsg_content(String msg_content) {
        this.msg_content = msg_content;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public int getMember_count() {
        return member_count;
    }

    public void setMember_count(int member_count) {
        this.member_count = member_count;
    }

    public int getMsg_unread_count() {
        return msg_unread_count;
    }

    public void setMsg_unread_count(int msg_unread_count) {
        this.msg_unread_count = msg_unread_count;
    }

    public String getMsg_unread_user_no_list() {
        return msg_unread_user_no_list;
    }

    public void setMsg_unread_user_no_list(String msg_unread_user_no_list) {
        this.msg_unread_user_no_list = msg_unread_user_no_list;
    }
}
