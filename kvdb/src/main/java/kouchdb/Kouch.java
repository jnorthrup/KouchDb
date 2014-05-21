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
    static URI ws_uri;

    static {
        try {
            ws_uri = new URI("ws://" + host + ":" + port + "/_connect");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static final PackedPayload<WsFrame> WS_FRAME_PACKED_PAYLOAD = new PackedPayload<>(WsFrame.class);

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

                        private Rfc822HeaderState.HttpResponse initResponse;

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
                                            Rfc822HeaderState.HttpResponse initResponse1 = initr.parseInitiatorRequest(req, ws_uri, host, "initResponse");
                                            response = initResponse1.as(ByteBuffer.class);
                                            statusEnum = initResponse1.statusEnum();
                                            int position = cursor.position();
                                            cursor.position(req.headerBuf().limit());
                                            if (cursor.hasRemaining())
                                                cursor = ByteBuffer.allocateDirect(4 << 10).put(((ByteBuffer) cursor.flip().position(position)).slice());
                                            key.interestOps(OP_WRITE);
                                        }
                                    } else {
                                        key.cancel();
                                    }
                                }

                                ByteBuffer response;

                                @Override
                                public void onWrite(SelectionKey key) throws Exception {
                                    if (initResponse == null) ;
                                    if (response.hasRemaining())
                                        socketChannel.write(response);
                                    else

                                    {
                                        if (HttpStatus.$101 != statusEnum) key.cancel();
                                        Impl initiated = new Impl() {
                                            @Override
                                            public void onWrite(SelectionKey key) throws Exception {
                                                super.onWrite(key);
                                            }

                                            @Override
                                            public void onRead(SelectionKey key) throws Exception {

                                                if (cursor.position() >= 2) {
                                                    WebSocketFrame webSocketFrame = new WebSocketFrame();
                                                    if (webSocketFrame.apply(cursor)) {
                                                        if (webSocketFrame.payloadLength > cursor.remaining()) {
                                                            ByteBuffer payload = ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(cursor);
                                                            Impl fsm = this;
                                                            new FinishRead(payload, () -> {

                                                                WsFrame wsFrame = WS_FRAME_PACKED_PAYLOAD.get(WsFrame.class, payload);
                                                                ByteBuffer res = ByteBuffer.allocateDirect(4 << 10);

                                                                DbInfo dbInfo = wsFrame.getDbInfo();
                                                                if (null != dbInfo) {
                                                                    String db = dbInfo.getDb();
                                                                    DbInfoResponse dbInfoResponse = new DbInfoResponse() {
                                                                        @Override
                                                                        public boolean getCompactRunning() {
                                                                            return false;
                                                                        }

                                                                        @Override
                                                                        public long getDiskSize() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public long getDocCount() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public long getDocDel_count() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public long getInstanceStart_time() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public long getPurgeSeq() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public long getUpdateSeq() {
                                                                            return 0;
                                                                        }

                                                                        @Override
                                                                        public String getDbName() {
                                                                            return db;
                                                                        }

                                                                        @Override
                                                                        public String getDiskFormat_version() {
                                                                            return "blob";
                                                                        }
                                                                    };
                                                                    PackedPayload.create(DbInfoResponse.class).put(dbInfoResponse, res);

                                                                }
                                                                if (null != payload && payload.hasRemaining()) {
                                                                    WebSocketFrame webSocketFrame1 = new WebSocketFrameBuilder().createWebSocketFrame();
                                                                    boolean apply = webSocketFrame1.apply((ByteBuffer) payload.flip());
                                                                    key.interestOps(OP_WRITE).attach(new FinishWrite(webSocketFrame1.as(payload), () -> key.interestOps(OP_WRITE).attach(new FinishWrite(payload,() -> key.interestOps(OP_READ).attach(fsm)))));
                                                                } else
                                                                    key.interestOps(OP_READ).attach(fsm);

                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        };
                                        initiated.onRead(key);
                                    }
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    System.out.println("failed startup");
                }
            });
            synchronized (exec) {
                exec.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
