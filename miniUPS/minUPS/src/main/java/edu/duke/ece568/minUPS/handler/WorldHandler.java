package edu.duke.ece568.minUPS.handler;

import edu.duke.ece568.minUPS.TruckTracker;
import edu.duke.ece568.minUPS.service.WorldService;
import net.bytebuddy.asm.Advice.Exit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

@Component
public class WorldHandler implements Runnable {
    private final static Logger LOG =  LoggerFactory.getLogger(WorldHandler.class);
    private CyclicBarrier barrier;

    private final WorldService worldService;

    @Autowired
    public WorldHandler(WorldService worldService) {
        this.worldService = worldService;
    }

    @Override
    public void run() {
        LOG.info("World handler running...");
        try {
            barrier.await();
            connectWorld();
            receiveConnectedFromWorld();
            startListeningToWorld();
            startTruckTracker(worldService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectWorld() throws IOException{
        worldService.connectWorld();
    }


    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }



    private void receiveConnectedFromWorld() throws IOException {
       worldService.receiveConnectedFromWorld();
    }

    private void startListeningToWorld() {
        new Thread(() -> {
            while (true) {
                try {
                    receiveUResponses();
                } catch (IOException e) {
                    LOG.error("world listener error:" + e.getMessage());
                    System.exit(1);
                }
            }
        }).start();
    }
    private void startTruckTracker(WorldService worldService){
        TruckTracker tt= new TruckTracker(worldService.getTrackingSet(),worldService);
        tt.start();
    }

    private void receiveUResponses() throws IOException {
        worldService.receiveUResponses();
    }

    public WorldService getWorldService() {
        return worldService;
    }
}
