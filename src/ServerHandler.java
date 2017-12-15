import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    static ConcurrentHashMap<String, Channel> client;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: Handler added");
        Channel added_channel = ctx.channel();
        /** 통신메세지 send - 접속한 클라이언트 */
        added_channel.writeAndFlush("[Server Message] - 채팅서버에 접속되었습니다");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: "+ctx.channel().remoteAddress() + ", 클라이언트 접속");
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        /** 통신메세지 received - 접속한 클라이언트 */
        // 클라이언트의 IP 주소 얻기
        String client_adrr = String.valueOf(ctx.channel().remoteAddress());
        // 클라이언트가 보낸 메세지 확인하기
        String message = (String)msg;
        System.out.println("[" + client_adrr + "]: " + message);

        /** 통신메세지 send - 접속한 클라이언트 */
        String sendMessage = "[Server Message] - " + message + "<<< 전달 받음";
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        ctx.writeAndFlush(messageBuffer).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("[SERVER]: 클라이언트에게 메세지 전송 완료");
            }
        });

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: "+ctx.channel().remoteAddress() + ", 클라이언트 접속 끊어짐");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("[SERVER]: Handler removed");
    }





    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("====== exceptionCaught ======");
        cause.printStackTrace();
        ctx.close();
    }













    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("====== userEventTriggered ======");
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("====== channelWritabilityChanged ======");
        super.channelWritabilityChanged(ctx);
    }


}
