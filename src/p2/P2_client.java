import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class P2_client {
    public P2_client(String[] args) {
        // hostname port measurement_type(rtt or tput) probes_number msg_size server_delay(in ms)
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        System.out.printf("Server address: %s:%d\n", hostName, portNumber);

        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            // CSP
            // <PROTOCOL PHASE><WS><MEASUREMENT TYPE><WS><NUMBER OF
            //PROBES><WS><MESSAGE SIZE><WS><SERVER DELAY>\n
            String csp = String.format("s %s %s %s %s", args[2], args[3], args[4], args[5]);

            out.println(csp);
            out.flush();

            String response = in.readLine();
            System.out.println("server: " + response);

            int[] timespan = new int[Integer.parseInt(args[3])];
            // MP
            for (int i = 1; i <= Integer.parseInt(args[3]); i++) {
                byte[] bytes = new byte[Integer.parseInt(args[4])];
//                new Random().nextBytes(bytes);
                Arrays.fill(bytes, (byte) 96);
                String payload = new String(bytes);
                // <PROTOCOL PHASE><WS><PROBE SEQUENCE NUMBER><WS><PAYLOAD>\n
                String msg = String.format("m %d %s", i, payload);
                long start = System.currentTimeMillis();
                out.println(msg);
                out.flush();
                response = in.readLine();
                long end = System.currentTimeMillis();

                if (!response.equals(msg)) {
                    // error
                    in.close();
                    out.close();
                    socket.close();
                    return;
                }

                timespan[i - 1] = (int) (end - start);
            }

            // CTP
            out.println("t");

            response = in.readLine();
            System.out.println("server: " + response);
            if (!response.contains("200 OK")) {
                // error
                in.close();
                out.close();
                socket.close();
                return;
            }

            // calculate time
            if (args[2].equals("rtt")) {
                int sum = 0;
                for (int t : timespan)
                    sum += t;
                float rtt = (float) sum / timespan.length;
                System.out.printf("rtt: %.2f ms\n", rtt);
            } else if (args[2].equals("tput")) {
                float tput = 0;
                int bits = Integer.parseInt(args[4]) * 8;
                for (int t : timespan)
                    tput += bits / (float) t;
                tput = tput / timespan.length;
                System.out.printf("tput: %.2f Kbps\n", tput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new P2_client(args);
    }
}
