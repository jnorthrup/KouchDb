package kouchdb.command;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jim on 5/5/14.
 */

public class PackedPayloadTest {

    @Test
    void packTest() {
        CreateOptions createOptions = new CreateOptions() {
            @Override
            public boolean getAuto_compaction() {
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
                    public boolean getAuto_compaction() {
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
                        return null;
                    }
                });
                return createOptionses;
            }

            @Override
            public List<String> getChallenge2() {
                return Collections.emptyList();
            }
        };
    }

}
