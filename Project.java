import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Project {
    public final static void main(String[] argv) {
        NetworkSimulator simulator;

        int nsim = -1;
        double loss = -1;
        double corrupt = -1;
        double delay = -1;
        int trace = -1;
        int seed = -1;
        int windowsize = -1;
        double timeout = -1;
        String buffer = "";
        File outputfile = new File("OutputFile");
        BufferedReader stdIn = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.println("-- * Network Simulator v1.0 * --");

        while (nsim < 1) {
            System.out.print("Enter number of messages to simulate (> 0): " +
                    "[10] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                nsim = 10;
            } else {
                try {
                    nsim = Integer.parseInt(buffer);
                } catch (NumberFormatException nfe) {
                    nsim = -1;
                }
            }
        }

        while (loss < 0) {
            System.out.print("Enter packet loss probability (0.0 for no " +
                    "loss): [0.0] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                loss = 0;
            } else {
                try {
                    loss = (Double.valueOf(buffer)).doubleValue();
                } catch (NumberFormatException nfe) {
                    loss = -1;
                }
            }
        }

        while (corrupt < 0) {
            System.out.print("Enter packet corruption probability (0.0 " +
                    "for no corruption): [0.0] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                corrupt = 0;
            } else {
                try {
                    corrupt = (Double.valueOf(buffer)).doubleValue();
                } catch (NumberFormatException nfe) {
                    corrupt = -1;
                }
            }
        }

        while (delay <= 0) {
            System.out.print("Enter average time between messages from " +
                    "sender's layer 5 (> 0.0): [1000] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                delay = 1000;
            } else {
                try {
                    delay = (Double.valueOf(buffer)).doubleValue();
                } catch (NumberFormatException nfe) {
                    delay = -1;
                }
            }
        }

        while (windowsize < 1) {
            System.out.print("Enter window size (> 0): [8] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                windowsize = 8;
            } else {
                try {
                    windowsize = Integer.parseInt(buffer);
                } catch (NumberFormatException nfe) {
                    windowsize = -1;
                }
            }
        }

        while (timeout <= 0) {
            System.out.print("Enter retransmission timeout (>0.0) [15.0] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                timeout = 15.0;
            } else {
                try {
                    timeout = (Double.valueOf(buffer)).doubleValue();
                } catch (NumberFormatException nfe) {
                    timeout = -1;
                }
            }
        }

        while (trace < 0) {
            System.out.print("Enter trace level (>= 0): [0] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                trace = 0;
            } else {
                try {
                    trace = Integer.parseInt(buffer);
                } catch (NumberFormatException nfe) {
                    trace = -1;
                }
            }
        }

        while (seed < 1) {
            System.out.print("Enter random seed: [0] ");
            try {
                buffer = stdIn.readLine();
            } catch (IOException ioe) {
                System.out.println("IOError reading your input!");
                System.exit(1);
            }

            if (buffer.equals("")) {
                seed = 0;
            } else {
                try {
                    seed = (Integer.valueOf(buffer)).intValue();
                } catch (NumberFormatException nfe) {
                    seed = -1;
                }
            }
        }

        simulator = new GBN(nsim, loss, corrupt, delay,
                trace, seed, windowsize, timeout);

        simulator.runSimulator();

        // statistic
        System.out.println();
        System.out.print("Input 0 to start statistic analysis: [0] ");
        try {
            buffer = stdIn.readLine();
        } catch (IOException ioe) {
            System.out.println("IOError reading your input!");
            System.exit(1);
        }

        if (buffer.equals(""))
            buffer = "0";

        if (!buffer.equals("0"))
            return;

        // 8 ratio, 30 seed, 3 data (tput, gput, delay)
        double[][][] sr_loss = new double[8][30][3];
        double[][][] gbn_loss = new double[8][30][3];
        double[][][] sr_corrupt = new double[8][30][3];
        double[][][] gbn_corrupt = new double[8][30][3];
        // confidence intervals for average delay. sr or gbn, loss or corrupt, 8 ratio, 2 result
        double[][][][] ci = new double[2][2][8][2];

        // different loss ratio and corrupt ratio
        for (double i = 0.0; i < 0.8; i += 0.1) {
            // different seed
            for (int j = 100; j < 130; j++) {
                SR sr_l = new SR(nsim, i, 0, delay, 0, j, windowsize, timeout);
                SR sr_c = new SR(nsim, 0, i, delay, 0, j, windowsize, timeout);
                GBN gbn_l = new GBN(nsim, i, 0, delay, 0, j, windowsize, timeout);
                GBN gbn_c = new GBN(nsim, 0, i, delay, 0, j, windowsize, timeout);

                sr_l.runSimulator();
                sr_c.runSimulator();
                gbn_l.runSimulator();
                gbn_c.runSimulator();

                sr_loss[(int) (i * 10)][j - 100] = sr_l.getStatistics();
                gbn_loss[(int) (i * 10)][j - 100] = gbn_l.getStatistics();
                sr_corrupt[(int) (i * 10)][j - 100] = sr_c.getStatistics();
                gbn_corrupt[(int) (i * 10)][j - 100] = gbn_c.getStatistics();
            }
            // confidence intervals [x − 1.645σ/√n,   x + 1.645σ/√n]
            // sr or gbn, loss or corrupt, 8 ratio, 2 result
            ci[0][0][(int) (i * 10)] = confidenceInterval(sr_loss[(int) (i * 10)]);
            ci[0][1][(int) (i * 10)] = confidenceInterval(sr_corrupt[(int) (i * 10)]);
            ci[1][0][(int) (i * 10)] = confidenceInterval(gbn_loss[(int) (i * 10)]);
            ci[1][1][(int) (i * 10)] = confidenceInterval(gbn_corrupt[(int) (i * 10)]);
        }

        System.out.println("\n-----Statistic Data------\n");
        System.out.println(Arrays.deepToString(sr_loss));
        System.out.println(Arrays.deepToString(gbn_loss));
        System.out.println(Arrays.deepToString(sr_corrupt));
        System.out.println(Arrays.deepToString(gbn_corrupt));
        System.out.println(Arrays.deepToString(ci));
    }

    // confidence intervals for average delay
    private static double[] confidenceInterval(double[][] data) {
        double mean = 0.0, std = 0.0;
        int length = data.length;

        for (double[] num : data) {
            mean += num[2];
        }
        mean = mean / length;

        for (double[] num : data) {
            std += Math.pow(num[2] - mean, 2);
        }
        std = Math.sqrt(std / length);

        return new double[]{mean - 1.645 * std / Math.sqrt(data.length), mean + 1.645 * std / Math.sqrt(data.length)};
    }
}
