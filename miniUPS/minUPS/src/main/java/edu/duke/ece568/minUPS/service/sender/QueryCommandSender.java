package edu.duke.ece568.minUPS.service.sender;

import edu.duke.ece568.minUPS.protocol.UPStoWorld.UResponses;
import edu.duke.ece568.minUPS.service.WorldService;

public class QueryCommandSender extends WorldCommandSender {

    public QueryCommandSender(long seq, int truckID, WorldService worldService) {
        super(seq, truckID, worldService);
    }

    @Override
    public void setTimerAndTask() {
    }

    @Override
    public void onReceive(){
    }
}
