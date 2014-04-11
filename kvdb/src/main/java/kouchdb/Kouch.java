package kouchdb;

/**
 * Created by jim on 4/11/14.
 */
public class Kouch {

    public static void main(String[] args) {
        try (Server server = new Server()) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (server){
                server.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
