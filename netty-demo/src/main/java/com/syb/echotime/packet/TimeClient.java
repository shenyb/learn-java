package com.syb.echotime.packet;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * nio客户端请求服务器时间
 *
 * @author shenyb
 * @time 2019-12-08 18:43
 */
public class TimeClient {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {

            }
        }
        new TimeClient().connect(port, "localhost");
    }

    public void connect(int port, String host) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            channel.pipeline().addLast(new StringDecoder());
                            channel.pipeline().addLast(new TimeClientHandler());

                        }
                    });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    class TimeClientHandler extends ChannelHandlerAdapter {
        int count = 0;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            Scanner scanner = new Scanner(System.in);
//            if (scanner.hasNextLine()) {
//                ctx.writeAndFlush(Unpooled.copiedBuffer(scanner.nextLine().getBytes()));
//            }
            for(int i=0;i<100;i++){
                ctx.write(Unpooled.copiedBuffer(("test"+System.lineSeparator()).getBytes()));
                ctx.flush();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("time client receive:" +msg+",count:"+(++count));
//            Scanner scanner = new Scanner(System.in);
//            if (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                //如果只是write没有flush，则只是写到tcp缓冲区,flush后才会真正由tcp发送出去
//                if ("".equals(line)) {
//                    ctx.writeAndFlush(Unpooled.copiedBuffer("回车".getBytes()));
//                } else {
//                    ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes()));
//                }
//            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
