package com.weds.socket.handler;

import com.weds.socket.channel.ChannelLogger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 连接空闲Handler
 */
@Component
public class IdleServerHandler extends ChannelInboundHandlerAdapter {
  private static final ChannelLogger channelLogger = new ChannelLogger(LoggerFactory.getLogger(IdleServerHandler.class));

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
    if (event instanceof IdleStateEvent) {
      IdleStateEvent evt = (IdleStateEvent) event;
      String type = "";
      if (evt.state() == IdleState.READER_IDLE) {
        type = "READ IDLE";
      } else if (evt.state() == IdleState.WRITER_IDLE) {
        type = "WRITE IDLE";
      } else if (evt.state() == IdleState.ALL_IDLE) {
        type = "ALL IDLE";
      }
      channelLogger.warn(ctx, "超时类型:" + type);
    } else {
      super.userEventTriggered(ctx, event);
    }
  }
}
