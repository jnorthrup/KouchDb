package kouchdb.ann;

import java.lang.annotation.*;

/**
 * Created by jim on 4/23/14.
 */@Retention(RetentionPolicy.RUNTIME)@Documented
   @Target(ElementType.TYPE)

   public @interface ProtoOrigin {
    String value();
}
