package kouchdb.web.inf;

import kouchdb.Server;
import kouchdb.util.Rfc822HeaderState;
import one.xio.AsioVisitor.Impl;
import one.xio.HttpMethod;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.channels.SelectionKey.OP_READ;
import static kouchdb.Server.getReceiveBufferSize;
import static kouchdb.Server.wheresWaldo;
import static kouchdb.web.inf.KouchNamespace.NAMESPACE;
import static one.xio.HttpMethod.GET;
import static one.xio.HttpMethod.POST;

public class ProtocolMethodDispatch extends Impl {

  public static final ByteBuffer NONCE = ByteBuffer.allocateDirect(0);

  /**
   * the PUT protocol handlers, only static for the sake of javadocs
   */
  public static Map<Pattern, Class<? extends Impl>> POSTmap =
      new LinkedHashMap<>();

  /**
   * the GET protocol handlers, only static for the sake of javadocs
   */
  public static Map<Pattern, Class<? extends Impl>> GETmap =
      new LinkedHashMap<>();

  static {
    NAMESPACE.put(POST, POSTmap);
    NAMESPACE.put(GET, GETmap);

    /**
     * for gwt requestfactory done via POST.
     *
     * TODO: rf GET from query parameters
     */
//    POSTmap.put(Pattern.compile("^/gwtRequest"), GwtRequestFactoryVisitor.class);

    /**
     * any url begining with /i is a proxied $req to couchdb but only permits image/* and text/*
     */

    Pattern passthroughExpr = Pattern.compile("^/i(/.*)$");
//    GETmap.put(passthroughExpr, HttpProxyImpl.class/*(passthroughExpr)*/);

    /**
     * general purpose httpd static content server that recognizes .gz and other compression suffixes when convenient
     *
     * any random config mechanism with a default will suffice here to define the content root.
     *
     * widest regex last intentionally
     * system proprty: {value #RXF_SERVER_CONTENT_ROOT}
     */
    GETmap.put(ContentRootCacheImpl.CACHE_PATTERN, ContentRootCacheImpl.class);
    GETmap.put(ContentRootNoCacheImpl.NOCACHE_PATTERN, ContentRootNoCacheImpl.class);
    GETmap.put(Pattern.compile(".*"), ContentRootImpl.class );
  }

  public void onAccept(SelectionKey key) throws IOException {
    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
    SocketChannel accept = channel.accept();
    accept.configureBlocking(false);
     Server.enqueue (accept, OP_READ, this);

  }

  public void onRead(SelectionKey key) throws Exception {
    SocketChannel channel = (SocketChannel) key.channel();

    ByteBuffer cursor = ByteBuffer.allocateDirect(getReceiveBufferSize());
    int read = channel.read(cursor);
    if (-1 == read) {
      ((SocketChannel) key.channel()).socket().close();//cancel();
      return;
    }

    HttpMethod method = null;
    Rfc822HeaderState.HttpRequest httpRequest = null;
    try {
      //find the method to dispatch
        httpRequest = new Rfc822HeaderState().$req();
        boolean apply = httpRequest.apply((ByteBuffer) cursor.flip());
        method = (httpRequest.method());

    } catch (Exception e) {
    }

    if (null == method) {
      ((SocketChannel) key.channel()).socket().close();//cancel();

      return;
    }

    Set<Entry<Pattern, Class<? extends Impl>>> entries = NAMESPACE.get(method).entrySet();
    String path = httpRequest.path();
    for (Entry<Pattern, Class<? extends Impl>> visitorEntry : entries) {
      Matcher matcher = visitorEntry.getKey().matcher(path);
      if (matcher.find()) {
        if (false) {
          System.err.println("+?+?+? using " + matcher.toString());
        }
        Class<? extends Impl> value = visitorEntry.getValue();
        Impl impl;

        impl = value.newInstance();
        Object a[] = {impl, httpRequest, cursor};
        key.attach(a);
        if ( (value.getClass().isAnnotationPresent(PreRead.class))) impl.onRead(key);
        key.selector().wakeup();
        return;
      }

    }
    System.err.println(deepToString("!!!1!1!!", "404", path, "using",
        NAMESPACE));
  }

    public static <T> String deepToString(T... d) {
        return Arrays.deepToString(d) + wheresWaldo();
    }


}
