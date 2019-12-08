package com.syb.echotime.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单的返回服务器的时间的netty服务器
 *
 * @author shenyb
 * @time 2019-12-08 18:01
 */
public class TimeServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            }catch (Exception e){

            }
        }
        new TimeServer().bind(port);
    }

    public void bind(int port) {
        //配置服务端的nio线程组,处理接受请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //配置服务端的nio线程组,处理io
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer(){
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new TimeServerHandler());
                        }
                    });
            //绑定端口
            ChannelFuture future = b.bind(port).sync();
            System.out.println("time server start port:"+port);
            //等待服务器监听端口的关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){

        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
    class TimeServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //所有的数据操作只能操作缓存区
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req, "UTF-8");

            System.out.println("the time server received data:" + body);

            String currentTime = SimpleDateFormat.getDateTimeInstance().format(new Date());
            ByteBuf res = Unpooled.copiedBuffer(currentTime.getBytes());
            ctx.channel().write(res);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
