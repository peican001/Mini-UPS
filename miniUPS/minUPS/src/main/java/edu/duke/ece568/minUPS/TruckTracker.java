package edu.duke.ece568.minUPS;

import edu.duke.ece568.minUPS.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;

public class TruckTracker extends Thread {
    private final static Logger LOG =  LoggerFactory.getLogger(TruckTracker.class);
    private HashSet<Integer> trackingSet;
    private WorldService worldService;

    public TruckTracker(HashSet<Integer> trackingSet, WorldService worldService) {
        this.trackingSet = trackingSet;
        this.worldService = worldService;
    }

    @Override
    public void run() {
        LOG.info("Running distance checker ... ");
        while (true) {
            try {
                Thread.sleep(1000);
                if (trackingSet.isEmpty()) {
                    continue;
                }
                for (Integer truckID : trackingSet) {
                    worldService.trackingTruckInfo(truckID);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
