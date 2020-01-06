package com.weds.socket.client;

import com.weds.socket.config.RPCSocket;
import com.weds.socket.protobuf.Command;
import com.weds.socket.protobuf.Message;
import com.weds.socket.util.AuthCheck;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Qualifier("testTcpClient")
@PropertySource(value = "classpath:/nettyserver.properties")
public class TestTCPClient extends AbstractTCPClient {

  private static final Logger logger = LoggerFactory.getLogger(TestTCPClient.class);

  private final static int READER_IDLE_TIME_SECONDS = 10;  //读操作空闲10秒
  private final static int WRITER_IDLE_TIME_SECONDS = 20;  //写操作空闲20秒
  private final static int ALL_IDLE_TIME_SECONDS = 30;  //读写全部空闲30秒

  @Autowired
  private RPCSocket rpcSocket;

  TestTCPClient() {
    super.configName = "rpcServer";
  }

  public void channelInit(SocketChannel ch) {
    ChannelPipeline p = ch.pipeline();
    p.addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS
        , WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS, TimeUnit.SECONDS));
    p.addLast(new ProtobufVarint32FrameDecoder());
    p.addLast(new ProtobufDecoder(Message.MessageBase.getDefaultInstance()));
    p.addLast(new ProtobufVarint32LengthFieldPrepender());
    p.addLast(new ProtobufEncoder());
    p.addLast("idleTimeoutHandler", new IdleClientHandler());
    p.addLast("clientHandler", new LogicClientHandler());
  }


  public class IdleClientHandler extends SimpleChannelInboundHandler<Message> {

    private int heartbeatCount = 0;


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
        IdleStateEvent event = (IdleStateEvent) evt;
        String type = "";
        if (event.state() == IdleState.READER_IDLE) {
          type = "read idle";
        } else if (event.state() == IdleState.WRITER_IDLE) {
          type = "write idle";
        } else if (event.state() == IdleState.ALL_IDLE) {
          type = "all idle";
        }
        logger.debug(ctx.channel().remoteAddress() + "超时类型：" + type);
        sendPingMsg(ctx);
      } else {
        super.userEventTriggered(ctx, evt);
      }
    }

    /**
     * 发送ping消息
     *
     * @param context
     */
    protected void sendPingMsg(ChannelHandlerContext context) {
      context.writeAndFlush(
          Message.MessageBase.newBuilder()
              .setCmd(Command.CommandType.PING)
              .setData("1")
              .build()
      );
      heartbeatCount++;
      if (heartbeatCount == 10) {
        context.close();
      }
      logger.info("Client sent ping msg to " + context.channel().remoteAddress() + ", count: " + heartbeatCount);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
  }

  public class LogicClientHandler extends SimpleChannelInboundHandler<Message.MessageBase> {

    // 连接成功后，向server发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      Message.MessageBase.Builder authMsg = Message.MessageBase.newBuilder();
      authMsg.setCmd(Command.CommandType.AUTH);
      authMsg.setData(AuthCheck.genAuthKey(rpcSocket.getSecret()));

      ctx.writeAndFlush(authMsg.build());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      logger.debug("连接断开 ");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message.MessageBase msg) throws Exception {
      logger.info(msg.getCmd() + " " + msg.getMsg());
      if (msg.getCmd().equals(Command.CommandType.AUTH_BACK)) {
        logger.debug("验证成功");
      } else if (msg.getCmd().equals(Command.CommandType.PING)) {
        //接收到server发送的ping指令
      } else if (msg.getCmd().equals(Command.CommandType.PONG)) {
        //接收到server发送的pong指令
        ctx.writeAndFlush(
            Message.MessageBase.newBuilder()
                .setCmd(Command.CommandType.PUSH_DATA)
                .setData("push data!!!!")
                .build()
        );
      } else if (msg.getCmd().equals(Command.CommandType.PUSH_DATA)) {
        //接收到server推送数据
      } else if (msg.getCmd().equals(Command.CommandType.PUSH_DATA_BACK)) {
        //接收到server返回数据
      } else {

      }
    }
  }
}
