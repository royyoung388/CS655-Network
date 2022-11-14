import java.util.ArrayList;
import java.util.List;

public class SR extends NetworkSimulator {
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity):
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment):
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData):
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    private int bufferSize;

    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // total run time
    private double startTime, endTime;

    // get statistics
    public double[] getStatistics() {
        // throughput, goodput, average delay
        return new double[]{
                (aTrans + aRetrans) * (MAXDATASIZE * 16 + 8 * 8) / (endTime - startTime),
                (bAbove) * (MAXDATASIZE * 16 + 8 * 8) / (endTime - startTime),
                avgCom
        };
    }

    // calculate checksum
    private int checkSum(Packet packet) {
        int result = packet.getSeqnum() + packet.getAcknum();
        for (byte b : packet.getPayload().getBytes()) {
            result += b;
        }
        return ~result;
    }

    // validate checksum
    private boolean validCheckSum(Packet packet) {
        return ~checkSum(packet) + packet.getChecksum() == -1;
    }

    // This is the constructor.  Don't touch!
    public SR(int numMessages,
              double loss,
              double corrupt,
              double avgDelay,
              int trace,
              int seed,
              int winsize,
              double delay) {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
        WindowSize = winsize;
        LimitSeqNo = winsize * 2; // set appropriately; assumes SR here!
        RxmtInterval = delay;
        bufferSize = numMessages;
    }


    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message) {
        // receive message from above
        Packet packet = new Packet(aNextSeq, -1, 0, message.getData());
        packet.setChecksum(checkSum(packet));
        aBuffer.add(new Packet(packet));
        sendTime.add(getTime());
        firstACKTime.add(0.0);
        ackTime.add(0.0);

        aNextSeq += 1;
        if (aNextSeq == LimitSeqNo)
            aNextSeq = FirstSeqNo;

        // send packet
        aSendPacket(aLPS + 1);
    }

    // send packet with index
    private void aSendPacket(int index) {
        // window full, no action
        if (aLPS - aLAR > WindowSize)
            return;

        if (index > aLPS && aLPS < aBuffer.size() - 1) {
            aTrans += 1;
            // window expand, send data
            aLPS += 1;
            toLayer3(0, aBuffer.get(aLPS));
            // restart timer
            stopTimer(0);
            startTimer(0, RxmtInterval);
        } else if (index <= aLPS) {
            System.out.println("retransmit A");
            aRetrans += 1;
            // retransmit
            toLayer3(0, aBuffer.get(index));
            // restart timer
            stopTimer(0);
            startTimer(0, RxmtInterval);
        }
    }

    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet) {
        System.out.println("fromLayer3: " + packet);

        // corrupted ack, ignore
        if (!validCheckSum(packet)) {
            corrupt += 1;
            return;
        }

        // find the index of packet
        int ack = packet.getAcknum();
        int firstACK = aLAR == -1 ? -1 : aBuffer.get(aLAR).getAcknum();
//        System.out.printf("%d, %d\n", firstACK, ack);

        if (ack < firstACK) {
            ack += LimitSeqNo;
        }
        int index = ack - firstACK + aLAR;
//        System.out.printf("%d, %d\n", aLAR, index);

//        // out of window packet, ignore
//        if (index < aLAR || index > aLPS)
//            return;

        Packet ackPacket = aBuffer.get(index);
        ackTime.set(index, getTime());

        System.out.printf("aLAR: %d, aLPS: %d, index: %d, ackPacket: %s\n", aLAR, aLPS, index, ackPacket);

        // new ack
        if (ackPacket.getAcknum() == -1) {
            firstACKTime.set(index, getTime());
            ackPacket.setAcknum(packet.getAcknum());
            // slide window
            aLAR = index;
            // send next packets
            for (int i = aLPS + 1; i <= aLAR + WindowSize; i++)
                aSendPacket(i);
        } else {
            // duplicate ack, retransmit
            aSendPacket(index + 1);
        }

        // stop timer
        if (aLAR == aLPS)
            stopTimer(0);
    }

    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    protected void aTimerInterrupt() {
        // retransmit all packet which ackNo != seqNo
        for (int i = aLAR; i <= aLPS; i++) {
            if (i == -1)
                continue;
//            Packet packet = aBuffer.get(i);
//            if (packet.getSeqnum() != packet.getAcknum())
            aSendPacket(i);
        }
    }

    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    private List<Packet> aBuffer;
    // last acknowledgment received (LAR)
    private int aLAR;
    // last packet sent (LPS)
    private int aLPS;
    // next seq no.
    private int aNextSeq;
    // statistic
    private int aTrans, aRetrans, corrupt;
    private List<Double> sendTime, firstACKTime, ackTime;
    private double avgRTT, avgCom;

    protected void aInit() {
        aBuffer = new ArrayList<>(bufferSize);
        aLAR = aLPS = FirstSeqNo - 1;
        aNextSeq = FirstSeqNo;
        sendTime = new ArrayList<>();
        firstACKTime = new ArrayList<>();
        ackTime = new ArrayList<>();
        startTime = getTime();
    }

    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    protected void bInput(Packet packet) {
        System.out.println("fromLayer3: " + packet);
        // corrupted packet, retransmit
        if (!validCheckSum(packet)) {
            corrupt += 1;
            // unknown ack no.
            if (bNPE == 0)
                return;
            Packet cumulate = new Packet(0, (bNPE - 1) % LimitSeqNo, 0);
            cumulate.setChecksum(checkSum(cumulate));
            toLayer3(1, cumulate);
            return;
        }

        bACK += 1;
        int expectSeq = bNPE % LimitSeqNo;
        int seq = packet.getSeqnum();
//        System.out.printf("%d, %d\n", firstSeq, seq);
        if (seq < expectSeq - 1) {
            seq += LimitSeqNo;
        }
        int index = seq - expectSeq + bNPE;
//        System.out.printf("%d, %d\n", bLPA, index);

        System.out.printf("bNPE: %d, index: %d, firstSeq: %d\n", bNPE, index, expectSeq);

        // out of window, discard
        if (index - bNPE >= WindowSize) {
            return;
        }

        // new data
        if (bBuffer[index] == null) {
            bBuffer[index] = packet;
        }

        // in-order
        if (index == bNPE) {
            do {
                // send above
                toLayer5(bBuffer[bNPE].getPayload());
                // slide window
                bNPE += 1;
                bAbove += 1;
            } while (bNPE < bufferSize && bBuffer[bNPE] != null);
        }

//        System.out.printf("%d, %d\n", bLPA, index);
//        System.out.printf("%d, %d\n", bNPE, aBuffer.size());

        // out-of-order, send ack
        // duplicate, send ack
        if (bNPE != 0) {
            Packet cumulate = new Packet(0, (bNPE - 1) % LimitSeqNo, 0);
            cumulate.setChecksum(checkSum(cumulate));
            toLayer3(1, cumulate);
        }
    }

    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    private Packet[] bBuffer;
    // next packet expected (NPE)
    private int bNPE;
    // statistic
    private int bAbove, bACK;

    protected void bInit() {
        bBuffer = new Packet[bufferSize];
        bNPE = 0;
    }

    // Use to print final statistics
    protected void Simulation_done() {
        endTime = getTime();

        int count = 0;
        for (int i = 0; i < sendTime.size(); i++)
            if (firstACKTime.get(i) > 0) {
                avgRTT += firstACKTime.get(i) - sendTime.get(i);
                count += 1;
            }
        if (count != 0)
            avgRTT /= count;

        count = 0;
        for (int i = 0; i < sendTime.size(); i++)
            if (ackTime.get(i) > 0) {
                avgCom += ackTime.get(i) - sendTime.get(i);
                count += 1;
            }
        if (count != 0)
            avgCom /= count;

        // TO PRINT THE STATISTICS, FILL IN THE DETAILS BY PUTTING VARIBALE NAMES. DO NOT CHANGE THE FORMAT OF PRINTED OUTPUT
        System.out.println("\n\n===============STATISTICS=======================");
        System.out.println("Number of original packets transmitted by A:" + aTrans);
        System.out.println("Number of retransmissions by A:" + aRetrans);
        System.out.println("Number of data packets delivered to layer 5 at B:" + bAbove);
        System.out.println("Number of ACK packets sent by B:" + bACK);
        System.out.println("Number of corrupted packets:" + corrupt);
        System.out.println("Ratio of lost packets:" + (double) (aRetrans - corrupt) / ((aTrans + aRetrans) + bACK));
        System.out.println("Ratio of corrupted packets:" + (double) corrupt / ((aTrans + aRetrans) + bACK - (aRetrans - corrupt)));
        System.out.println("Average RTT:" + avgRTT);
        System.out.println("Average communication time:" + avgCom);
        System.out.println("==================================================");

        // PRINT YOUR OWN STATISTIC HERE TO CHECK THE CORRECTNESS OF YOUR PROGRAM
        System.out.println("\nEXTRA:");
        // EXAMPLE GIVEN BELOW
        //System.out.println("Example statistic you want to check e.g. number of ACK packets received by A :" + "<YourVariableHere>");
        System.out.println("Total run time:" + (endTime - startTime));
        System.out.println("Number of data packet send to layer 3:" + getnToLayer3());
        System.out.println("Number of data packet lost:" + getnLost());
        System.out.println("Number of data packet corrupted:" + getnCorrupt());
    }
}
