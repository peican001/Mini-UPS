package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.ConnectionStream;
import edu.duke.ece568.minUPS.dao.PackageDao;
import edu.duke.ece568.minUPS.dao.TruckDao;
import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.protocol.UPStoAmazon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class AmazonService {
    private static Logger LOG =  LoggerFactory.getLogger(AmazonService.class);
    private WorldService worldService;
    private ConnectionStream amazonStream;
    private TruckDao truckDao;

    private PackageDao packageDao;

    @Autowired
    public AmazonService(TruckDao truckDao, PackageDao packageDao) {
        this.truckDao = truckDao;
        this.packageDao = packageDao;
        this.worldService = null;
        this.amazonStream = null;
    }
    public long receiveWorldId() throws IOException {
        // Read the message from the socket
        AInformWorld worldId = AInformWorld.parseDelimitedFrom(amazonStream.inputStream);
        LOG.info("receive from world " + worldId);

        // Send a response message
        // UReceivedWorld ACKMessage = UReceivedWorld.newBuilder().setWorldid(worldId.getWorldid()).build();
        // ACKMessage.writeDelimitedTo(amazonStream.outputStream);
        // amazonStream.outputStream.flush();
        return worldId.getWorldid();
    }
    public void sendUTruckArrived(int truckID, ArrayList<Long> updatedPackageIDs) {
        new Thread(() -> {
            try {
                //writing
                UACommunication.Builder uaCommunication = UACommunication.newBuilder();
                for(Long packageID :updatedPackageIDs){
                    UTruckArrived.Builder uaTruckArrive = UTruckArrived.newBuilder();
                    uaTruckArrive.setTruckid(truckID).setPackageid(packageID);
                    uaCommunication.addArrived(uaTruckArrive);

                }
                UACommunication responses = uaCommunication.build();
                LOG.info("sending Amazon -------- " + responses + "-----------\n");
                responses.writeDelimitedTo(amazonStream.outputStream);
                amazonStream.outputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void setAmazonStream(Socket socket) throws IOException{
        this.amazonStream = new ConnectionStream(socket);
    }

    public void closeAmazonStream()throws IOException{
        this.amazonStream.close();
    }

    public void setWorldId(long worldID) {
        worldService.setWorldId(worldID);
    }

    public void setWorldService(WorldService worldService){
        this.worldService = worldService;
    }

    public void receiveAUCommunication() throws IOException{
        AUCommunication auCommunication = AUCommunication.parseDelimitedFrom(amazonStream.inputStream);
        LOG.info("\nAmazon send: \nAAAAAAAAAA"+ auCommunication + "\n");
        handleABookTruck(auCommunication);
        handleAStartDeliver(auCommunication);
    }
    private void handleAStartDeliver(AUCommunication auCommunication) {
        LOG.info("start new thread for Delivering...");
        new Thread(() -> {
            try {
                LOG.info("start handle AStartDeliver:"+ " num " + auCommunication.getDeliversCount() );
                for (int i = 0; i < auCommunication.getDeliversCount(); i++) {
                    AStartDeliver aStartDeliver = auCommunication.getDelivers(i);
                    LOG.info("Amazon: start deliver packageID = " + aStartDeliver.getPackageid());
                    Optional<Package> packageOptional = packageDao.findById(aStartDeliver.getPackageid());
                    if(packageOptional.isEmpty()){
                        LOG.error("This is a new package!");
                        continue;
                    }
                    Package pack = packageOptional.get();
                    worldService.goDeliver(pack.getTruck().getTruckID(),pack.getPackageID(),pack.getDestinationX(),pack.getDestinationY());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

//    message ABookTruck {
//        required int64 packageid = 1;
//        required int32 warehouseid = 2;
//        required int32 warehousex = 3;
//        required int32 warehousey = 4;
//        required int32 destinationx = 5;
//        required int32 destinationy = 6;
//        optional string upsid = 7;
//        optional string detail = 9;
//
//    }
    private void handleABookTruck(AUCommunication auCommunication) {
        LOG.info("start new thread for booking...");
        new Thread(() -> {
            try {
                LOG.info("start handle ABookTruck:"+ " num " + auCommunication.getBookingsCount());
                for(int i = 0; i < auCommunication.getBookingsCount(); i++){
                    ABookTruck aBookTruck = auCommunication.getBookings(i);
                    LOG.info("--- BOOK Truck" + "\npackageID:" + aBookTruck.getPackageid()
                            + "\nwarehouseID: " + aBookTruck.getWarehouseid()
                            + "\nwarehouseX: " + aBookTruck.getWarehousex()
                            + "\nwarehouseY: " + aBookTruck.getWarehousey()
                            + "\ndestinationX: " + aBookTruck.getDestinationx()
                            + "\ndestinationY: " + aBookTruck.getDestinationy()
                            + "\nupsID: " + aBookTruck.getUpsid()
                            + "\ndetail: " + aBookTruck.getDetail()
                    );
                    //database operation:
                    Package pack = new Package();
                    pack.setPackageID(aBookTruck.getPackageid());
                    pack.setWarehouseID(aBookTruck.getWarehouseid());
                    pack.setDestinationX(aBookTruck.getDestinationx());
                    pack.setDestinationY(aBookTruck.getDestinationy());
                    pack.setUpsID(aBookTruck.getUpsid());
                    pack.setDetails(aBookTruck.getDetail());
                    pack.setStatus(Package.Status.CREATED.getText());
                    createPackage(pack);
                    int truckID = worldService.findAvailableTrucks();
                    LOG.info("---find truck " + truckID + " to deliver the package");
                    packageDao.updateTruckID(aBookTruck.getPackageid(),truckID);
                    worldService.trackingSet.add(truckID);
                    worldService.pickup(truckID,aBookTruck.getWarehouseid(),pack.getPackageID());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void createPackage(Package pack) {
        packageDao.save(pack);
    }
//    message UDelivered{
//        required int64 packageid = 1;
//        optional int64 seqnum = 2;
//    }
    public void sendUDelivered(long packageID) {
        new Thread(() -> {
            try {
                UACommunication.Builder uaCommunication = UACommunication.newBuilder();
                UDelivered.Builder uDeliveredB = UDelivered.newBuilder();
                uDeliveredB.setPackageid(packageID);
                uaCommunication.addDelivered(uDeliveredB.build());
                UACommunication responses = uaCommunication.build();

                LOG.info("sending Amazon -------- " + responses + "-----------\n");
                responses.writeDelimitedTo(amazonStream.outputStream);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
