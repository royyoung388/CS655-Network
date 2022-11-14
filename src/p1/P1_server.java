import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class P1_server {
    public static void main(String[] args) {
        int portNumber = Integer.parseInt(args[0]);
        System.out.printf("Server listening port:%d\n", portNumber);

        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String msg;

            while ((msg = in.readLine()) != null) {
                System.out.println("Client: " + msg);
                out.println(msg);
                out.flush();
                if (msg.equals("quit"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
