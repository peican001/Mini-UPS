package edu.duke.ece568.minUPS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.duke.ece568.minUPS.service.WorldService;

@Component
public class MyCommandLineRunner implements CommandLineRunner{

    private final UpsServer upsServer;

    //private final WorldService worldService;

    @Autowired
    public MyCommandLineRunner(UpsServer upsServer) {
        this.upsServer = upsServer;
        //this.worldService = worldService;
    }

    @Override
    public void run(String... args) throws Exception {
        // TODO Auto-generated method stub
        upsServer.startServer();
        //worldService.RunTest();
    }
    
}
