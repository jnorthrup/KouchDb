package kouchdb.ann;

import java.lang.annotation.*;

/**
 * Created by jim on 4/23/14.
 */@Retention(RetentionPolicy.RUNTIME)@Documented
   @Target(ElementType.METHOD)

   public @interface ProtoNumber {
    int value();

}
