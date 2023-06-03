package edu.duke.ece568.minUPS.service.sender;

import edu.duke.ece568.minUPS.protocol.UPStoWorld.UResponses;
import edu.duke.ece568.minUPS.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;

public class DeliveryCommandSender extends WorldCommandSender{
    private static Logger LOG =  LoggerFactory.getLogger(DeliveryCommandSender.class);
    private long packageID;
    private int desX;
    private int desY;
    public DeliveryCommandSender(long seq, int truckID, WorldService worldService, long packageID, int desX,int desY) {
        super(seq, truckID, worldService);
        this.packageID = packageID;
        this.desX = desX;
        this.desY = desY;
    }
    @Override
    public void setTimerAndTask() {
        resend = new TimerTask() {
            @Override
            public void run() {
                try {
                    LOG.info("Resending Delivery seq=" + seq + ", truckID=" + truckID + ", packageID=" + packageID + ", desX=" + desX + ", dexY=" + desY);
                    worldService.sendUGoDeliver(seq,truckID,packageID,desX,desY);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(resend, TIME_OUT);
    }

    @Override
    public void onReceive() {
        cancelTimer();
    }
}
