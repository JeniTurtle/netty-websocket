package com.weds.socket.server;

import com.weds.socket.channel.ChannelRepository;
import com.weds.socket.handler.WebSocketServerHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Qualifier("websocketServer")
@PropertySource(value = "classpath:/nettyserver.properties")
public class WebsocketServer extends AbstractServer {

  private static final Logger logger = LoggerFactory.getLogger(WebsocketServer.class);

  public static ChannelRepository channelGroup = new ChannelRepository();

  @Value("${socketServer.socketUrlPath}")
  private String socketUrlPath;

  @Autowired
  @Qualifier("webSocketServerHandler")
  private WebSocketServerHandler webSocketServerHandler;

  WebsocketServer() {
    super.configName = "socketServer";
  }

  @Override
  public void channelHandlerRegister(SocketChannel socketChannel) {
    ChannelPipeline pipeline = socketChannel.pipeline();
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpObjectAggregator(64 * 1024));
    pipeline.addLast(new ChunkedWriteHandler());
    pipeline.addLast(new WebSocketServerProtocolHandler(socketUrlPath));
    pipeline.addLast(webSocketServerHandler);
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
