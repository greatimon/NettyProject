import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.out;

public class Chat_server {

    // key-user_no, value-channel
    static ConcurrentHashMap<String, Channel> clients;
    // key-user_no, value-chatroom_no
    static ConcurrentHashMap<String, String> clients_chatroom;

    private Chat_server() {
        clients = new ConcurrentHashMap<>();
        clients_chatroom = new ConcurrentHashMap<>();
    }

    private void start() {
        out.println("server start!!");

        EventLoopGroup connRequestGroup = new NioEventLoopGroup(1);
        EventLoopGroup IOProcessGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(connRequestGroup, IOProcessGroup)
                    // 논블럭 방식 적용
                    .channel(NioServerSocketChannel.class)
                    // 서버 로그 출력
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    .handler(new OutBoundHandler())
                    // 옵션 - 운영체제에서 지정된 시간에 한번씩 keepAlive 패킷을 상대방에게 전송한다
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 옵션 - TIME_WAIT 상태의 포트를 서버 소켓에 바인드할 수 있게 한다
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            out.println("접속한 클라이언트의 IP: "+ch.remoteAddress());
                            ChannelPipeline pipeline = ch.pipeline();
                            // Hearbeat 기능
                            pipeline.addLast("idleStateHandler", new IdleStateHandler(90, 30, 0));
                            // 클라이언트와 관련된 로그 출력
//                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            // String 인/디코더 (default인 UTF-8)
                            pipeline.addLast(new StringEncoder(), new StringDecoder());
                            pipeline.addLast(new GatheringHandler());
                            // IO 이벤트 핸들러
                            pipeline.addLast(new ServerHandler());
//                            p.addLast(new OutBoundHandler());
                        }
                    });

            serverBootstrap.bind(8888).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            connRequestGroup.shutdownGracefully();
            IOProcessGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        Chat_server server = new Chat_server();
        server.start();
    }
}
