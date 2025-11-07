import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            Main.usage();
            System.err.println("Got: " + Arrays.stream(args).reduce("|", (x,y)->x+y)+"|");
            return;
        }

        switch (args[0]) {
            case "server" -> {
                Server.run();
            }
            case "client" -> {
                Client.run();
            }
            default -> {
                Main.usage();
                System.err.println("Got " + args[0]);
                return;
            }
        }
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("gradle run --args=\"client\"");
        System.err.println("gradle run --args=\"server\"");
    }
}
