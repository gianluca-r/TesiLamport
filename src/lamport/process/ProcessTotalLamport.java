package lamport.process;

import lamport.payload.TimestampedIDPayload;
import lamport.payload.TimestampedPayload;
import lamport.payload.TimestampedPayload;

import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessTotalLamport extends Process<TimestampedIDPayload> {

    public ProcessTotalLamport(int port) {
        super(port);
        SetTimestamp(0,port);
    }

    private int[] timestamp={0,0}; //TODO: classe apposita per timestamp per evitare ridondanza
    private Lock lockTimestamp=new ReentrantLock(); //l'accesso a timestamp deve essere gestito da un lock unico per evitare conflitti di concorrenza

    public int[] GetTimestamp() { return timestamp; }
    public void SetTimestamp(int[] t) { timestamp = t; }
    public void SetTimestamp(int time) { timestamp[0] = time; }
    public void SetTimestamp(int time, int id) { timestamp[0] = time; timestamp[1] = id; }

    @Override
    void OutputHandler() {
        while(true) {
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 3000));
            } catch (Exception e) { }

            lockTimestamp.lock();

            SetTimestamp(GetTimestamp()[0] + 1);

            TimestampedIDPayload payload = new TimestampedIDPayload();
            payload.SetTimestamp(GetTimestamp());

            Send(GetRandomOutSocket(), payload);

            lockTimestamp.unlock();
        }
    }

    @Override
    void PayloadReceivedHandler(Socket s, TimestampedIDPayload payload) {

        lockTimestamp.lock();

        int[] t=payload.GetTimestamp();
        int newT=Math.max(t[0],GetTimestamp()[0])+1;
        Log("Received packet with timestamp ("+payload.GetTimestamp()[0]+","+payload.GetTimestamp()[1]+"), current timestamp is ("+GetTimestamp()[0]+","+GetTimestamp()[1]+").");
        Log("New timestamp is ("+newT+","+GetTimestamp()[1]+").");
        SetTimestamp(newT);

        lockTimestamp.unlock();

    }

}