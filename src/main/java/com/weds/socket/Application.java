package com.weds.socket;

import com.weds.socket.server.RPCServer;
import com.weds.socket.server.WebsocketServer;
import com.weds.socket.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    SpringUtil springUtil = new SpringUtil();
    springUtil.setApplicationContext(context);
    new Thread(SpringUtil.getBean(RPCServer.class)).start();
    new Thread(SpringUtil.getBean(WebsocketServer.class)).start();
  }
}
