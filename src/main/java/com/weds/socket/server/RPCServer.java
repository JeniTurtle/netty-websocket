package com.weds.socket.server;

import com.weds.socket.channel.ChannelRepository;
import com.weds.socket.handler.AuthServerHandler;
import com.weds.socket.handler.IdleServerHandler;
import com.weds.socket.handler.LogicServerHandler;
import com.weds.socket.protobuf.Message.MessageBase;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Qualifier("rpcServer")
public class RPCServer extends AbstractServer {
  private static final Logger logger = LoggerFactory.getLogger(RPCServer.class);

  private final static int READER_IDLE_TIME_SECONDS = 20;  //读操作空闲20秒
  private final static int WRITER_IDLE_TIME_SECONDS = 20;  //写操作空闲20秒
  private final static int ALL_IDLE_TIME_SECONDS = 40;  //读写全部空闲40秒

  public static ChannelRepository channelGroup = new ChannelRepository();

  @Autowired
  @Qualifier("logicServerHandler")
  private LogicServerHandler logicServerHandler;

  @Autowired
  @Qualifier("authServerHandler")
  private AuthServerHandler authServerHandler;

  RPCServer() {
    super.configName = "rpcServer";
  }

  @Override
  public void channelHandlerRegister(SocketChannel socketChannel) {
    ChannelPipeline pipeline = socketChannel.pipeline();
    pipeline.addLast(new IdleStateHandler(
        READER_IDLE_TIME_SECONDS,
        WRITER_IDLE_TIME_SECONDS,
        ALL_IDLE_TIME_SECONDS,
        TimeUnit.SECONDS
    ));
    pipeline.addLast(new IdleServerHandler());
    pipeline.addLast(new ProtobufVarint32FrameDecoder());
    pipeline.addLast(new ProtobufDecoder(MessageBase.getDefaultInstance()));
    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
    pipeline.addLast(new ProtobufEncoder());
    pipeline.addLast(authServerHandler);
    pipeline.addLast(logicServerHandler);
  }

  @Override
  public void run() {
    try {
      startServer();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }
}
