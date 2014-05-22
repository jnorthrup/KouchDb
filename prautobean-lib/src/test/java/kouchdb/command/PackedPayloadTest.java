package kouchdb.command;

import junit.framework.Assert;
import kouchdb.io.PackedPayload;
import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.lang.Integer.toBinaryString;


public class PackedPayloadTest {

    @Test
    public void packTest() {
        BitSet bitSet = new BitSet(6);
        bitSet.set(0, 6);
        System.err.println(Integer.toBinaryString(bitSet.toByteArray()[0]));
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(256 << 10);
        ComplexPrautoBean complexPrautoBean = new ComplexPrautoBean() {
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
            public List<ComplexPrautoBean> getChallenge() {
                ArrayList<ComplexPrautoBean> createOptionses = new ArrayList<>();
                createOptionses.add(new ComplexPrautoBean() {
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
                    public List<ComplexPrautoBean> getChallenge() {
                        return null;
                    }

                    @Override
                    public List<String> getChallenge2() {
                        return Arrays.asList(new String[]{"c", "d"});
                    }

                    @Override
                    public List<TheEnum> getEnumThingy() {
                        return null;
                    }
                });
                return createOptionses;
            }

            @Override
            public List<String> getChallenge2() {
                return Arrays.asList("a", "b");
            }

            @Override
            public List<TheEnum> getEnumThingy() {
                List<TheEnum> lists =
                        (Arrays.asList(TheEnum.y, TheEnum.y, TheEnum.z));

                return lists;
            }
        };
        PackedPayload<ComplexPrautoBean> createOptionsClassPackedPayload =PackedPayload.create(ComplexPrautoBean.class);
        createOptionsClassPackedPayload.put(complexPrautoBean, byteBuffer);
        ByteBuffer flip = (ByteBuffer) byteBuffer.flip();

        System.err.println(StandardCharsets.UTF_8.decode(byteBuffer.duplicate()));

        ByteBuffer duplicate = (ByteBuffer) byteBuffer.duplicate();
        PackedPayload.readSize(duplicate);
        byte b = duplicate.get();
        System.err.println(toBinaryString(b & 0xff));


        ComplexPrautoBean complexPrautoBean1 =
        PackedPayload.create(ComplexPrautoBean.class).get(ComplexPrautoBean.class, (ByteBuffer) flip);

        TheEnum theEnum = complexPrautoBean1.getEnumThingy().get(2);
        org.junit.Assert.assertEquals( TheEnum.z ,theEnum);
        String cache = complexPrautoBean.getCache();
        String cache1 = complexPrautoBean1.getCache();
        org.junit.Assert.assertEquals(cache1, cache);
    }

}
