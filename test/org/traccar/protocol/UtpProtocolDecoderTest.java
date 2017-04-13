package org.traccar.protocol;

import org.junit.Assert;
import org.junit.Test;
import org.traccar.ProtocolTest;

import java.nio.ByteOrder;

public class UtpProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {
        UtpProtocolDecoder decoder = new UtpProtocolDecoder(new UtpProtocol());

        verifyNothing(decoder, binary(ByteOrder.BIG_ENDIAN,
                "626C6B000000140000007A3030303030383635373333303234383139343237"));
    }
}
