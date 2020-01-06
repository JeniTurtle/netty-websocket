package com.weds.socket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

abstract class AbstractTCPClient implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(TestTCPClient.class);

  protected String configName;

  private Properties configProps;

  private String host;

  private Integer port;

  private void startInit() {
    configProps = new Properties();
    try {
      configProps.load(AbstractTCPClient.class.getClassLoader().getResourceAsStream("nettyserver.properties"));
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
    host = getProperty("host");
    port = Integer.valueOf(getProperty("port"));
  }

  @Override
  public void run() {
    try {
      startClient();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  public void startClient() throws Exception {
    startInit();
    EventLoopGroup group = new NioEventLoopGroup();

    try {
      Bootstrap b = new Bootstrap();
      b.group(group) // 注册线程池
          .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
          .remoteAddress(new InetSocketAddress(host, port)) // 绑定连接端口和host信息
          .handler(new CustomChannelInitializer());

      ChannelFuture cf = b.connect().sync(); // 异步连接服务器

      cf.channel().closeFuture().sync(); // 异步等待关闭连接channel
      logger.warn("客户端关闭"); // 关闭完成
    } finally {
      group.shutdownGracefully().sync(); // 释放线程池资源
    }
  }

  public abstract void channelInit(SocketChannel ch);

  private class CustomChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
      channelInit(ch);
      logger.warn("客户端连接成功");
    }
  }

  private String getProperty(String name) {
    return configProps.getProperty(configName + "." + name);
  }
}
