package com.weds.socket.common;

import com.weds.socket.protobuf.Command;
import com.weds.socket.protobuf.Message;

public class ResponseMessage {
  private static Message.MessageBase.Builder createRespDataBuilder(Command.CommandType cmd, ResponseCodeEnum responseCode) {
    Message.MessageBase.Builder body = Message.MessageBase.newBuilder();
    body.setCmd(cmd);
    body.setCode(responseCode.getCode());
    body.setMsg(responseCode.getMsg());
    return body;
  }

  private static Message.MessageBase.Builder createRespDataBuilder(Command.CommandType cmd, ResponseCodeEnum responseCode, String data) {
    Message.MessageBase.Builder body = createRespDataBuilder(cmd, responseCode);
    body.setData(data);
    return body;
  }

  public static Message.MessageBase createRespData(Command.CommandType cmd, ResponseCodeEnum responseCode) {
    return createRespDataBuilder(cmd, responseCode).build();
  }

  public static Message.MessageBase createRespData(Command.CommandType cmd, ResponseCodeEnum responseCode, String data) {
    return createRespDataBuilder(cmd, responseCode, data).build();
  }
}
