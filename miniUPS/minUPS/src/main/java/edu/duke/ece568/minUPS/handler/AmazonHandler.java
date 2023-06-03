package edu.duke.ece568.minUPS.handler;

import edu.duke.ece568.minUPS.service.AmazonService;
import edu.duke.ece568.minUPS.service.WorldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

@Component
public class AmazonHandler implements Runnable {
    private static final Logger LOG =  LoggerFactory.getLogger(AmazonHandler.class);
    private final AmazonService amazonService;

    private CyclicBarrier barrier;

    @Autowired
    public AmazonHandler(AmazonService amazonService) {
        this.amazonService = amazonService;
    }

    @Override
    public void run() {
        LOG.info("Amazon handler thread running...");
        try  {
            long worldID = amazonService.receiveWorldId();
            amazonService.setWorldId(worldID);
            barrier.await();
            StartListeningToAmazon();
        } catch (Exception e){
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void StartListeningToAmazon() {
        new Thread(() -> {
            while (true) {
                try {
                    receiveAUCommunication();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }).start();
    }

    private void receiveAUCommunication() throws IOException{
        amazonService.receiveAUCommunication();
    }

    public void setBarrier(CyclicBarrier barrier){
        this.barrier = barrier;
    }

    public void setAmazonStream(Socket socket) throws IOException{
        amazonService.setAmazonStream(socket);
    }

    public AmazonService getAmazonService() {
        return amazonService;
    }
}