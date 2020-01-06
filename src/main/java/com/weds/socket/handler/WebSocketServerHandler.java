package com.weds.socket.handler;

import com.weds.socket.channel.ChannelLogger;
import com.weds.socket.server.WebsocketServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("webSocketServerHandler")
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
  private static final ChannelLogger channelLogger = new ChannelLogger(LoggerFactory.getLogger(IdleServerHandler.class), "'Web客户端'");

  /**
   * 往websocket所有客户端发送实时数据
   *
   * @param message
   */
  public static void sendMessageToClients(String message) {
    for (Channel websocketChannel : WebsocketServer.channelGroup.getChannels()) {
      websocketChannel.writeAndFlush(new TextWebSocketFrame(message));
    }
  }

  // 每次接受到客户端传来的数据后触发
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
    channelLogger.info(ctx, "发来信息: " + msg.text());
  }

  // 每次生成一个channel都会往pipeline注册handler,这时候就会触发这个方法
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    channelLogger.info(ctx, "加入连接");
  }

  // pipeline注销handler时触发
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    channelLogger.info(ctx, "终止连接，当前连接数量：" + WebsocketServer.channelGroup.size());
  }

  // 客户端连接成功后触发
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    WebsocketServer.channelGroup.add(ctx.channel());
    channelLogger.info(ctx, "成功连接，当前连接数量：" + WebsocketServer.channelGroup.size());
  }

  // 客户端断开连接后触发
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    channelLogger.info(ctx, "断开连接");
    WebsocketServer.channelGroup.remove(ctx.channel());
  }

  // 错误状态后触发
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    channelLogger.info(ctx, "出现异常");
    cause.printStackTrace();
    WebsocketServer.channelGroup.remove(ctx.channel());
    ctx.close();
  }
}
