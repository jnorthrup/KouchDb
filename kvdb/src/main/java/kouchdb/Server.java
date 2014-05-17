package kouchdb;

import one.xio.AsioVisitor;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

import static java.lang.StrictMath.min;

/**
 * ws-only http server
 * The handshake from the client looks as follows:
 * <pre>
 * GET /chat HTTP/1.1
 * Host: server.example.com
 * Upgrade: websocket
 * Connection: Upgrade
 * Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
 * Origin: http://example.com
 * Sec-WebSocket-Protocol: chat, superchat
 * Sec-WebSocket-Version: 13
 *
 * The handshake from the server looks as follows:
 *
 * HTTP/1.1 101 Switching Protocols
 * Upgrade: websocket
 * Connection: Upgrade
 * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
 * Sec-WebSocket-Protocol: chat
 *
 *
 * Fette & Melnikov             Standards Track                   [Page 16]
 *
 *
 * RFC 6455                 The WebSocket Protocol            December 2011
 * Once a connection to the server has been established (including a
 * connection via a proxy or over a TLS-encrypted tunnel), the client
 * MUST send an opening handshake to the server.  The handshake consists
 * of an HTTP Upgrade request, along with a list of required and
 * optional header fields.  The requirements for this handshake are as
 * follows.
 *
 * 1.   The handshake MUST be a valid HTTP request as specified by
 * [RFC2616].
 *
 * 2.   The method of the request MUST be GET, and the HTTP version MUST
 * be at least 1.1.
 *
 * For example, if the WebSocket URI is "ws://example.com/chat",
 * the first line sent should be "GET /chat HTTP/1.1".
 *
 * 3.   The "Request-URI" part of the request MUST match the /resource
 * name/ defined in Section 3 (a relative URI) or be an absolute
 * http/https URI that, when parsed, has a /resource name/, /host/,
 * and /port/ that match the corresponding ws/wss URI.
 *
 * 4.   The request MUST contain a |Host| header field whose value
 * contains /host/ plus optionally ":" followed by /port/ (when not
 * using the default port).
 *
 * 5.   The request MUST contain an |Upgrade| header field whose value
 * MUST include the "websocket" keyword.
 *
 *
 *
 *
 *
 * 6.   The request MUST contain a |Connection| header field whose value
 * MUST include the "Upgrade" token.
 *
 * 7.   The request MUST include a header field with the name
 * |Sec-WebSocket-Key|.  The value of this header field MUST be a
 * nonce consisting of a randomly selected 16-byte value that has
 * been base64-encoded (see Section 4 of [RFC4648]).  The nonce
 * MUST be selected randomly for each connection.
 *
 * NOTE: As an example, if the randomly selected value was the
 * sequence of bytes 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 0x09
 * 0x0a 0x0b 0x0c 0x0d 0x0e 0x0f 0x10, the value of the header
 * field would be "AQIDBAUGBwgJCgsMDQ4PEC=="
 *
 * 8.   The request MUST include a header field with the name |Origin|
 * [RFC6454] if the request is coming from a browser client.  If
 * the connection is from a non-browser client, the request MAY
 * include this header field if the semantics of that client match
 * the use-case described here for browser clients.  The value of
 * this header field is the ASCII serialization of origin of the
 * context in which the code establishing the connection is
 * running.  See [RFC6454] for the details of how this header field
 * value is constructed.
 *
 * As an example, if code downloaded from www.example.com attempts
 * to establish a connection to ww2.example.com, the value of the
 * header field would be "http://www.example.com".
 *
 * 9.   The request MUST include a header field with the name
 * |Sec-WebSocket-Version|.  The value of this header field MUST be
 * 13.
 *
 * NOTE: Although draft versions of this document (-09, -10, -11,
 * and -12) were posted (they were mostly comprised of editorial
 * changes and clarifications and not changes to the wire
 * protocol), values 9, 10, 11, and 12 were not used as valid
 * values for Sec-WebSocket-Version.  These values were reserved in
 * the IANA registry but were not and will not be used.
 *
 * 10.  The request MAY include a header field with the name
 * |Sec-WebSocket-Protocol|.  If present, this value indicates one
 * or more comma-separated subprotocol the client wishes to speak,
 * ordered by preference.  The elements that comprise this value
 * MUST be non-empty strings with characters in the range U+0021 to
 * U+007E not including separator characters as defined in
 * [RFC2616] and MUST all be unique strings.  The ABNF for the
 * value of this header field is 1#token, where the definitions of
 * constructs and rules are as given in [RFC2616].
 *
 *
 * 11.  The request MAY include a header field with the name
 * |Sec-WebSocket-Extensions|.  If present, this value indicates
 * the protocol-level extension(s) the client wishes to speak.  The
 * interpretation and format of this header field is described in
 * Section 9.1.
 *
 * 12.  The request MAY include any other header fields, for example,
 * cookies [RFC6265] and/or authentication-related header fields
 * such as the |Authorization| header field [RFC2616], which are
 * processed according to documents that define them.
 *
 * Once the client's opening handshake has been sent, the client MUST
 * wait for a response from the server before sending any further data.
 * The client MUST validate the server's response as follows:
 *
 * 1.  If the status code received from the server is not 101, the
 * client handles the response per HTTP [RFC2616] procedures.  In
 * particular, the client might perform authentication if it
 * receives a 401 status code; the server might redirect the client
 * using a 3xx status code (but clients are not required to follow
 * them), etc.  Otherwise, proceed as follows.
 *
 * 2.  If the response lacks an |Upgrade| header field or the |Upgrade|
 * header field contains a value that is not an ASCII case-
 * insensitive match for the value "websocket", the client MUST
 * _Fail the WebSocket Connection_.
 *
 * 3.  If the response lacks a |Connection| header field or the
 * |Connection| header field doesn't contain a token that is an
 * ASCII case-insensitive match for the value "Upgrade", the client
 * MUST _Fail the WebSocket Connection_.
 *
 * 4.  If the response lacks a |Sec-WebSocket-Accept| header field or
 * the |Sec-WebSocket-Accept| contains a value other than the
 * base64-encoded SHA-1 of the concatenation of the |Sec-WebSocket-
 * Key| (as a string, not base64-decoded) with the string "258EAFA5-E914-47DA-95CA-C5AB0DC85B11" but ignoring any leading and
 * trailing whitespace, the client MUST _Fail the WebSocket
 * Connection_.
 *
 * 5.  If the response includes a |Sec-WebSocket-Extensions| header
 * field and this header field indicates the use of an extension
 * that was not present in the client's handshake (the server has
 * indicated an extension not requested by the client), the client
 * MUST _Fail the WebSocket Connection_.  (The parsing of this
 * header field to determine which extensions are requested is
 * discussed in Section 9.1.)
 *
 *
 *
 *
 *
 * 6.  If the response includes a |Sec-WebSocket-Protocol| header field
 * and this header field indicates the use of a subprotocol that was
 * not present in the client's handshake (the server has indicated a
 * subprotocol not requested by the client), the client MUST _Fail
 * the WebSocket Connection_.
 *
 * If the server's response does not conform to the requirements for the
 * server's handshake as defined in this section and in Section 4.2.2,
 * the client MUST _Fail the WebSocket Connection_.
 */
