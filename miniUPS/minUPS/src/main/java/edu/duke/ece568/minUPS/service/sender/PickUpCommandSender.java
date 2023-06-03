package edu.duke.ece568.minUPS.service.sender;

import edu.duke.ece568.minUPS.protocol.UPStoWorld.UResponses;
import edu.duke.ece568.minUPS.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;

public class PickUpCommandSender extends WorldCommandSender{
    private static Logger LOG =  LoggerFactory.getLogger(PickUpCommandSender.class);
    private int warehouseID;
    private long packageID;
    public PickUpCommandSender(long seq, int truckID, WorldService worldService, int warehouseID,long packageID) {
        super(seq, truckID, worldService);
        this.warehouseID = warehouseID;
        this.packageID = packageID;
    }
    @Override
    public void setTimerAndTask() {
        resend = new TimerTask() {
            @Override
            public void run() {
                try {
                    LOG.info("Resending pick up seq=" + seq + ", truckID=" + truckID + ", warehouseID=" + warehouseID + ", packageID=" + packageID);
                    worldService.sendUGoPickUp(seq,truckID,warehouseID,packageID);
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
