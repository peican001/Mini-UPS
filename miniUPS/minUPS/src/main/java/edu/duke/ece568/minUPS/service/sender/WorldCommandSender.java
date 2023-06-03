package edu.duke.ece568.minUPS.service.sender;

import edu.duke.ece568.minUPS.protocol.UPStoWorld.UResponses;
import edu.duke.ece568.minUPS.service.WorldService;

import java.util.Timer;
import java.util.TimerTask;

public abstract class WorldCommandSender {
    protected final Long TIME_OUT = 1000 * 5L;
    protected Timer timer;
    protected TimerTask resend;

    WorldService worldService;
    Long seq;
    Integer truckID;

    WorldCommandSender(long seq, int truckID, WorldService worldService) {
        this.seq = seq;
        this.truckID = truckID;
        this.worldService = worldService;

        timer = new Timer("Resend");
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public abstract void setTimerAndTask();

    public abstract void onReceive();
}