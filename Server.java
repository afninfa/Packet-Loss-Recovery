import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static Runnable makeReceiverRoutine() {
        // This map doesn't need to be concurrent, we only have one listener thread
        // Yes, virtual threads means you may have many different physical threads context switch
        // in and out of this one virtual thread, but semantics are preserved
        Map<String, Set<Integer>> ackedSeqNumbersPerClient = new HashMap<>();
        return () -> {
            
        };
    }

    public static void main(String[] args) {
        var receiver = Thread.startVirtualThread(Server.makeReceiverRoutine());
        try {
            receiver.join();
        } catch (Exception e) {
            System.err.println("Failed while joining listener " + e.getMessage());
        }
    }
}
