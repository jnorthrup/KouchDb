package kouchdb;

import kouchdb.command.DbInfo;
import kouchdb.command.DbInfoResponse;
import kouchdb.command.WsFrame;
import kouchdb.io.PackedPayload;
import one.xio.AsioVisitor;
import one.xio.HttpStatus;
import rxf.core.*;

import java.io.IOException;

import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static one.xio.AsioVisitor.*;

/**
 * Created by jim on 4/11/14.
 */
public class Kouch {


    final private static Integer port = Integer.valueOf(Config.get("PORT", "1984"));
    final private static String host = Config.get("HOST", "localhost");
    public static final PackedPayload<WsFrame> WS_FRAME_PACKED_PAYLOAD = PackedPayload.create(WsFrame.class);
    static URI ws_uri;

    static {
        try {
            ws_uri = new URI("ws://" + host + ":" + port + "/_connect");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            InetSocketAddress serverSocket = new InetSocketAddress(host, port);
            serverSocketChannel.socket().bind(serverSocket);
            serverSocketChannel.configureBlocking(false);

            ExecutorService exec = Executors.newCachedThreadPool();
            exec.submit(() -> {
                try {
                    Server.enqueue(serverSocketChannel, OP_ACCEPT);
                    Server.init(new Impl() {


                        @Override
                        public void onAccept(SelectionKey key) throws Exception {

                            ServerSocketChannel c = (ServerSocketChannel) key.channel();
                            SocketChannel socketChannel = c.accept();
                            socketChannel.configureBlocking(false);
                            Server.enqueue(socketChannel, OP_READ, new Impl() {
                                ByteBuffer cursor = ByteBuffer.allocateDirect(4 << 10);
                                public Rfc822HeaderState.HttpRequest req;
                                public HttpStatus statusEnum;

                                @Override
                                public void onRead(SelectionKey key) throws Exception {
                                    int read = socketChannel.read(cursor);
                                    if (-1 != read) {
                                        if (null == req)
                                            req = Rfc6455WsInitiator.req();
                                        ByteBuffer buf = (ByteBuffer) cursor.duplicate().flip();
                                        boolean apply = req.apply(buf);
                                        if (apply) {
                                            Rfc6455WsInitiator initr = new Rfc6455WsInitiator();
                                            Rfc822HeaderState.HttpResponse initResponse1 = initr.parseInitiatorRequest(req, ws_uri, host, "kvdb");
                                            response = initResponse1.as(ByteBuffer.class);
                                            statusEnum = initResponse1.statusEnum();
                                            int position = cursor.position();
                                            PackedPayload.reposition(cursor,req.headerBuf().limit());
                                            if (cursor.hasRemaining())
                                                cursor = ByteBuffer.allocateDirect(4 << 10).put(((ByteBuffer) PackedPayload.reposition((ByteBuffer) cursor.flip(),position)).slice());
                                            key.interestOps(OP_WRITE);
                                        }
                                    } else {
                                        key.cancel();
                                    }
                                }

                                ByteBuffer response;

                                @Override
                                public void onWrite(SelectionKey key) throws Exception {
                                    if (response.hasRemaining())
                                        socketChannel.write(response);
                                    else

                                    {
                                        if (HttpStatus.$101 != statusEnum) {
                                            key.cancel();
                                            return;
                                        }
                                        key.interestOps(OP_READ).attach(new Impl() {

                                            @Override
                                            public void onRead(SelectionKey key) throws Exception {

                                                if (0 == cursor.position()) {
                                                    if (-1 == socketChannel.read(cursor)) {
                                                        key.cancel();
                                                        return;
                                                    }

                                                }
                                                WebSocketFrame webSocketFrame = new WebSocketFrame();
                                                int limit = cursor.limit();
                                                int position = cursor.position();
                                                if (!webSocketFrame.apply((ByteBuffer) cursor.flip())) {
                                                    PackedPayload.reposition((ByteBuffer) cursor.limit(limit), position);
                                                    return;
                                                }
                                                ByteBuffer payload = ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength);
                                                ByteBuffer t = cursor;
                                                if (cursor.remaining() > payload.remaining()) {
                                                    t = (ByteBuffer) cursor.slice().limit(payload.remaining());
                                                    PackedPayload.reposition(cursor, cursor.position() + payload.remaining());
                                                }
                                                payload.put(t);

                                                Impl fsm = this;

                                                key.attach(new FinishRead(payload, () -> {
                                                    if(webSocketFrame.isMasked)webSocketFrame.applyMask((ByteBuffer) payload.rewind());
                                                    payload.rewind();
                                                    System.err.println(StandardCharsets.UTF_8.decode(payload.duplicate()));
                                                    WsFrame wsFrame = WS_FRAME_PACKED_PAYLOAD.get(WsFrame.class, payload);
                                                    DbInfo dbInfo = wsFrame.getDbInfo();

                                                    key.interestOps(OP_READ).attach(fsm);
                                                }));
                                            }

                                        });
                                    }
                                }

                            });
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            synchronized (exec) {
                exec.wait();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
