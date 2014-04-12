package kouchdb;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import kouchdb.util.Rfc822HeaderState;
import one.xio.HttpMethod;
import one.xio.HttpStatus;

import javax.xml.bind.DatatypeConverter;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.concurrent.Executors;

import static java.lang.StrictMath.min;
import static one.xio.HttpHeaders.*;

/**
 * Created by jim on 4/11/14.
 */
public interface Kouch {


}
