package com.syb.echotime.simple;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Scanner;

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
                    .handler(new TimeClientHandler());
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    class TimeClientHandler extends ChannelHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                ctx.writeAndFlush(Unpooled.copiedBuffer(scanner.nextLine().getBytes()));
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] res = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(res);
            System.out.println("time client receive:" + new String(res, "UTF-8"));
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //如果只是write没有flush，则只是写到tcp缓冲区,flush后才会真正由tcp发送出去
                if ("".equals(line)) {
                    ctx.writeAndFlush(Unpooled.copiedBuffer("回车".getBytes()));
                } else {
                    ctx.writeAndFlush(Unpooled.copiedBuffer(line.getBytes()));
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
