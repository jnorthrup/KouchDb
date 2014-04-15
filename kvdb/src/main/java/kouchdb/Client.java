package kouchdb;

import com.dyuproject.protostuff.parser.Message;
import com.dyuproject.protostuff.parser.Proto;
import com.dyuproject.protostuff.parser.ProtoUtil;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by jim on 4/14/14.
 */
public class Client {

    public static final Pattern COMPILE = Pattern.compile("[/]{2}.*$");

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/home/jim/work/KouchDb/kvdb/src/main/proto/Commands.proto");

        try {
            Proto target = new Proto();
            ProtoUtil.loadFrom(Files.newBufferedReader(path), target);

            Collection<Message> messages = target.getMessages();

            messages.forEach(o -> System.err.println( o.getFullName()));
            for (Message message : messages) {
                descend(message);
            }
         } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void descend(Message message) {
        LinkedHashMap<String, Message> nestedMessageMap = message.getNestedMessageMap();
        for (Map.Entry<String, Message> stringMessageEntry : nestedMessageMap.entrySet()) {
             descend(stringMessageEntry.getValue());
        }

        writeGen(message);


        String name = message.getName();
        System.err.println(""+ name);

    }

    private static void writeGen(Message message) {
        System.out.println("public enum " +message.getName()+ " {");
        ArrayList<String> objects = new ArrayList<>();
        message.getFields().forEach(o -> objects.add(o.getName()+"(" +o.getJavaType()+
                ")"));
        System.out.println(Joiner.on(',').join(objects));
        System.out.println("}");
    }

}

