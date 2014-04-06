package kouchdb.util;

import javolution.text.TextBuilder;
import one.xio.HttpHeaders;
import one.xio.HttpMethod;
import one.xio.HttpStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.abs;
import static java.util.Arrays.asList;
import static kouchdb.util.Utf8.UTF8;

/**
 * this is a utility class to parse a HttpRequest header or
 * $res header according to declared need of
 * header/cookies downstream.
 * <p/>
 * since java string parsing can be expensive and addHeaderInterest
 * can be numerous this class is designed to parse only
 * what is necessary or typical and enable slower dynamic
 * grep operations to suit against a captured
 * {@link java.nio.ByteBuffer} as needed (still cheap)
 * <p/>
 * preload addHeaderInterest and cookies, send $res
 * and HttpRequest initial onRead for .apply()
 * <p/>
 * <p/>
 * <p/>
 * User: jim
 * Date: 5/19/12
 * Time: 10:00 PM
 */
public class Rfc822HeaderState {
//TODO: convert Map <String,int[]> to IntBuffer keys;short[][]values; create once and store with instance.
//TODO: extend int[] beyond 2


    public static final String HTTP = "HTTP";
    public static final String HTTP_1_1 = HTTP + "/1.1";
    public static final char SPC = ' ';
    public static final char CR = '\r';
    public static final char LF = '\n';
    public static final String CRLF = "" + CR + LF;
    private static final char COLON = ':';
    public static final String COLONSPC = "" + COLON + SPC;
    public AtomicReference<String[]> headerInterest = new AtomicReference<>();
    Pair cookies;
    /**
     * the source route from the active socket.
     * <p/>
     * this is necessary to look up  GeoIpService queries among other things
     */
    private AtomicReference<InetAddress> sourceRoute = new AtomicReference<>();
    /**
     * stored buffer from which things are parsed and later grepped.
     * <p/>
     * NOT atomic.
     */
    private ByteBuffer headerBuf;
    /**
     * parsed valued post-{@link #apply(java.nio.ByteBuffer)}
     */
    private AtomicReference<Map<String, String>> headerStrings = new AtomicReference<>();
    /**
     * dual purpose HTTP protocol header token found on the first line of a HttpRequest/$res in the first position.
     * <p/>
     * contains either the method (HttpRequest) or a the "HTTP/1.1" string (the protocol) on responses.
     * <p/>
     * user is responsible for populating this on outbound addHeaderInterest
     */
    private AtomicReference<String> methodProtocol = new AtomicReference<>();
    /**
     * dual purpose HTTP protocol header token found on the first line of a HttpRequest/$res in the second position
     * <p/>
     * contains either the path (HttpRequest) or a the numeric result code on responses.
     * <p/>
     * user is responsible for populating this on outbound addHeaderInterest
     */
    private AtomicReference<String> pathRescode = new AtomicReference<>();
    /**
     * Dual purpose HTTP protocol header token found on the first line of a HttpRequest/$res in the third position.
     * <p/>
     * Contains either the protocol (HttpRequest) or a status line message ($res)
     */
    private AtomicReference<String> protocolStatus = new AtomicReference<>();
    /**
     * passed in on 0.0.0.0 dispatch to tie the header state to an nio object, to provide a socketchannel handle, and to lookup up the incoming source route
     */
    private AtomicReference<SelectionKey> sourceKey = new AtomicReference<>();

    /**
     * copy ctor
     * <p/>
     * jrn: moved most things to atomic state soas to provide letter-envelope abstraction without
     * undue array[1] members to do the same thing.
     *
     * @param proto the original Rfc822HeaderState
     */
    public Rfc822HeaderState(Rfc822HeaderState proto) {
        cookies = proto.cookies;
        headerBuf = proto.headerBuf;
        headerInterest = proto.headerInterest;
        headerStrings = proto.headerStrings;
        methodProtocol = proto.methodProtocol;
        pathRescode = proto.pathRescode;
        protocolStatus = proto.protocolStatus;
        sourceKey = proto.sourceKey;
        sourceRoute = proto.sourceRoute;
    }

    /**
     * default ctor populates {@link #headerInterest}
     *
     * @param headerInterest keys placed in     {@link #headerInterest} which will be parsed on {@link #apply(java.nio.ByteBuffer)}
     */
    public Rfc822HeaderState(String... headerInterest) {

        this.headerInterest.set(headerInterest);
    }

