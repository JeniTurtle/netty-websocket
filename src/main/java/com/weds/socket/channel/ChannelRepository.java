package com.weds.socket.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;


@Component
public class ChannelRepository {

  private ChannelGroup channelCache = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

  public void add(Channel channel) {
    channelCache.add(channel);
  }

  public Channel find(ChannelId channelId) {
    return channelCache.find(channelId);
  }

  public void remove(Channel key) {
    channelCache.remove(key);
  }

  public ChannelGroup getChannels() {
    return channelCache;
  }

  public int size() {
    return channelCache.size();
  }
}
