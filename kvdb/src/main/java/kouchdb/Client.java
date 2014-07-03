package kouchdb;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import kouchdb.command.DbInfo;
import kouchdb.command.DbInfoResponse;
import kouchdb.command.WsFrame;
import one.xio.HttpMethod;
import one.xio.HttpStatus;
import prauto.io.PackedPayload;
import rxf.core.Rfc822HeaderState;
import rxf.core.WebSocketFrame;
import rxf.core.WebSocketFrameBuilder;

import java.io.IOException;
import java.lang.Override;
import java.lang.String;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static one.xio.HttpHeaders.*;

/**
 * Created by jim on 5/12/14.
 */
public class Client {

    public static final Random RANDOM = new Random();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {


        AsynchronousSocketChannel asynchronousSocketChannel = AsynchronousSocketChannel.open();
        asynchronousSocketChannel.connect(new InetSocketAddress("localhost", 1984)).get();
        Rfc822HeaderState rfc822HeaderState = new Rfc822HeaderState();
        byte[] nonce = new byte[16];
        RANDOM.nextBytes(nonce);
        String encode = BaseEncoding.base64().encode(nonce);
        String accept = BaseEncoding.base64().encode(Hashing.sha1().hashString(encode + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11", UTF_8).asBytes());
        Rfc822HeaderState.HttpRequest httpRequest = (Rfc822HeaderState.HttpRequest) rfc822HeaderState.$req().method(HttpMethod.GET)
                .path("/_connect")
                .headerString(Connection, "Upgrade")
                .headerString(Host, "localhost:1984")
                .headerString(Origin, "localhost:1984")
                .headerString(Sec$2dWebSocket$2dKey, encode)
                .headerString(Sec$2dWebSocket$2dProtocol, "kvdb")
                .headerString(Sec$2dWebSocket$2dVersion, "13")
                .headerString(Upgrade, "websocket");
        ByteBuffer as = httpRequest.as(ByteBuffer.class);
        try {
            Integer integer = asynchronousSocketChannel.write(as).get();
            ByteBuffer dst = ByteBuffer.allocateDirect(4 << 10);
            Integer integer1 = asynchronousSocketChannel.read(dst).get();
            Rfc822HeaderState.HttpResponse httpResponse = new Rfc822HeaderState().$res();
            boolean apply = httpResponse.headerInterest(
                    Connection,
                    Sec$2dWebSocket$2dAccept,
                    Sec$2dWebSocket$2dProtocol,
                    Upgrade
            ).apply((ByteBuffer) dst.flip());
            assert apply : "no cursor implemented";
            assert httpResponse.statusEnum() == HttpStatus.$101 : "expected result code 101";
            assert accept.equalsIgnoreCase(httpResponse.headerString(Sec$2dWebSocket$2dAccept));
            System.err.println("");

            PackedPayload<WsFrame> wsFramePackedPayload = PackedPayload.create(WsFrame.class);

            WsFrame wsFrame = new WsFrame() {
                @Override
                public DbInfo getDbInfo() {
                    return new DbInfo() {
                        @Override
                        public String getDb() {
                            return "test";
                        }
                    };
                }
            };
            ByteBuffer payload = ByteBuffer.allocate(4 << 10);
            wsFramePackedPayload.put(wsFrame, payload);
            WebSocketFrame webSocketFrame = new WebSocketFrameBuilder()
                    .setIsMasked(true)
                    .setOpcode(WebSocketFrame.OpCode.binary)
                    .createWebSocketFrame();
            ByteBuffer header = webSocketFrame.as((ByteBuffer) payload.flip());


            asynchronousSocketChannel.write(header).get();
            asynchronousSocketChannel.write((ByteBuffer) payload.flip()).get();

            asynchronousSocketChannel.read((ByteBuffer) payload.clear()).get();
            payload.flip();
            DbInfoResponse dbInfoResponse = PackedPayload.create(DbInfoResponse.class).get(DbInfoResponse.class, payload);

            System.err.println("" + dbInfoResponse.getDbName());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}