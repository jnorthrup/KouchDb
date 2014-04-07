package kouchdb;

/**
 * Created by jim on 4/6/14.
 */

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * <pre>      0                   1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-------+-+-------------+-------------------------------+
 * |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
 * |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
 * |N|V|V|V|       |S|             |   (if payload len==126/127)   |
 * | |1|2|3|       |K|             |                               |
 * +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
 * |     Extended payload length continued, if payload len == 127  |
 * + - - - - - - - - - - - - - - - +-------------------------------+
 * |                               |Masking-key, if MASK set to 1  |
 * +-------------------------------+-------------------------------+
 * | Masking-key (continued)       |          Payload Data         |
 * +-------------------------------- - - - - - - - - - - - - - - - +
 * :                     Payload Data continued ...                :
 * + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
 * |                     Payload Data continued ...                |
 * +---------------------------------------------------------------+
 * <p>
 * FIN:  1 bit
 * <p>
 * Indicates that this is the final fragment in a message.  The first
 * fragment MAY also be the final fragment.
 * <p>
 * RSV1, RSV2, RSV3:  1 bit each
 * <p>
 * MUST be 0 unless an extension is negotiated that defines meanings
 * for non-zero values.  If a nonzero value is received and none of
 * the negotiated extensions defines the meaning of such a nonzero
 * value, the receiving endpoint MUST _Fail the WebSocket
 * Connection_.
 * <p>
 * <p>
 * <p>
 * <p>
 * Fette & Melnikov             Standards Track                   [Page 28]
 * <p>
 * RFC 6455                 The WebSocket Protocol            December 2011
 * <p>
 * <p>
 * Opcode:  4 bits
 * <p>
 * Defines the interpretation of the "Payload data".  If an unknown
 * opcode is received, the receiving endpoint MUST _Fail the
 * WebSocket Connection_.  The following values are defined.
 * <p>
 *  %x0 denotes a continuation frame
 * <p>
 *  %x1 denotes a text frame
 * <p>
 *  %x2 denotes a binary frame
 * <p>
 *  %x3-7 are reserved for further non-control frames
 * <p>
 *  %x8 denotes a connection close
 * <p>
 *  %x9 denotes a ping
 * <p>
 *  %xA denotes a pong
 * <p>
 *  %xB-F are reserved for further control frames
 * <p>
 * Mask:  1 bit
 * <p>
 * Defines whether the "Payload data" is masked.  If set to 1, a
 * masking key is present in masking-key, and this is used to unmask
 * the "Payload data" as per Section 5.3.  All frames sent from
 * client to server have this bit set to 1.
 * <p>
 * Payload length:  7 bits, 7+16 bits, or 7+64 bits
 * <p>
 * The length of the "Payload data", in bytes: if 0-125, that is the
 * payload length.  If 126, the following 2 bytes interpreted as a
 * 16-bit unsigned integer are the payload length.  If 127, the
 * following 8 bytes interpreted as a 64-bit unsigned integer (the
 * most significant bit MUST be 0) are the payload length.  Multibyte
 * length quantities are expressed in network byte order.  Note that
 * in all cases, the minimal number of bytes MUST be used to encode
 * the length, for example, the length of a 124-byte-long string
 * can't be encoded as the sequence 126, 0, 124.  The payload length
 * is the length of the "Extension data" + the length of the
 * "Application data".  The length of the "Extension data" may be
 * zero, in which case the payload length is the length of the
 * "Application data".
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Fette & Melnikov             Standards Track                   [Page 29]
 * <p>
 * RFC 6455                 The WebSocket Protocol            December 2011
 * <p>
 * <p>
 * Masking-key:  0 or 4 bytes
 * <p>
 * All frames sent from the client to the server are masked by a
 * 32-bit value that is contained within the frame.  This field is
 * present if the mask bit is set to 1 and is absent if the mask bit
 * is set to 0.  See Section 5.3 for further information on client-
 * to-server masking.
 * <p>
 * Payload data:  (x+y) bytes
 * <p>
 * The "Payload data" is defined as "Extension data" concatenated
 * with "Application data".
 * <p>
 * Extension data:  x bytes
 * <p>
 * The "Extension data" is 0 bytes unless an extension has been
 * negotiated.  Any extension MUST specify the length of the
 * "Extension data", or how that length may be calculated, and how
 * the extension use MUST be negotiated during the opening handshake.
 * If present, the "Extension data" is included in the total payload
 * length.
 * <p>
 * Application data:  y bytes
 * <p>
 * Arbitrary "Application data", taking up the remainder of the frame
 * after any "Extension data".  The length of the "Application data"
 * is equal to the payload length minus the length of the "Extension
 * data".
 * <p>
 * </pre>
 */


public class WsFrameDecoder {
    private byte[] maskingKey;
    private long len;
    private boolean isMasked;
    private OpCode opcode;
    private boolean isFin;

    public WsFrameDecoder(AsynchronousSocketChannel socketChannel) {
        ByteBuffer dst = ByteBuffer.allocateDirect(8 << 10);
        socketChannel.read(dst, null, new CompletionHandler<Integer, Void>() {


                    private ByteBuffer payload;

                    @Override
                    public void completed(Integer result, Void attachment) {
                        if (dst.position() > 3) {
                            dst.flip();
                            byte b = dst.get();
                            isFin = (b & 0b10000000) != 0;
                            opcode = OpCode.values()[b & 0b00001111];
                            b = dst.get();
                            isMasked = (b & 0b10000000) != 0;
                            int i = b & 0b01111111;
                            switch (i) {
                                case 126:
                                    len = dst.getShort() & 0xffff;
                                    break;
                                case 127:
                                    len = dst.getLong();
                                    break;
                                default:
                                    len = i;
                                    break;
                            }
                            if (isMasked) dst.get(maskingKey = new byte[4]);
                            if (len > Integer.MAX_VALUE) throw new RuntimeException("length too large: " + len);
                            payload = ByteBuffer.allocateDirect((int) len).put(dst);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {

                    }
                }
        );
    }

    public static void applyMask(byte[] mask, ByteBuffer data) {
        ByteBuffer overwrite = data.duplicate();
        int c = 0;
        while (data.hasRemaining()) overwrite.put((byte) ((mask[c++ % 4] & 0xff ^ data.get() & 0xff) & 0xff));
    }

    public enum OpCode {
        continuation, text, binary,
        reservedDataFrame3,
        reservedDataFrame4,
        reservedDataFrame5,
        reservedDataFrame6,
        reservedDataFrame7,
        close,
        ping,
        pong
    }
}