    /**
     * 'do the right thing' when handed a buffer with no remaining bytes.
     *
     * @param buf
     * @return
     */
    public static ByteBuffer avoidStarvation(ByteBuffer buf) {
        if (0 == buf.remaining()) {
            buf.rewind();
        }
        return buf;
    }

    public static String[] staticHeaderStrings(HttpHeaders... replaceInterest) {
        String[] strings = new String[replaceInterest.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = replaceInterest[i].getHeader();
        }
        return strings;
    }

    public static String dequote(String s) {
        String ret = s;
        if (null != s && ret.startsWith("\"") && ret.endsWith("\"")) {
            ret = ret.substring(1, ret.lastIndexOf('"'));
        }

        return ret;
    }

    public static boolean moveCaretToDoubleEol(ByteBuffer buffer) {
        int distance;
        int eol = buffer.position();

        do {
            int prev = eol;
            while (buffer.hasRemaining() && LF != buffer.get()) ;
            eol = buffer.position();
            distance = abs(eol - prev);
            if (2 == distance && CR == buffer.get(eol - 2))
                return true;
        } while (buffer.hasRemaining() && 1 < distance);
        return false;
    }

    public String headerString(one.xio.HttpHeaders httpHeader) {
        return headerString(httpHeader.getHeader()); //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * simple wrapper for HttpRequest setters
     */
    public Rfc822HeaderState.HttpRequest $req() {
        return Rfc822HeaderState.HttpRequest.class == this.getClass() ? (Rfc822HeaderState.HttpRequest) this : new Rfc822HeaderState.HttpRequest(this);
    }

    /**
     * simple wrapper for HttpRequest setters
     */
    public Rfc822HeaderState.HttpResponse $res() {
        return Rfc822HeaderState.HttpResponse.class == this.getClass() ? (Rfc822HeaderState.HttpResponse) this : new Rfc822HeaderState.HttpResponse(this);
    }

    public <T> T as(final Class<T> clazz) {
        if (clazz.equals(Rfc822HeaderState.HttpResponse.class)) {
            return (T) $res();

        } else if (clazz.equals(Rfc822HeaderState.HttpRequest.class)) {
            return (T) $req();
        } else if (clazz.equals(String.class)) {
            return (T) toString();
        } else if (clazz.equals(ByteBuffer.class)) {
            throw new UnsupportedOperationException(
                    "must promote to as((HttpRequest|HttpResponse)).class first");
        } else
            throw new UnsupportedOperationException("don't know how to infer " + clazz.getCanonicalName());

    }

    /**
     * terminates header keys
     */
    public Rfc822HeaderState headerString(HttpHeaders hdrEnum, String s) {
        return headerString(hdrEnum.getHeader().trim(), s); //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * assigns a state parser to a  {@link java.nio.channels.SelectionKey} and attempts to grab the source route froom the active socket.
     * <p/>
     * this is necessary to look up GeoIpService queries among other things
     *
     * @param key a NIO select key
     * @return self
     * @throws java.io.IOException
     */
    public Rfc822HeaderState sourceKey(SelectionKey key) throws IOException {
        sourceKey.set(key);
        SocketChannel channel = (SocketChannel) sourceKey.get().channel();
        sourceRoute.set(channel.socket().getInetAddress());
        return this;
    }

    /**
     * the actual {@link java.nio.ByteBuffer} associated with the state.
     * <p/>
     * this buffer must start at position 0 in most cases requiring {@link   java.nio.channels.ReadableByteChannel#read(java.nio.ByteBuffer)}
     *
     * @return what is sent to {@link #apply(java.nio.ByteBuffer)}
     */

    public ByteBuffer headerBuf() {
        return headerBuf;
    }

    /**
     * this is a grep of the full header state to find one or more headers of a given name.
     * <p/>
     * performs rewind
     *
     * @param header a header name
     * @return a list of values
     */
    List<String> getHeadersNamed(String header) {
        CharBuffer charBuffer = CharBuffer.wrap(header);
        ByteBuffer henc = UTF8.encode(charBuffer);

        Pair<ByteBuffer, ? extends Pair> ret = headerExtract(henc);

        List<String> objects = new ArrayList<>();
        while (null != ret) {
            objects.add(UTF8.decode(ret.getA()).toString());
            ret = ret.getB();
        }

        return objects;
    }

    /**
     * this is agrep of the full header state to find one or more headers of a given name.
     * <p/>
     * performs rewind
     *
     * @param theHeader a header enum
     * @return a list of values
     */
    List<String> getHeadersNamed(HttpHeaders theHeader) {

        Pair<ByteBuffer, ? extends Pair> ret = headerExtract(theHeader.getToken());

        List<String> objects = new ArrayList<>();
        while (null != ret) {
            objects.add(UTF8.decode(ret.getA()).toString());
            ret = ret.getB();
        }

        return objects;
    }

    /**
     * string-averse buffer based header extraction
     *
     * @param hdrEnc a header token
     * @return a backwards singley-linked list of pairs.
     */
    public Pair<ByteBuffer, ? extends Pair> headerExtract(ByteBuffer hdrEnc) {
        hdrEnc = (ByteBuffer) hdrEnc.asReadOnlyBuffer().rewind();
        ByteBuffer buf = avoidStarvation(headerBuf());

        Pair<ByteBuffer, ? extends Pair> ret = null;
        int hdrTokenEnd = hdrEnc.limit();

        while (buf.hasRemaining()) {
            int begin = buf.position();
            while (buf.hasRemaining() && ':' != buf.get() && buf.position() - 1 - begin <= hdrTokenEnd) ;
            int tokenEnd = buf.position() - 1 - begin;
            if (tokenEnd == hdrTokenEnd) {
                ByteBuffer sampleHdr =
                        (ByteBuffer) ((ByteBuffer) buf.duplicate().position(begin)).slice().limit(hdrTokenEnd);
                if (sampleHdr.equals(hdrEnc.rewind())) {
                    //found it for sure
                    begin = buf.position();
                    while (buf.hasRemaining()) {
                        int endl = buf.position();
                        byte b;
                        while (buf.hasRemaining() && LF != (b = buf.get())) {
                            if (!Character.isWhitespace(b)) {
                                endl = buf.position();
                            }
                        }

                        buf.mark();
                        if (buf.hasRemaining()) {
                            b = buf.get();
                            if (!Character.isWhitespace(b)) {

                                ByteBuffer outBuf =
                                        (ByteBuffer) ((ByteBuffer) buf.reset()).duplicate().position(begin).limit(endl);
                                while (outBuf.hasRemaining()
                                        && Character.isWhitespace(((ByteBuffer) outBuf.mark()).get())) {
                                }
                                outBuf.reset();//ltrim()
                                ret = new Pair<ByteBuffer, Pair<ByteBuffer, ? extends Pair>>(outBuf, ret);
                                break;
                            }
                        }

                    }
                }
            }
            if (buf.remaining() > hdrTokenEnd + 3) {
                while (buf.hasRemaining() && LF != buf.get()) {
                }
            }
        }
        return ret;
    }

    /**
     * direction-agnostic RFC822 header state is mapped from a ByteBuffer with tolerance for HTTP method and results in the first line.
     * <p/>
     * {@link #headerInterest } contains a list of addHeaderInterest that will be converted to a {@link java.util.Map} and available via {@link Rfc822HeaderState#headerStrings()}
     * <p/>
     *
     * @param cursor
     * @return completion
     */
    public boolean  apply(ByteBuffer cursor) {
        if (!cursor.hasRemaining()) cursor.flip();
        int anchor = cursor.position();
        ByteBuffer slice = cursor.duplicate().slice();
        while (slice.hasRemaining() && SPC != slice.get()) ;
        methodProtocol.set(UTF8.decode((ByteBuffer) slice.flip()).toString().trim());//todo: nuke the trim leaks.

        while (cursor.hasRemaining() && SPC != cursor.get()) ;
        slice = cursor.slice();
        while (slice.hasRemaining() && SPC != slice.get()) ;
        pathRescode.set(UTF8.decode((ByteBuffer) slice.flip()).toString().trim());

        while (cursor.hasRemaining() && SPC != cursor.get()) ;
        slice = cursor.slice();
        while (slice.hasRemaining() && LF != slice.get()) ;
        protocolStatus.set(UTF8.decode((ByteBuffer) slice.flip()).toString().trim());

        headerBuf = null;
        boolean wantsCookies = null != cookies;
        boolean wantsHeaders = wantsCookies || 0 < headerInterest.get().length;
        if (moveCaretToDoubleEol(cursor)) {
            headerBuf = (ByteBuffer) cursor.duplicate().flip();
            headerStrings().clear();
            if (wantsHeaders) {
                Map<String, int[]> headerMap = HttpHeaders.getHeaders((ByteBuffer) headerBuf.rewind());
                headerStrings.set(new LinkedHashMap<>());
                for (String o : headerInterest.get()) {
                    int[] o1 = headerMap.get(o);
                    if (null != o1) headerStrings.get().put(
                            o,
                            UTF8.decode((ByteBuffer) headerBuf.duplicate().clear().position(o1[0]).limit(o1[1]))
                                    .toString().trim()
                    );
                }
            }
            return true;
        }
        return false;


    }

    public Rfc822HeaderState headerInterest(HttpHeaders... replaceInterest) {
        String[] strings = staticHeaderStrings(replaceInterest);
        return headerInterest(strings);
    }

    public Rfc822HeaderState headerInterest(String... replaceInterest) {
        headerInterest.set(replaceInterest);
        return this;
    }

    public Rfc822HeaderState addHeaderInterest(HttpHeaders... appendInterest) {
        String[] strings = staticHeaderStrings(appendInterest);
        return addHeaderInterest(strings);
    }

    /**
     * Appends to the Set of header keys this parser is interested in mapping to strings.
     * <p/>
     * these addHeaderInterest are mapped at cardinality<=1 when  {@link #apply(java.nio.ByteBuffer)}  }is called.
     * <p/>
     * for cardinality=>1  addHeaderInterest {@link #getHeadersNamed(String)} is a pure grep over the entire ByteBuffer.
     * <p/>
     *
     * @param newInterest
     * @return
     * @see #getHeadersNamed(String)
     * @see #apply(java.nio.ByteBuffer)
     */
    public Rfc822HeaderState addHeaderInterest(String... newInterest) {

        //adds a few more instructions than the blind append but does what was desired
        Set<String> theCow =
                new CopyOnWriteArraySet<>(Arrays.<String>asList(headerInterest.get()));
        theCow.addAll(asList(newInterest));
        String[] strings = theCow.toArray(new String[theCow.size()]);
        Arrays.sort(strings);
        headerInterest.set(strings);

        return this;
    }

    /**
     * @return
     * @see #headerInterest
     */

    public String[] headerInterest() {
        headerInterest.compareAndSet(null, new String[]{});
        return headerInterest.get();
    }

    /**
     * @return inet4 addr
     * @see #sourceRoute
     */
    public InetAddress sourceRoute() {
        return sourceRoute.get();
    }

    /**
     * this holds an inet address which may be inferred diuring {@link #sourceKey(java.nio.channels.SelectionKey)} as well as directly
     *
     * @param sourceRoute an internet ipv4 address
     * @return self
     */
    public Rfc822HeaderState sourceRoute(InetAddress sourceRoute) {
        this.sourceRoute.set(sourceRoute);
        return this;
    }

    /**
     * this is what has been sent to {@link #apply(java.nio.ByteBuffer)}.
     * <p/>
     * care must be taken to avoid {@link java.nio.ByteBuffer#compact()} during the handling of
     * the dst/cursor found in AsioVisitor code if this is sent in without a clean ByteBuffer.
     *
     * @param headerBuf an immutable  {@link  java.nio.ByteBuffer}
     * @return self
     */
    public Rfc822HeaderState headerBuf(ByteBuffer headerBuf) {
        this.headerBuf = headerBuf;
        return this;
    }

    /**
     * holds the values parsed during {@link #apply(java.nio.ByteBuffer)} and holds the key-values created as addHeaderInterest in
     * {@link #asRequestHeaderByteBuffer()} and {@link #asResponseHeaderByteBuffer()}
     *
     * @return
     */
    public Rfc822HeaderState headerStrings(Map<String, String> headerStrings) {
        this.headerStrings.set(headerStrings);
        return this;
    }

    /**
     * header values which are pre-parsed during {@link #apply(java.nio.ByteBuffer)}.
     * <p/>
     * addHeaderInterest in the HttpRequest/HttpResponse not so named in this list will be passed over.
     * <p/>
     * the value of a header appearing more than once is unspecified.
     * <p/>
     * multiple occuring headers require {@link #getHeadersNamed(String)}
     *
     * @return the parsed values designated by the {@link #headerInterest} list of keys.  addHeaderInterest present in {@link #headerInterest}
     * not appearing in the {@link java.nio.ByteBuffer} input will not be in this map.
     */
    public Map<String, String> headerStrings() {

        headerStrings.compareAndSet(null, new LinkedHashMap<>());
        return headerStrings.get();
    }

    /**
     * @return
     * @see #methodProtocol
     */
    public String methodProtocol() {
        return methodProtocol.get();
    }

    /**
     * @return
     * @see #methodProtocol
     */
    public Rfc822HeaderState methodProtocol(String methodProtocol) {
        this.methodProtocol.set(methodProtocol);
        return this;
    }

    /**
     * dual purpose HTTP protocol header token found on the first line of a HttpRequest/HttpResponse in the second position
     * contains either the path (HttpRequest) or a the numeric result code on responses.
     * user is responsible for populating this on outbound addHeaderInterest
     *
     * @return
     * @see #pathRescode
     */

    public String pathResCode() {
        return pathRescode.get();
    }

    /**
     * @return
     * @see #pathRescode
     */
    public Rfc822HeaderState pathResCode(String pathRescode) {
        this.pathRescode.set(pathRescode);
        return this;
    }

    /**
     * Dual purpose HTTP protocol header token found on the first line of a HttpRequest/HttpResponse in the third position.
     * <p/>
     * Contains either the protocol (HttpRequest) or a status line message (HttpResponse)
     */
    public String protocolStatus() {
        return protocolStatus.get();
    }

    /**
     * @see Rfc822HeaderState#protocolStatus()
     */
    public Rfc822HeaderState protocolStatus(String protocolStatus) {
        this.protocolStatus.set(protocolStatus);
        return this;
    }

    /**
     * writes method, headersStrings, and cookieStrings to a {@link String } suitable for Response addHeaderInterest
     * <p/>
     * populates addHeaderInterest from {@link #headerStrings}
     * <p/>
     *
     * @return http addHeaderInterest for use with http 1.1
     */
    public String asResponseHeaderString() {
        String protocol =
                (null == methodProtocol() ? HTTP_1_1 : methodProtocol()) + SPC + pathResCode() + SPC
                        + protocolStatus() + CRLF;
        for (Map.Entry<String, String> stringStringEntry : headerStrings().entrySet()) {
            protocol += stringStringEntry.getKey() + COLONSPC + stringStringEntry.getValue() + CRLF;
        }

        protocol += CRLF;
        return protocol;
    }

    /**
     * writes method, headersStrings, and cookieStrings to a {@link java.nio.ByteBuffer} suitable for Response addHeaderInterest
     * <p/>
     * populates addHeaderInterest from {@link #headerStrings}
     * <p/>
     *
     * @return http addHeaderInterest for use with http 1.1
     */
    public ByteBuffer asResponseHeaderByteBuffer() {
        String protocol = asResponseHeaderString();
        return ByteBuffer.wrap(protocol.getBytes( UTF8));
    }

    /**
     * writes method, headersStrings, and cookieStrings to a {@link String} suitable for RequestHeaders
     * <p/>
     * populates addHeaderInterest from {@link #headerStrings}
     *
     * @return http addHeaderInterest for use with http 1.1
     */
    public String asRequestHeaderString() {
        TextBuilder builder = new TextBuilder();
        builder.append(methodProtocol()).append(SPC).append(pathResCode()).append(SPC).append(
                null == protocolStatus() ? HTTP_1_1 : protocolStatus()).append(CRLF);
        for (Map.Entry<String, String> stringStringEntry : headerStrings().entrySet())
            builder.append(stringStringEntry.getKey()).append(COLONSPC).append(
                    stringStringEntry.getValue()).append(CRLF);

        builder.append(CRLF);
        return builder.toString();
    }

    /**
     * writes method, headersStrings, and cookieStrings to a {@link java.nio.ByteBuffer} suitable for RequestHeaders
     * <p/>
     * populates addHeaderInterest from {@link #headerStrings}
     *
     * @return http addHeaderInterest for use with http 1.1
     */
    public ByteBuffer asRequestHeaderByteBuffer() {
        String protocol = asRequestHeaderString();
        return ByteBuffer.wrap(protocol.getBytes( UTF8));
    }

    /**
     * utliity shortcut method to get the parsed value from the {@link #headerStrings} map
     *
     * @param headerKey name of a header presumed to be parsed during {@link #apply(java.nio.ByteBuffer)}
     * @return the parsed value from the {@link #headerStrings} map
     */
    public String headerString(String headerKey) {
        return headerStrings().get(headerKey); //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * utility method to strip quotes off of things that makes couchdb choke
     *
     * @param headerKey name of a header
     * @return same string without quotes
     */
    public String dequotedHeader(String headerKey) {
        String s = headerString(headerKey);
        return dequote(s);
    }

    /**
     * setter for a header (String)
     *
     * @param key headername
     * @param val header value
     * @see #headerStrings
     */
    public Rfc822HeaderState headerString(String key, String val) {
        headerStrings().put(key, val);
        return this;
    }

    /**
     * @return the key
     * @see #sourceKey
     */
    public SelectionKey sourceKey() {
        return sourceKey.get(); //To change body of created methods use File | Settings | File Templates.
    }

    @SuppressWarnings({"RedundantCast"})
    public static class HttpRequest extends Rfc822HeaderState {
        public static final ByteBuffer[] EMPTY_BBAR = new ByteBuffer[0];
        private ByteBuffer[] cookieInterest;
        private Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> parsedCookies;

        public HttpRequest(Rfc822HeaderState proto) {
            super(proto);
            String protocol = protocol();
            if (null != protocol && !protocol.startsWith(HTTP)) {
                protocol(null);
            }
        }

        public String method() {
            return methodProtocol(); //To change body of overridden methods use File | Settings | File Templates.
        }

        public HttpRequest method(HttpMethod method) {
            return method(method.name()); //To change body of overridden methods use File | Settings | File Templates.
        }

        public HttpRequest method(String s) {
            return (HttpRequest) methodProtocol(s);
        }

        public String path() {
            return pathResCode(); //To change body of overridden methods use File | Settings | File Templates.
        }

        public HttpRequest path(String path) {
            return (HttpRequest) pathResCode(path);
        }

        public String protocol() {
            return protocolStatus(); //To change body of overridden methods use File | Settings | File Templates.
        }

        public HttpRequest protocol(String protocol) {
            return (HttpRequest) protocolStatus(protocol); //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public String toString() {
            return asRequestHeaderString();
        }

        public <T> T as(Class<T> clazz) {
            if (ByteBuffer.class.equals(clazz)) {
                if (null == protocol()) {
                    protocol(HTTP_1_1);
                }
                return (T) asRequestHeaderByteBuffer();
            }
            return (T) super.as(clazz);
        }

        /**
         * warning !!! interns your keys. thinking of high tx here.
         *
         * @param keys
         * @return
         */
        public HttpRequest cookieInterest(String... keys) {
            if (0 == keys.length) { //rare event
                Set<String> strings = new CopyOnWriteArraySet<>(asList(headerInterest()));
                strings.remove(HttpHeaders.Cookie.getHeader());
                headerInterest(strings.toArray(new String[strings.size()]));

                cookieInterest = null;
            } else {
                addHeaderInterest(HttpHeaders.Cookie);
                cookieInterest = new ByteBuffer[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    String s = keys[i];
                    cookieInterest[i] = ByteBuffer.wrap(s.intern().getBytes(UTF8));
                }
            }

            return this;
        }

        /**
         * @return slist of cookie pairs
         */
        Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> parsedCookies() {

            if (null != parsedCookies)
                return parsedCookies;
            else {
                cookieInterest = null == cookieInterest ? EMPTY_BBAR : cookieInterest;
                Pair<ByteBuffer, ? extends Pair> p1 = headerExtract(HttpHeaders.Cookie.getToken());
                parsedCookies = null;
                while (null != p1) {
                    Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> p2 =
                            CookieRfc6265Util.parseCookie(p1.getA());
                    if (parsedCookies != null) {//seek to null of prev.
                        Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> p3 = parsedCookies;
                        Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> p4 = parsedCookies;
                        while (p3 != null)
                            p3 = (p4 = p3).getB();
                        parsedCookies = new Pair<Pair<ByteBuffer, ByteBuffer>, Pair>(p4.getA(), p2);
                    } else {
                        parsedCookies = p2;
                    }
                    p1 = p1.getB();

                }
            }
            return parsedCookies;
        }

        /**
         * warning: interns the keys.  make them count!
         *
         * @param keys optional list of keys , default is full cookieInterest
         * @return stringy cookie map
         */
        public Map<String, String> getCookies(String... keys) {

            ByteBuffer[] k;
            if (0 >= keys.length) {
                k = cookieInterest;
            } else {
                k = new ByteBuffer[keys.length];
                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];
                    k[i] = (ByteBuffer) ByteBuffer.wrap(key.intern().getBytes(UTF8));
                }
            }
            Map<String, String> ret = new TreeMap<>();
            Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> pair = parsedCookies();
            List<ByteBuffer> kl = new LinkedList<>(asList(k));
            while (null != pair && !kl.isEmpty()) {
                Pair<ByteBuffer, ByteBuffer> a1 = pair.getA();
                ByteBuffer ckey = (ByteBuffer) a1.getA();
                ListIterator<ByteBuffer> ki = kl.listIterator();
                while (ki.hasNext()) {
                    ByteBuffer interestKey = ki.next();
                    if (interestKey.equals(ckey)) {
                        ret
                                .put(UTF8.decode(interestKey).toString().intern(), UTF8.decode(a1.getB())
                                        .toString());
                        ki.remove();
                        break;
                    }
                }
                pair = pair.getB();
            }
            return ret;
        }

        /**
         * warning!  interns the key.  make it count!
         *
         * @param key
         * @return cookie value
         */

        public String getCookie(String key) {
            ByteBuffer k = (ByteBuffer) ByteBuffer.wrap(key.intern().getBytes(UTF8)).mark();
            Pair<Pair<ByteBuffer, ByteBuffer>, ? extends Pair> pair = parsedCookies();
            while (null != pair) {

                Pair<ByteBuffer, ByteBuffer> a1 = pair.getA();
                ByteBuffer a = (ByteBuffer) a1.getA();
                if (a.equals(k)) {
                    return String.valueOf(UTF8.decode(avoidStarvation((ByteBuffer) a1
                            .getB())));
                }
                pair = pair.getB();
            }
            return null;
        }
    }

    public static class HttpResponse extends Rfc822HeaderState {

        public HttpResponse(Rfc822HeaderState proto) {
            super(proto);
            String protocol = protocol();
            if (null != protocol && !protocol.startsWith(HTTP)) {
                protocol(null);
            }
        }

        public HttpStatus statusEnum() {
            try {
                return HttpStatus.valueOf('$' + resCode());
            } catch (Exception e) {
                e.printStackTrace(); //todo: verify for a purpose
            }
            return null;
        }

        @Override
        public String toString() {
            return asResponseHeaderString();
        }

        public String protocol() {
            return methodProtocol();
        }

        public String resCode() {
            return pathResCode();
        }

        public String status() {
            return protocolStatus();
        }

        public HttpResponse protocol(String protocol) {
            return (HttpResponse) methodProtocol(protocol);
        }

        public HttpResponse resCode(String res) {
            return (HttpResponse) pathResCode(res);
        }

        public HttpResponse resCode(HttpStatus resCode) {
            return (HttpResponse) pathResCode(resCode.name().substring(1));
        }

        public HttpResponse status(String status) {
            return (HttpResponse) protocolStatus(status);
        }

        /**
         * convenience method ^2 -- sets rescode and status captions from same enum
         *
         * @param httpStatus
         * @return
         */
        public HttpResponse status(HttpStatus httpStatus) {
            return ((HttpResponse) protocolStatus(httpStatus.caption)).resCode(httpStatus);
        }

        @Override
        public <T> T as(Class<T> clazz) {
            if (ByteBuffer.class.equals(clazz)) {
                if (null == protocol()) {
                    protocol(HTTP_1_1);
                }
                return (T) asResponseHeaderByteBuffer();
            }
            return super.as(clazz); //To change body of overridden methods use File | Settings | File Templates.

        }
    }
}
