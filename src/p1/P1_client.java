import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class P1_client {
    public static void main(String[] args) {
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        System.out.printf("Server address: %s:%d\n", hostName, portNumber);

        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in);
        ) {
            String server = "", client;

            while (!server.equals("quit")) {
                client = scanner.nextLine();
                if (client == null)
                    continue;

                out.println(client);
                out.flush();

                if ((server = in.readLine()) != null) {
                    System.out.println("Server: " + server);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
