package edu.duke.ece568.minUPS;

import edu.duke.ece568.minUPS.handler.AmazonHandler;
import edu.duke.ece568.minUPS.handler.WorldHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CyclicBarrier;

@Component
public class UpsServer {
    private final static Logger LOG =  LoggerFactory.getLogger(UpsServer.class);
    ServerSocket serverSocket;
    AmazonHandler amazonHandler;
    WorldHandler worldHandler;
    @Autowired
    public UpsServer(ServerSocket serverSocket, AmazonHandler amazonHandler, WorldHandler worldHandler) {
        this.serverSocket = serverSocket;
        this.amazonHandler = amazonHandler;
        this.worldHandler = worldHandler;
        //startServer();
    }
    public void startServer() {
        try {
            // Create a CyclicBarrier for two threads
            CyclicBarrier barrier = new CyclicBarrier(2);
            amazonHandler.getAmazonService().setWorldService(worldHandler.getWorldService());
            worldHandler.getWorldService().setAmazonService(amazonHandler.getAmazonService());
            amazonHandler.setBarrier(barrier);
            worldHandler.setBarrier(barrier);
            // Create two BarrierTasks with the same CyclicBarrier
            LOG.info("Server is listening on port " + serverSocket.getLocalPort());
            Socket socket = serverSocket.accept();
            amazonHandler.setAmazonStream(socket);
            new Thread(amazonHandler).start();
            new Thread(worldHandler).start();
        } catch (IOException e) {
            LOG.error("Error starting server: " + e.getMessage());
        }
    }

}