public class Server implements Closeable {
    final static boolean $DBG = "true".equals(Config.get("KOUCH_DEBUG", "false"));
    private static final Integer KOUCH_BACKLOG = Integer.valueOf(Config.get("KOUCH_BACKLOG", "16"));
    private static final Queue<Object[]> q = new ConcurrentLinkedQueue<>();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    public static Object waitObject = new Object();
    public static Thread selectorThread;
    public static boolean killswitch;
    static URI WS_URI;

    static {
        try {
            WS_URI = new URI(Config.get("KOUCH_WS_URI", "ws://localhost:1984/_connect"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static Selector selector;

    public static String wheresWaldo(int... depth) {
        int d = depth.length > 0 ? depth[0] : 2;
        Throwable throwable = new Throwable();
        Throwable throwable1 = throwable.fillInStackTrace();
        StackTraceElement[] stackTrace = throwable1.getStackTrace();
        String ret = "";
        for (int i = 2, end = min(stackTrace.length - 1, d); i <= end; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            ret +=
                    "\tat " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName()
                            + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber()
                            + ")\n";

        }
        return ret;
    }

    public static Selector getSelector() {
        return selector;
    }

    public static void setSelector(Selector selector) {
        kouchdb.Server.selector = selector;
    }

    /**
     * handles the threadlocal ugliness if any to registering user threads into the selector/reactor pattern
     *
     * @param channel the socketchanel
     * @param op      int ChannelSelector.operator
     * @param s       the payload: grammar {enum,data1,data..n}
     */
    public static void enqueue(SelectableChannel channel, int op, Object... s) {
        assert channel != null && !killswitch : "Server appears to have shut down, cannot enqueue";
        if (Thread.currentThread() == selectorThread)
            try {
                channel.register(getSelector(), op, s);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        else {
            q.add(new Object[]{channel, op, s});
        }
        Selector selector1 = getSelector();
        if (null != selector1)
            selector1.wakeup();
    }

    public static void init(AsioVisitor protocoldecoder, String... a) throws IOException {
        setSelector(Selector.open());
        selectorThread = Thread.currentThread();

        synchronized (a) {
            long timeoutMax = 1024, timeout = 1;

            while (!killswitch) {
                while (!q.isEmpty()) {
                    Object[] s = q.remove();
                    SelectableChannel x = (SelectableChannel) s[0];
                    Selector sel = getSelector();
                    Integer op = (Integer) s[1];
                    Object att = s[2];
                    //          System.err.println("" + op + "/" + String.valueOf(att));
                    try {
                        x.configureBlocking(false);
                        SelectionKey register = x.register(sel, op, att);
                        assert null != register;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                int select = selector.select(timeout);

                timeout = 0 == select ? min(timeout << 1, timeoutMax) : 1;
                if (0 != select)
                    innerloop(protocoldecoder);
            }
        }
    }

    private static void innerloop(AsioVisitor protocoldecoder) throws IOException {
        Set<SelectionKey> keys = selector.selectedKeys();

        for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext(); ) {
            SelectionKey key = i.next();
            i.remove();

            if (key.isValid()) {
                SelectableChannel channel = key.channel();
                try {
                    AsioVisitor m = inferAsioVisitor(protocoldecoder, key);

                    if (key.isValid() && key.isWritable()) {
                        if (((SocketChannel) channel).socket().isOutputShutdown()) {
                            key.cancel();
                        } else {
                            m.onWrite(key);
                        }
                    }
                    if (key.isValid() && key.isReadable()) {
                        if (((SocketChannel) channel).socket().isInputShutdown()) {
                            key.cancel();
                        } else {
                            m.onRead(key);
                        }
                    }
                    if (key.isValid() && key.isAcceptable()) {
                        m.onAccept(key);
                    }
                    if (key.isValid() && key.isConnectable()) {
                        m.onConnect(key);
                    }
                } catch (Throwable e) {
                    Object attachment = key.attachment();
                    if (attachment instanceof Object[]) {
                        Object[] objects = (Object[]) attachment;
                        System.err.println("BadHandler: " + Arrays.deepToString(objects));

                    } else
                        System.err.println("BadHandler: " + String.valueOf(attachment));

                    if (AsioVisitor.$DBG) {
                        AsioVisitor asioVisitor = inferAsioVisitor(protocoldecoder, key);
                        if (asioVisitor instanceof AsioVisitor.Impl) {
                            AsioVisitor.Impl visitor = (AsioVisitor.Impl) asioVisitor;
                            if (AsioVisitor.$origins.containsKey(visitor)) {
                                String s = AsioVisitor.$origins.get(visitor);
                                System.err.println("origin" + s);
                            }
                        }
                    }
                    e.printStackTrace();
                    key.attach(null);
                    channel.close();
                }
            }
        }
    }

    static AsioVisitor inferAsioVisitor(AsioVisitor default$, SelectionKey key) {
        Object attachment = key.attachment();
        AsioVisitor m;
        if (null == attachment)
            m = default$;
        if (attachment instanceof Object[])
            for (Object o : ((Object[]) attachment)) {
            attachment = o;
            break;
        }
        if (attachment instanceof Iterable) {
            Iterable iterable = (Iterable) attachment;
            for (Object o : iterable) {
                attachment = o;
                break;
            }
        }
        m = attachment instanceof AsioVisitor ? (AsioVisitor) attachment : default$;
        return m;
    }

    public static void setKillswitch(boolean killswitch) {
        kouchdb.Server.killswitch = killswitch;
    }

    @Override
    public void close() throws IOException {

    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("REALTIME_UNIT", TimeUnit.MINUTES.name());

        Future<?> submit = EXECUTOR_SERVICE.submit(() -> {
            try {
                ServerSocketChannel channel = ServerSocketChannel.open().bind(new InetSocketAddress(InetAddress.getByName(Config.get("KOUCHDB_BIND", "0.0.0.0")), Integer.parseInt(Config.get("KOUCHDB_PORT", "1984"))));
                channel.configureBlocking(false);
                enqueue(channel, SelectionKey.OP_ACCEPT);
                init(new AsioVisitor() {
                    @Override
                    public void onRead(SelectionKey key) throws Exception {

                    }

                    @Override
                    public void onConnect(SelectionKey key) throws Exception {

                    }

                    @Override
                    public void onWrite(SelectionKey key) throws Exception {

                    }

                    @Override
                    public void onAccept(SelectionKey key) throws Exception {

                    }
                });
            } catch (Exception ignored) {
            }
        });


        Object o = new Object();
        synchronized (o) {
            o.wait();
        }
    }
   public  static int getReceiveBufferSize(){return 4<<10;}
}
