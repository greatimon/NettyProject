import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GatheringHandler extends ChannelInboundHandlerAdapter {

    private String temp = "";
    private int count = 0;

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
        ServerHandler.send_to_client(ctx.channel(), call_back_data);
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelInactive -- 채널이 비활성화가 되었을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: "+ctx.channel().remoteAddress() + ", 클라이언트 접속 끊어짐");
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelRead -- 클라이언트로부터 메세지가 왔을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws InterruptedException {
        count++;
        // 클라이언트가 보낸 통신메세지 확인하기
        String temp_2 = (String) obj;
        String message = temp_2.replace(" ", "");
        System.out.println("==================== GatheringHandler_ message[" + count + "] -" + message + " ====================");

        temp = temp + message;
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> channelReadComplete -- 읽을 데이터(메세지)가 없을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelRead(temp);
        temp = "";
        count = 0;
    }


    /**---------------------------------------------------------------------------
     콜백메소드 ==> handlerRemoved -- 채널이 등록 해제되었을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
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


    /**---------------------------------------------------------------------------
     콜백메소드 ==> exceptionCaught -- Exception이 발생했을 때 호출되는 콜백
     ---------------------------------------------------------------------------*/
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("====== exceptionCaught ======");
        cause.printStackTrace();
        ctx.close();
    }
}
