package org.traccar.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.FrameDecoder;


import java.util.Objects;

public class UtpFrameDecoder extends FrameDecoder {

    private static final int TRANSPORT_LAYER_LENGHT = 11;
    private static final int MINIMAL_PACKET_LENGHT = 14;
    private static final String SIGNATURE = "blk";

    @Override
    protected Object decode(
            ChannelHandlerContext ctx,
            Channel channel,
            ChannelBuffer buf) throws Exception {

        byte[] signatureBuffer;
        signatureBuffer = new byte[3];

        if (buf.readableBytes() < MINIMAL_PACKET_LENGHT) {
            return null;
        }

        buf.getBytes(0, signatureBuffer, 0, 3);
        String signatureString = new String(signatureBuffer, "ISO-8859-1");

        if (Objects.equals(signatureString, SIGNATURE)) {
            int dataLenght = buf.getInt(buf.readerIndex() + 3);

            if (buf.readableBytes() >= (dataLenght + TRANSPORT_LAYER_LENGHT)) {
                //return extractFrame(buf, 0, dataLenght + TRANSPORT_LAYER_LENGHT);

                return buf.readBytes(dataLenght + TRANSPORT_LAYER_LENGHT);
            }

            return null;
        }

        return null;
    }
}
