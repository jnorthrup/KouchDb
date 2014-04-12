package kouchdb;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;

/**
* Created by jim on 4/12/14.
*/
public class CommandCreation implements CompletionHandler<Integer, Object> {
    private WebSocketFsm fsm;

    public CommandCreation(WebSocketFsm fsm) {
        this.fsm = fsm;
    }

    @Override
    public void completed(Integer result, Object attachment) {
        List<WebSocketFrame> segmented = new ArrayList<WebSocketFrame>();
        WebSocketFrame webSocketFrame = new WebSocketFrame();
        boolean apply = webSocketFrame.apply(fsm.cursor);

        if (apply) {
            if (webSocketFrame.payloadLength > fsm.cursor.remaining()) {
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) (webSocketFrame.payloadLength - fsm.cursor.remaining()));
                fsm.socketChannel.read(byteBuffer, attachment, new CompletionHandler<Integer, Object>() {
                    @Override
                    public void completed(Integer result, Object attachment) {
                        if (!byteBuffer.hasRemaining()) {//spin till full.  then goto top
                            ByteBuffer slice = null;
                            if (webSocketFrame.isMasked) {
                                slice = fsm.cursor.slice();
                                WebSocketFrame.applyMask(webSocketFrame.maskingKey, slice);
                                WebSocketFrame.applyMask(webSocketFrame.maskingKey, byteBuffer);
                            }
                            command(webSocketFrame, slice, byteBuffer);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
            }else
                //(webSocketFrame.payloadLength<=cursor.remaining())
                {

                      command(webSocketFrame,(ByteBuffer) ByteBuffer.allocateDirect((int) webSocketFrame.payloadLength).put(fsm.cursor).rewind());
                      fsm.cursor.compact();
                }
        }

    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
    /**
     * receive a series of command fragments and assemble.  each fragment may have more than one buffer.
     * @param webSocketFrame
     * @param byteBuffer
     */
    void command(WebSocketFrame webSocketFrame, ByteBuffer... byteBuffer) {

        System.err.println("");
        //do something.... crack a payload... then
        //
        // then...

        fsm.socketChannel.read(fsm.cursor,null,fsm.startNewFrameHandler);


    }
}
