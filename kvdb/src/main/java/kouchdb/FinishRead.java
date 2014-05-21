package kouchdb;

import one.xio.AsioVisitor;import rxf.core.Server;

import java.lang.Exception;import java.lang.Override;import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;
import java.util.function.Function;

public class FinishRead extends AsioVisitor.Impl{
 public FinishRead( ByteBuffer cursor,Runnable success) {
        this.cursor = cursor;

     this.success = success;
 }


    ByteBuffer cursor;
    private Runnable success;

    @Override
    public void onRead(SelectionKey key) throws Exception {
        int read = ((ReadableByteChannel) key.channel()).read(cursor);
        if(read==-1)key.cancel();
        if(!cursor.hasRemaining()) success.run();

    }


}

