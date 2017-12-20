import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class GatheringHandler extends ChannelInboundHandlerAdapter {

    private String temp = "";
    private int count = 0;


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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.fireChannelRead(temp);
        temp = "";
        count = 0;
    }
}
