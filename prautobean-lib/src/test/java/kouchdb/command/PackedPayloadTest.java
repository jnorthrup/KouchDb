package kouchdb.command;

import kouchdb.io.PackedPayload;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.lang.Integer.toBinaryString;


public class PackedPayloadTest {

    @Test public
    void packTest() {
        final BitSet bitSet = new BitSet(6);
        bitSet.set(0, 6);
        System.err.println(Integer.toBinaryString(bitSet.toByteArray()[0]));
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256 << 10);
        CreateOptions createOptions = new CreateOptions() {
            @Override
            public boolean getAutoCompaction() {
                return true;
            }

            @Override
            public String getName() {
                return "theName";
            }

            @Override
            public String getCache() {
                return "someString";
            }

            @Override
            public String getAdapter() {
                return "someOtherString";
            }

            @Override
            public List<CreateOptions> getChallenge() {
                ArrayList<CreateOptions> createOptionses = new ArrayList<>();
                createOptionses.add(new CreateOptions() {
                    @Override
                    public boolean getAutoCompaction() {
                        return false;
                    }

                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getCache() {
                        return null;
                    }

                    @Override
                    public String getAdapter() {
                        return null;
                    }

                    @Override
                    public List<CreateOptions> getChallenge() {
                        return null;
                    }

                    @Override
                    public List<String> getChallenge2() {
                        return Arrays.asList(new String[]{"c", "d"});
                    }
                });
                return createOptionses;
            }
            @Override
            public List<String> getChallenge2() {
                return Arrays.asList("a", "b");
            }
        };
        PackedPayload<CreateOptions> createOptionsClassPackedPayload = new PackedPayload<>(CreateOptions.class);
        createOptionsClassPackedPayload.put(createOptions, byteBuffer);
        final Buffer flip = byteBuffer.flip();

        System.err.println(StandardCharsets.UTF_8.decode((ByteBuffer) byteBuffer.duplicate()));

        final ByteBuffer duplicate = (ByteBuffer) byteBuffer.duplicate(   );
        duplicate.getInt();//skip an int.
        byte b = duplicate. get( );
        System.err.println( toBinaryString(b & 0xff));

        assert 0b111111 == (b & 0xff); //bitmap

        CreateOptions createOptions1 = createOptionsClassPackedPayload.get(CreateOptions.class, (ByteBuffer) flip);
        final String cache = createOptions.getCache();
        final String cache1 = createOptions1.getCache();
        assert cache1.equals(cache);
    }

}
