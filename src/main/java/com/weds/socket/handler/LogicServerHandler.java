package com.weds.socket.handler;

import com.weds.socket.channel.ChannelLogger;
import com.weds.socket.common.ResponseCodeEnum;
import com.weds.socket.common.ResponseMessage;
import com.weds.socket.protobuf.Command.CommandType;
import com.weds.socket.protobuf.Message.MessageBase;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("logicServerHandler")
@ChannelHandler.Sharable
public class LogicServerHandler extends ChannelInboundHandlerAdapter {
  private static final ChannelLogger channelLogger = new ChannelLogger(LoggerFactory.getLogger(LogicServerHandler.class));

  /**
   * 处理心跳消息
   *
   * @param ctx
   */
  private void ping(ChannelHandlerContext ctx) {
    MessageBase body = ResponseMessage.createRespData(CommandType.PONG, ResponseCodeEnum.PING_SUCCESS);
    ctx.writeAndFlush(body);
  }

  /**
   * 推送消息给websocket客户端
   *
   * @param ctx
   * @param message
   */
  private void pushMsg(ChannelHandlerContext ctx, MessageBase message) {
    try {
      WebSocketServerHandler.sendMessageToClients(message.getData());
      MessageBase body = ResponseMessage.createRespData(CommandType.PUSH_DATA_BACK, ResponseCodeEnum.PUSH_DATA_SUCCESS);
      ctx.writeAndFlush(body);
    } catch (Exception exception) {
      MessageBase body = ResponseMessage.createRespData(CommandType.PONG, ResponseCodeEnum.PUSH_DATA_FAILED);
      ctx.writeAndFlush(body);
      channelLogger.error(ctx, exception.getMessage());
      exception.printStackTrace();
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    try {
      MessageBase msgBase = (MessageBase) msg;
      CommandType command = msgBase.getCmd();
      if (command.equals(CommandType.PING)) {  // 处理心跳
        ping(ctx);
      } else if (command.equals(CommandType.PUSH_DATA)) {  // 推送消息
        pushMsg(ctx, msgBase);
      }
    } catch (Exception exception) {
      channelLogger.error(ctx, exception.getMessage());
      exception.printStackTrace();
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }
}
