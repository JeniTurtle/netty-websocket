package com.weds.socket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;

@Data
@Configuration
@PropertySource("classpath:application.properties")
@ConfigurationProperties(prefix = "rpcSocket")
public class RPCSocket {
  private String secret;
  private ArrayList<String> ipWhiteList = new ArrayList<>();
}