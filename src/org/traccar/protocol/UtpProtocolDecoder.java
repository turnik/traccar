package org.traccar.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.traccar.BaseProtocolDecoder;
import org.traccar.DeviceSession;
import org.traccar.model.Position;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtpProtocolDecoder extends BaseProtocolDecoder {

    public UtpProtocolDecoder(UtpProtocol protocol) {
        super(protocol);
    }

    private static final Map<Integer, Integer> TAG_LENGTH_MAP = new HashMap<>();
    private static final int PACKET_TYPE_POSITION = 7;
    private static final int CHECKSUM_POSITION_INDEX = 10;
    private static final int INIT_PACKET = 0x00;
    private static final int DATA_PACKET = 0x01;
    private static final int ACK_PACKET = 0x03;
    private static final int COMMAND_PACKET = 0x04;

    private static final int DATA_AUTO_MODE = 0x1a;

    static {
        TAG_LENGTH_MAP.put(0x02, 15);
        TAG_LENGTH_MAP.put(0x03, 17);
        TAG_LENGTH_MAP.put(0x04, 17);
        TAG_LENGTH_MAP.put(0x05, 18);
        TAG_LENGTH_MAP.put(0x06, 8);
        TAG_LENGTH_MAP.put(0x07, 7);
        TAG_LENGTH_MAP.put(0x13, 4);
    }

    private static int getTagLength(int tag) {
        Integer length = TAG_LENGTH_MAP.get(tag);
        if (length == null) {
            throw new IllegalArgumentException("Unknown tag: " + tag);
        }
        return length;
    }

//    private static void decodeState() {
//
//    }

    private void decodeTag(Position position, ChannelBuffer buf, int tag) {
        switch (tag) {
            case 0x02:
                position.setTime(new Date(buf.readLong()));
                break;
            case 0x03:
                break;
            default:
                break;
        }
    }

    private void sendAck(Channel channel, byte[] dataHeader) {
        int responseLen = 11;
        int dataPackLen = 0x00;
        if (dataHeader != null) {
            responseLen = responseLen + dataHeader.length;
            dataPackLen = dataHeader.length;
        }

        ChannelBuffer response = ChannelBuffers.directBuffer(ByteOrder.BIG_ENDIAN, responseLen);
        response.writeByte(0x62);
        response.writeByte(0x6c);
        response.writeByte(0x6b);
        response.writeInt(dataPackLen);
        response.writeByte(ACK_PACKET);
        response.writeByte(0x00);
        response.writeByte(0x00);
        response.writeByte(0x00); //tempo byte

        if (dataHeader != null) {
            response.writeBytes(dataHeader);
        }
        response.setByte(CHECKSUM_POSITION_INDEX, checksum(response));


        if (channel != null) {
            channel.write(ChannelBuffers.copiedBuffer(response));
        }
    }

    private int checksum(ChannelBuffer buffer) {
        char checksum = 0;

        for (int i = 0; i < buffer.readableBytes(); i++) {
            if (i != CHECKSUM_POSITION_INDEX) {
                checksum ^= buffer.getUnsignedByte(i);
            }
        }
        return checksum;
    }

    private Object processHandshake(Channel channel, SocketAddress remoteAddress, ChannelBuffer buf) {
        String imei = buf.readBytes(20).toString(StandardCharsets.US_ASCII);

        if (getDeviceSession(channel, remoteAddress, imei) != null) {
            sendAck(channel, null);
        }
        return null;
    }

    public Object processDataPacket(Channel channel, DeviceSession deviceSession, SocketAddress remoteAddress, ChannelBuffer buf) {
        Position position = new Position();
        position.setProtocol(getProtocolName());
        position.setId(deviceSession.getDeviceId());
        int packetBlockType = buf.getByte(buf.readerIndex());

        if (packetBlockType == DATA_AUTO_MODE) {
            //byte[] packetDataHeader = new byte[3];
            //packetDataHeader = ;
            sendAck(channel, buf.readBytes(3).array());

            return null;
        }

        return null;
    }

    @Override
    protected Object decode(
            Channel channel,
            SocketAddress remoteAddress,
            Object msg) throws Exception {

        ChannelBuffer buf = (ChannelBuffer) msg;

        buf.skipBytes(3); //read signature and pass
        int packetLength = buf.readInt();
        int packetType = buf.readUnsignedByte();
        buf.skipBytes(2); //pass option and version
        int packetChecksum = buf.readUnsignedByte();

        List<Position> positions = new LinkedList<>();
        Set<Integer> tags = new HashSet<>();
        boolean hasLocation = false;

        DeviceSession deviceSession = getDeviceSession(channel, remoteAddress);


        Position position = new Position();

        if (packetType == INIT_PACKET) {
            return processHandshake(channel, remoteAddress, buf);
        } else if (packetType == DATA_PACKET) {
            if (deviceSession == null) {
                return null; //if empty session make exit
            }
            return processDataPacket(channel, deviceSession, remoteAddress, buf);
        }
        return null;
    }
}
