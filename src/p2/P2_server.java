import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class P2_server {
    public P2_server(String[] args) {
        int portNumber = Integer.parseInt(args[0]);
        System.out.printf("Server listening port:%d\n", portNumber);

        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
        ) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Server_thread processor = new Server_thread(clientSocket);
                processor.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new P2_server(args);
    }
}