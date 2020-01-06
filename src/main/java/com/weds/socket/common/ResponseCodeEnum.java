package com.weds.socket.common;

public enum ResponseCodeEnum {
  AUTH_SUCCESS(10000, "认证授权成功"),
  UNCERTIFIED(10001, "连接未授权，无法连接"),
  NOT_ON_THE_WHITE_LIST(10002, "IP不在白名单内，无法连接"),
  AUTHORIZATION_FAILURES(10003, "授权失败，无法连接"),

  PING_SUCCESS(20000, "心跳监测正常"),

  PUSH_DATA_SUCCESS(30000, "消息推送成功"),
  PUSH_DATA_FAILED(30001, "消息推送失败");

  private int code;
  private String msg;

  ResponseCodeEnum(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public int getCode() {
    return code;
  }

  public String getMsg() {
    return msg;
  }
}