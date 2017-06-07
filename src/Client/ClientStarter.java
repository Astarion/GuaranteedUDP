package Client;

/**
 * Created by Alex on 27.05.2017.
 */
public class ClientStarter {

    public static void main(String[] args) {
        Client client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], Integer.parseInt(args[4]));
        Runtime.getRuntime().addShutdownHook(new Thread(client::stop));
    }
}
