package com.weds.socket.handler;

import com.weds.socket.channel.ChannelLogger;
import com.weds.socket.common.ResponseCodeEnum;
import com.weds.socket.common.ResponseMessage;
import com.weds.socket.config.RPCSocket;
import com.weds.socket.protobuf.Command.CommandType;
import com.weds.socket.protobuf.Message.MessageBase;

import java.net.InetSocketAddress;

import com.weds.socket.server.RPCServer;
import com.weds.socket.util.AuthCheck;
import com.weds.socket.util.IPWhiteList;
import io.netty.channel.Channel;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

/**
 * 连接认证Handler
 * 1. 连接成功后客户端发送CommandType.AUTH指令，Sever端验证通过后返回CommandType.AUTH_BACK指令
 * 2. 处理心跳指令
 * 3. 触发下一个Handler
 */

@ChannelHandler.Sharable
@Component
@Qualifier("authServerHandler")
public class AuthServerHandler extends ChannelInboundHandlerAdapter {
  private static final ChannelLogger channelLogger = new ChannelLogger(LoggerFactory.getLogger(AuthServerHandler.class));

  @Autowired
  private RPCSocket rpcSocket;

  private final AttributeKey<Boolean> isAuthed = AttributeKey.valueOf("isAuthed");

  private void setCtxAuthed(ChannelHandlerContext ctx, boolean authed) {
    Attribute<Boolean> attr = ctx.attr(isAuthed);
    attr.set(authed);
  }

  private boolean getCtxAuthed(ChannelHandlerContext ctx) {
    Attribute<Boolean> attr = ctx.attr(isAuthed);
    return attr.get();
  }

  private void authSuccessBack(ChannelHandlerContext ctx) {
    Channel channel = ctx.channel();
    setCtxAuthed(ctx, true);
    RPCServer.channelGroup.add(channel);
    MessageBase body = ResponseMessage.createRespData(CommandType.AUTH_BACK, ResponseCodeEnum.AUTH_SUCCESS);
    channelLogger.info(ctx, ResponseCodeEnum.AUTH_SUCCESS);
    ctx.writeAndFlush(body);
  }

  private void authFailedBack(ChannelHandlerContext ctx, ResponseCodeEnum responseCode) {
    MessageBase body = ResponseMessage.createRespData(CommandType.AUTH_BACK, responseCode);
    ctx.writeAndFlush(body);
    RPCServer.channelGroup.remove(ctx.channel());
    channelLogger.warn(ctx, responseCode);
    ctx.close();
  }

  /**
   * ip白名单检查
   *
   * @param ctx
   * @return
   */
  private boolean checkEffective(ChannelHandlerContext ctx) {
    InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
    String clientIp = insocket.getAddress().getHostAddress();
    boolean through = IPWhiteList.checkLoginIP(clientIp, rpcSocket.getIpWhiteList());
    if (!through) {
      authFailedBack(ctx, ResponseCodeEnum.NOT_ON_THE_WHITE_LIST);
    }
    return through;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    channelLogger.info(ctx, "已加入处理队列");
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    // 检查白名单
    if (checkEffective(ctx)) {
      ctx.fireChannelActive();
      channelLogger.info(ctx, "连接成功，当前连接数量：" + RPCServer.channelGroup.size());
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    RPCServer.channelGroup.remove(ctx.channel());
    channelLogger.warn(ctx, "终止连接，当前连接数量：" + RPCServer.channelGroup.size());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    channelLogger.warn(ctx, "断开连接");
  }

  // 错误状态后触发
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    RPCServer.channelGroup.remove(ctx.channel());
    channelLogger.error(ctx, "出现异常");
    channelLogger.error(ctx, cause.getMessage());
    cause.printStackTrace();
    ctx.close();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    try {
      MessageBase msgBase = (MessageBase) msg;
      channelLogger.info(ctx, "接收到数据 -> ");
      channelLogger.info(ctx, msgBase);
      // 客户端认证
      if (msgBase.getCmd().equals(CommandType.AUTH)) {
        boolean isAuthed = AuthCheck.validateAuthKey(rpcSocket.getSecret(), msgBase.getData());
        if (isAuthed) {
          // 授权成功
          authSuccessBack(ctx);
        } else {
          // 授权失败
          authFailedBack(ctx, ResponseCodeEnum.AUTHORIZATION_FAILURES);
        }
      } else if (!getCtxAuthed(ctx)) {  // 检查授权状态
        authFailedBack(ctx, ResponseCodeEnum.UNCERTIFIED);
      } else if (ctx.channel().isOpen()) {
        //触发下一个handler
        ctx.fireChannelRead(msg);
      }
    } catch (Exception exception) {
      channelLogger.error(ctx, exception.getMessage());
      exception.printStackTrace();
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }
}
