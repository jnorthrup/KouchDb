import junit.framework.Assert;
import kouchdb.command.DbInfo;
import kouchdb.command.WsFrame;
import kouchdb.io.PackedPayload;
import org.junit.Test;

import java.nio.ByteBuffer;

import java.nio.charset.StandardCharsets;

/**
 * Created by jim on 5/22/14.
 */
public class WsFrameCheck
{
    @Test public void testWsFrameDbInfo(){

        WsFrame wsFrame = new WsFrame(){

            private DbInfo   dbInfo = () -> "test";

            @Override
            public DbInfo getDbInfo() {
                return dbInfo;

            }
        };

        ByteBuffer payload = ByteBuffer.allocate(4 << 10);
        PackedPayload<WsFrame> wsFramePackedPayload = PackedPayload.create(WsFrame.class);
        wsFramePackedPayload.put(wsFrame, payload);
        
        System.err.println(StandardCharsets.UTF_8.decode(((ByteBuffer) payload.flip()).duplicate()));

        WsFrame wsFrame1 = wsFramePackedPayload.get(WsFrame.class, payload);
        DbInfo dbInfo = wsFrame1.getDbInfo();
        String db = dbInfo.getDb();
        Assert.assertEquals(db,"test");

    }
}
