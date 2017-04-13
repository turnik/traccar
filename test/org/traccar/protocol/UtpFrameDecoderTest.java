package org.traccar.protocol;


import org.junit.Assert;
import org.junit.Test;
import org.traccar.ProtocolTest;

import java.nio.ByteOrder;

public class UtpFrameDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {
        UtpFrameDecoder decoder = new UtpFrameDecoder();

        Assert.assertEquals(
                binary(ByteOrder.BIG_ENDIAN, "626C6B000000360100044C1A00003202070810025757C9570000FF00000A000A8F311A1203007F8000007F80000000000040000034B20A040409C4454600FA0001"),
                decoder.decode(null, null, binary(ByteOrder.BIG_ENDIAN, "626C6B000000360100044C1A00003202070810025757C9570000FF00000A000A8F311A1203007F8000007F80000000000040000034B20A040409C4454600FA0001"))
        );
    }
}
