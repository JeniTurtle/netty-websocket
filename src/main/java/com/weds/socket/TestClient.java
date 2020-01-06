package com.weds.socket;

import com.weds.socket.client.TestTCPClient;
import com.weds.socket.util.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TestClient {
  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    SpringUtil springUtil = new SpringUtil();
    springUtil.setApplicationContext(context);
    try {
      for (int i = 0; i < 10; i ++) {
        new Thread(SpringUtil.getBean(TestTCPClient.class)).start();
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
  }
}
