import java.util.Arrays;

public class Packet {
    private int seqnum;
    private int acknum;
    private int[] sack;
    private int checksum;
    private String payload;

    public Packet(Packet p) {
        seqnum = p.getSeqnum();
        acknum = p.getAcknum();
        sack = p.getSack();
        checksum = p.getChecksum();
        payload = new String(p.getPayload());
    }

    public Packet(int seq, int ack, int check, String newPayload) {
        seqnum = seq;
        acknum = ack;
        sack = new int[0];
        checksum = check;
        if (newPayload == null) {
            payload = "";
        } else if (newPayload.length() > NetworkSimulator.MAXDATASIZE) {
            payload = null;
        } else {
            payload = new String(newPayload);
        }
    }

    public Packet(int seq, int ack, int[] sack, int check, String newPayload) {
        this(seq, ack, check, newPayload);
        this.sack = sack;
    }

    public Packet(int seq, int ack, int check) {
        seqnum = seq;
        acknum = ack;
        sack = new int[0];
        checksum = check;
        payload = "";
    }

    public boolean setSeqnum(int n) {
        seqnum = n;
        return true;
    }

    public boolean setAcknum(int n) {
        acknum = n;
        return true;
    }

    public boolean setSack(int[] sack) {
        this.sack = sack;
        return true;
    }

    public boolean setChecksum(int n) {
        checksum = n;
        return true;
    }

    public boolean setPayload(String newPayload) {
        if (newPayload == null) {
            payload = "";
            return false;
        } else if (newPayload.length() > NetworkSimulator.MAXDATASIZE) {
            payload = "";
            return false;
        } else {
            payload = new String(newPayload);
            return true;
        }
    }

    public int getSeqnum() {
        return seqnum;
    }

    public int getAcknum() {
        return acknum;
    }

    public int[] getSack() {
        return sack;
    }

    public int getChecksum() {
        return checksum;
    }

    public String getPayload() {
        return payload;
    }

    public String toString() {
        return ("seqnum: " + seqnum + "  acknum: " + acknum + "  sack:  " + Arrays.toString(sack) +
                "  checksum: " + checksum + "  payload: " + payload);
    }

}
