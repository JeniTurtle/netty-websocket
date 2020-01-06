# 基于Netty实现的socket服务器

------

## 介绍

主要提供两个socket，一个是服务端的RPC服务，一个是客户端的webSocket服务。

业务服务使用protobuf协议推送数据给RPC服务，RPC服务会再把数据转发给webSocket客户端。

RPC服务端支持ip白名单、授权认证、心跳监测等。