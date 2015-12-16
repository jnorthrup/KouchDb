package simplest.possible;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * <pre>
 * some goals:
 * ===
 * - [ ] take a string, a key or an expression, and return bytes
 *
 * cat was the most literal translation of the first goal.
 *
 * highly unsafe
 */
public class Cat implements Consumer<String> {
    public static void main(String... args) {

        if (args.length == 0) {
            String x = String.valueOf(Paths.get("").toAbsolutePath());
            System.err.println(x);
            return;
        }
        new Cat().accept(args[0]);

    }

    public static String saferKey(String arg) {
        return arg.replaceAll("\\.+", "\\.");
    }


    @Override
    public void accept(String s) {
        String saferKey = saferKey(s);
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(saferKey));
            System.out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
