package org.traccar.protocol;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.traccar.BaseProtocol;
import org.traccar.TrackerServer;

import java.nio.ByteOrder;
import java.util.List;

public class UtpProtocol extends BaseProtocol {

    public UtpProtocol() {
        super("utp");
    }

    @Override
    public void initTrackerServers(List<TrackerServer> serverList) {
        TrackerServer server = new TrackerServer(new ServerBootstrap(), getName()) {
            @Override
            protected void addSpecificHandlers(ChannelPipeline pipeline) {
                pipeline.addLast("frameDecoder", new UtpFrameDecoder());
                pipeline.addLast("objectDecoder", new UtpProtocolDecoder(UtpProtocol.this));
            }
        };
        server.setEndianness(ByteOrder.BIG_ENDIAN);
        serverList.add(server);
    }
}
