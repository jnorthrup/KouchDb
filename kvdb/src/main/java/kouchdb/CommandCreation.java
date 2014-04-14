package kouchdb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

/**
 * Created by jim on 4/12/14.
 */
public class CommandCreation implements CompletionHandler<Integer, Object> {
    private WebSocketFsm fsm;

    public CommandCreation(WebSocketFsm fsm) {
        this.fsm = fsm;
    }



    @Override
    public void completed(Integer ignored, Object attachment) {
        while (fsm.cursor.hasRemaining()) {if(-1==ignored) try {
            fsm.socketChannel.close();return;
        } catch (IOException e) {
         }
            WebSocketFrame webSocketFrame = new WebSocketFrame();//todo: keep this cheap
            boolean apply = webSocketFrame.apply((ByteBuffer) fsm.cursor.flip());//todo: keep this cheap
            if (apply) {
                if (webSocketFrame.payloadLength > fsm.cursor.remaining()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) (webSocketFrame.payloadLength - fsm.cursor.remaining()));
                    fsm.socketChannel.read(byteBuffer, attachment, new CompletionHandler<Integer, Object>() {//relinquish control
                        @Override
                        public void completed(Integer ignored, Object attachment) {
                            if (!byteBuffer.hasRemaining()) {//spin till full.  then goto top
                                ByteBuffer slice = null;
                                if (webSocketFrame.isMasked) {
                                    slice = (ByteBuffer) ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(fsm.cursor);
                                    WebSocketFrame.applyMask(webSocketFrame.maskingKey, slice);
                                    WebSocketFrame.applyMask(webSocketFrame.maskingKey, byteBuffer);
                                }

                                command(webSocketFrame, slice, byteBuffer);
                                completed(ignored, attachment);
                                CommandCreation.this.completed(ignored, attachment);//resume control of draining the cursor to the outer outer class loop
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                        }
                    });
                    return;//relinquish control
                } else
                //(webSocketFrame.payloadLength<=cursor.remaining())
                    command(webSocketFrame,   (ByteBuffer) ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(fsm.cursor).rewind());
            } else
                bail();//deficient cursor.  handle elswhere.
            return;
        }
    }

    private void bail() {
        fsm.socketChannel.read(fsm.cursor, null, fsm.startNewFrameHandler);
    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }

    /**
     * receive a series of command fragments and assemble.  each fragment may have more than one buffer.
     *
     * @param webSocketFrame
     * @param byteBuffer
     */
    void command(WebSocketFrame webSocketFrame, ByteBuffer... byteBuffer) {
        fsm.cursor.compact();
        System.err.println("");
        //do something.... crack a payload... then

        // then...


    }
}
