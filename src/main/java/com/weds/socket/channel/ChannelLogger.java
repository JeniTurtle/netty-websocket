package com.weds.socket.channel;

import com.weds.socket.common.ResponseCodeEnum;
import com.weds.socket.protobuf.Message.MessageBase;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

public class ChannelLogger {
  private Logger logger;
  private String prefix = "客户端";

  public ChannelLogger(Logger logger) {
    this.logger = logger;
  }

  public ChannelLogger(Logger logger, String prefix) {
    this.logger = logger;
    this.prefix = prefix;
  }

  private String generateMessage(ChannelHandlerContext ctx, String msg) {
    String address = ctx.channel().remoteAddress().toString();
    return prefix + "[" + address + "]: " + msg;
  }

  public void info(ChannelHandlerContext ctx, String msg) {
    logger.info(generateMessage(ctx, msg));
  }

  public void info(ChannelHandlerContext ctx, ResponseCodeEnum respCode) {
    logger.info(generateMessage(ctx, respCode.getMsg()));
  }

  public void info(ChannelHandlerContext ctx, MessageBase msg) {
    logger.info(generateMessage(ctx, msgToString(msg)));
  }

  public void warn(ChannelHandlerContext ctx, String msg) {
    logger.warn(generateMessage(ctx, msg));
  }

  public void warn(ChannelHandlerContext ctx, ResponseCodeEnum respCode) {
    logger.warn(generateMessage(ctx, respCode.getMsg()));
  }

  public void warn(ChannelHandlerContext ctx, MessageBase msg) {
    logger.warn(generateMessage(ctx, msgToString(msg)));
  }

  public void error(ChannelHandlerContext ctx, String msg) {
    logger.error(generateMessage(ctx, msg));
  }

  public void error(ChannelHandlerContext ctx, ResponseCodeEnum respCode) {
    logger.error(generateMessage(ctx, respCode.getMsg()));
  }

  public void error(ChannelHandlerContext ctx, MessageBase msg) {
    logger.error(generateMessage(ctx, msgToString(msg)));
  }

  public void debug(ChannelHandlerContext ctx, String msg) {
    logger.debug(generateMessage(ctx, msg));
  }

  public void debug(ChannelHandlerContext ctx, ResponseCodeEnum respCode) {
    logger.debug(generateMessage(ctx, respCode.getMsg()));
  }

  public void debug(ChannelHandlerContext ctx, MessageBase msg) {
    logger.debug(generateMessage(ctx, msgToString(msg)));
  }

  public String msgToString(MessageBase msg) {
    StringBuffer sb = new StringBuffer();
    sb.append("Message { ");
    if (msg.getCmd() != null) {
      sb.append("command: ");
      sb.append(msg.getCmd());
      sb.append("; ");
    }
    if (msg.getCode() != 0) {
      sb.append("code: ");
      sb.append(msg.getCode());
      sb.append("; ");
    }
    if (msg.getMsg() != null) {
      sb.append("msg: ");
      sb.append(msg.getMsg());
      sb.append("; ");
    }
    if (msg.getData() != null) {
      sb.append("data: ");
      sb.append(msg.getData());
      sb.append("; ");
    }
    sb.append("}");
    return new String(sb);
  }
}
