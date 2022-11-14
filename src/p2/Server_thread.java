import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Server_thread extends Thread {
    private Socket socket;

    public Server_thread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            //CSP
            String[] csp = in.readLine().split(" ");
            if (check(csp)) {
                out.println("200 OK: Ready");
                out.flush();
            } else {
                // invalid message
                out.println("404 ERROR: Invalid Connection Setup Message");
                out.flush();
                in.close();
                out.close();
                socket.close();
                return;
            }

            // MP
            for (int i = 1; i <= Integer.parseInt(csp[2]); i++) {
                String probe = in.readLine();
                String[] msg = probe.split(" ");
                if (check(msg) && Integer.parseInt(msg[1]) == i) {
                    sleep(Integer.parseInt(csp[4]));
                    out.println(probe);
                    out.flush();
                } else {
                    // invalid message
                    out.println("404 ERROR: Invalid Measurement Message");
                    out.flush();
                    in.close();
                    out.close();
                    socket.close();
                    return;
                }
            }

            // CTP
            String[] msg = in.readLine().split(" ");
            if (check(msg) && msg.length == 1) {
                out.println("200 OK: Closing Connection");
                out.flush();
            } else {
                // invalid message
                out.println("404 ERROR: Invalid Connection Termination Message");
                out.flush();
                in.close();
                out.close();
                socket.close();
                return;
            }

            in.close();
            out.close();
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // check is the message valid
    private boolean check(String[] msg) {
        if (msg.length < 1) {
            System.out.println("message length is not valid");
        }

        switch (msg[0]) {
            case "s":
                // Connection Setup Phase
                if (msg.length != 5) {
                    System.out.println("Invalid connection setup protocol");
                    return false;
                }
                break;
            case "m":
                // Measurement Phase
                if (msg.length != 3) {
                    System.out.println("Invalid measurement protocol");
                    return false;
                }
                break;
            case "t":
                // terminate
                if (msg.length != 1) {
                    System.out.println("Invalid termination protocol");
                    return false;
                }
                break;
            default:
                System.out.println("unknown protocol");
                return false;
        }
        return true;
    }
}