import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.logging.Logger;

public class InBoundHandler_test implements ChannelInboundHandler {

    private Logger log;

    InBoundHandler_test() {
        log = Logger.getLogger(InBoundHandler_test.class.getName());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelRegistered ======");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelUnregistered ======");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelActive ======");
        log.info("localAddress: "+ctx.channel().localAddress());
        log.info("remoteAddress: "+ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelInactive ======");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("====== channelRead ======");

        String readMsg = ((ByteBuf)msg).toString(Charset.defaultCharset());
        log.info("수신한 문자열: " + readMsg);

        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelReadComplete ======");
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.info("====== userEventTriggered ======");
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("====== channelWritabilityChanged ======");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("====== handlerAdded ======");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("====== handlerRemoved ======");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("====== exceptionCaught ======");
        cause.printStackTrace();
        ctx.close();
    }
}
