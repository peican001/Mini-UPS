package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.ConnectionStream;
import edu.duke.ece568.minUPS.dao.PackageDao;
import edu.duke.ece568.minUPS.dao.TruckDao;
import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.entity.Truck;
import edu.duke.ece568.minUPS.protocol.UPStoWorld.*;
import edu.duke.ece568.minUPS.service.sender.DeliveryCommandSender;
import edu.duke.ece568.minUPS.service.sender.PickUpCommandSender;
import edu.duke.ece568.minUPS.service.sender.QueryCommandSender;
import edu.duke.ece568.minUPS.service.sender.WorldCommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

@Service
public class WorldService {
    private static Logger LOG =  LoggerFactory.getLogger(WorldService.class);
    private final ConnectionStream worldStream;
    private TruckDao truckDao;
    private PackageDao packageDao;
    private AmazonService amazonService;
    private UserService userService;
    private EmailService emailService;
    private long worldId;
    final int TRUCK_CNT = 1000;
    final int TRUCK_X = 10;
    final int TRUCK_Y = 10;
    private AtomicInteger truck_alloc = new AtomicInteger(0);
    private AtomicLong seq = new AtomicLong(0);
    private HashSet<Long> ackSet;
    HashSet<Integer> trackingSet;

    HashMap<Long, WorldCommandSender> seqSenderMap;

    public void closeWorldStream()throws IOException {
        worldStream.close();
    }
    public void setWorldId(long worldId) {
        this.worldId = worldId;
    }

    public HashSet<Integer> getTrackingSet() {
        return trackingSet;
    }

    @Autowired
    public WorldService(Socket worldSocket,PackageDao packageDao,TruckDao truckDao,EmailService emailService, UserService userService) throws IOException {
        this.worldStream = new ConnectionStream(worldSocket);
        this.truckDao = truckDao;
        this.packageDao = packageDao;
        this.worldId = -1;
        this.amazonService = null;
        this.emailService = emailService;
        this.userService = userService;
        this.trackingSet = new HashSet<>();
        this.seqSenderMap = new HashMap<>();
        this.ackSet = new HashSet<>();
    }


    public void setAmazonService(AmazonService amazonService){
        this.amazonService = amazonService;
    }

    public void connectWorld() throws IOException {
        UConnect.Builder uConnectBuilder = UConnect.newBuilder();
        uConnectBuilder.setWorldid(worldId);
        LOG.info("Connect to world:" + worldId);
        truckDao.deleteAll();
        for (int i = 0; i < TRUCK_CNT; ++i) {
            UInitTruck.Builder uInitBuilder = UInitTruck.newBuilder();
            uInitBuilder.setId(i).setX(TRUCK_X).setY(TRUCK_Y);
            uConnectBuilder.addTrucks(uInitBuilder.build());
            Truck truck = new Truck();
            truck.setPosX(TRUCK_X);
            truck.setPosY(TRUCK_Y);
            truck.setStatus(Truck.Status.IDLE.getText());
            truck.setTruckID(i);
            truckDao.save(truck);
        }
        uConnectBuilder.setIsAmazon(false);
        UConnect request = uConnectBuilder.build();
        //LOG.info("sending world -------- " + request + "-----------\n");
        request.writeDelimitedTo(worldStream.outputStream);
    }

    public void receiveConnectedFromWorld() throws IOException {
        UConnected uConnected = UConnected.parseDelimitedFrom(worldStream.inputStream);
        LOG.info("receive world -------- " + uConnected + "-----------\n");
        String result = uConnected.getResult();
        if (!result.equalsIgnoreCase("connected!")) {
            LOG.error("World creating connection error:\n" + result);
            worldStream.close();
            System.exit(1);
        }
    }

    public void receiveUResponses() throws IOException {
        UResponses uResponses = UResponses.parseDelimitedFrom(worldStream.inputStream);
        //LOG.info("\nReceived a UResponse:\n len of Acks=" + uResponses.getAcksCount() + " uf=" + uResponses.getCompletionsCount()
        //        + " truckStatus=" + uResponses.getTruckstatusCount() + " delivered=" + uResponses.getDeliveredCount() + " err=" + uResponses.getErrorCount());
        LOG.info("receive world -------- " + uResponses.getCompletionsList().toString() +"\n" + uResponses.getDeliveredList().toString() + "\n" + uResponses.getErrorList().toString() + "-----------\n");
        handleUFinished(uResponses);
        handleUTruck(uResponses);
        handleUDeliveryMade(uResponses);
        handleAcks(uResponses);
        handleUErr(uResponses);
        if(uResponses.getFinished()){
            LOG.info("World disconnect!!!");
        }

    }

    private void handleUErr(UResponses uResponses) throws IOException{
        for (int i = 0; i < uResponses.getErrorCount(); ++i) {
            UErr uErr= uResponses.getError(i);
            sendAck(uErr.getSeqnum());
            if(ackSet.contains(uErr.getSeqnum())){
                continue;
            }else {
                ackSet.add(uErr.getSeqnum());
            }
            LOG.warn("\nReceived  UError:\n"+ uErr.getErr() + "\nOriginal seq:"+ uErr.getOriginseqnum());
        }
    }
//    message UDeliveryMade{
//        required int32 truckid = 1;
//        required int64 packageid = 2;
//        required int64 seqnum = 3;
//    }
    private void handleUDeliveryMade(UResponses uResponses) throws IOException{
        new Thread(() -> {
            try {
                for (int i = 0; i < uResponses.getDeliveredCount(); ++i) {
                    UDeliveryMade uDeliveryMade = uResponses.getDelivered(i);
                    sendAck(uDeliveryMade.getSeqnum());
                    if(ackSet.contains(uDeliveryMade.getSeqnum())){
                        continue;
                    }else {
                        ackSet.add(uDeliveryMade.getSeqnum());
                    }
                    System.out.println("--- Package " + uDeliveryMade.getPackageid() + " of truck " + uDeliveryMade.getTruckid() + " delivered");

                    // database: Package delivered
                    packageDao.updateStatus(uDeliveryMade.getPackageid(), Package.Status.DELIVERED.getText());
                    // inform amazon
                    amazonService.sendUDelivered(uDeliveryMade.getPackageid());
                    Optional<Package> packageOptional = packageDao.findById(uDeliveryMade.getPackageid());
                    Package pack;
                    if(packageOptional.isPresent()){
                        pack = packageOptional.get();
                    }else {
                        LOG.error("Package " + uDeliveryMade.getPackageid() + " does not exist!SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSDSSSSSSSSSSSSSSSSS");
                        continue;
                    }
                    if(pack.getUpsID().isEmpty()||pack.getUpsID().isBlank()){
                        continue;
                    }

                    try{
                        String userEmail = userService.getEmailByUpsID(userService.getEmailByUpsID(pack.getUpsID()));
                        emailService.sendDeliveredEmail(pack.getPackageID(), pack.getDetails(),userEmail);
                    }catch (Exception ignored) {
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }


    private void handleAcks(UResponses uResponses) {
        int len = uResponses.getAcksCount();
        for (int i = 0; i < len; ++i) {
            long ack = uResponses.getAcks(i);
            if (seqSenderMap.containsKey(ack)) {
                WorldCommandSender handler = seqSenderMap.get(ack);
                handler.onReceive();
                seqSenderMap.remove(ack);
                //LOG.info("Received ack = " + ack + " at index = " + i);
                continue;
            }
            LOG.error("\nAck:\n"+ ack+ " has received twice!\n");
        }
    }

    private void handleUTruck(UResponses uResponses) throws IOException{
        new Thread(() -> {
            try {
                for (int i = 0; i < uResponses.getTruckstatusCount(); ++i) {
                    UTruck uTruck = uResponses.getTruckstatus(i);
                    sendAck(uTruck.getSeqnum());
                    if(ackSet.contains(uTruck.getSeqnum())){
                        continue;
                    }else {
                        ackSet.add(uTruck.getSeqnum());
                    }

                    updateTruckInfo(uTruck);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void updateTruckInfo(UTruck uTruck) {
        truckDao.updatePosition(uTruck.getTruckid(),uTruck.getX(), uTruck.getY());
        truckDao.updateStatus(uTruck.getTruckid(),uTruck.getStatus());
        // LOG.info("Update position for Truck " + uTruck.getTruckid() + ": X = "
        //         + uTruck.getX() + ", Y = " + uTruck.getY() + ". Status: " +uTruck.getStatus());
    }

    public void sendAck(long ack) throws IOException {
        UCommands.Builder uCommandsBuilder = UCommands.newBuilder();
        uCommandsBuilder.addAcks(ack).setDisconnect(false);
        UCommands uCommands = uCommandsBuilder.build();
        //LOG.info("sending world AAAAAAAA" + uCommands + "AAAAAAAA  sendACK\n");
        uCommands.writeDelimitedTo(worldStream.outputStream);
    }
    public void handleUFinished(UResponses uResponses)throws IOException {
        new Thread(() -> {
            try {
                for (int i = 0; i < uResponses.getCompletionsCount(); ++i) {
                    UFinished uFinished = uResponses.getCompletions(i);
                    LOG.info("--- Truck " + uFinished.getTruckid() + " status: " + uFinished.getStatus());
                    sendAck(uFinished.getSeqnum());
                    if(ackSet.contains(uFinished.getSeqnum())){
                        continue;
                    }else {
                        ackSet.add(uFinished.getSeqnum());
                    }
                    //database operation : truck arrive, waiting for package
                    List<Package> aPackages = packageDao.findByTruck_TruckID(uFinished.getTruckid());
                    ArrayList<Long> updatedPackageIDs = new ArrayList<>();
                    for (Package pack : aPackages) {
                        LOG.info("Package" + pack.getPackageID()+ " status : " + pack.getStatus());
                        if (pack.getStatus().equalsIgnoreCase(Package.Status.ROUTING.getText())) {
                            pack.setStatus(Package.Status.WAITING.getText());
                            packageDao.updateStatus(pack.getPackageID(), Package.Status.WAITING.getText());
                            //inform amazon to load
                            updatedPackageIDs.add(pack.getPackageID());
                            LOG.info("Package" + pack.getPackageID()+ " status now chang to: " + Package.Status.WAITING.getText());
                        }
                    }
                    if (updatedPackageIDs.isEmpty()) {
                        LOG.info("Truck" + uFinished.getTruckid()+ " status now chang to: " + Truck.Status.IDLE.getText());
                        truckDao.updateStatus(uFinished.getTruckid(), Truck.Status.IDLE.getText());
                    }else {
                        LOG.info("Truck" + uFinished.getTruckid()+ " status now chang to: " + Truck.Status.ARRIVE.getText());
                        truckDao.updateStatus(uFinished.getTruckid(), Truck.Status.ARRIVE.getText());
                        amazonService.sendUTruckArrived(uFinished.getTruckid(), updatedPackageIDs);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
    public void trackingTruckInfo(Integer truckID)throws IOException {
        long seqNum = seq.incrementAndGet();
        sendQuery(seqNum,truckID);
    }
    public void sendQuery(long seqNum, int truckID) throws IOException {
        new Thread(() -> {
            try {
                UCommands.Builder uCommandB = UCommands.newBuilder();
                UQuery.Builder uQueryB = UQuery.newBuilder();
                uQueryB.setTruckid(truckID).setSeqnum(seqNum);
                uCommandB.addQueries(uQueryB.build()).setDisconnect(false);
                UCommands commands = uCommandB.build();

                //putting in the map
                QueryCommandSender queryCommandSender = new QueryCommandSender(seqNum, truckID, this);
                //LOG.info("start listen to query " + seqNum);
                seqSenderMap.put(seqNum, queryCommandSender);

                //sending
                //LOG.info("sending world -------- " + commands + "-----------\n");
                commands.writeDelimitedTo(worldStream.outputStream);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public int findAvailableTrucks() {
        // List<Truck> truckList = truckDao.findByStatus(Truck.Status.IDLE.getText());
        // if(!truckList.isEmpty()){
        //     return truckList.get(0).getTruckID();
        // }
        // truckList = truckDao.findByStatus(Truck.Status.DELIVERING.getText());
        // if(!truckList.isEmpty()){
        //     return truckList.get(0).getTruckID();
        // }
        // truckList = truckDao.findByStatus(Truck.Status.ARRIVE.getText());
        // if(!truckList.isEmpty()){
        //     return truckList.get(0).getTruckID();
        // }
        // try {
        //     sleep(1000);
        // }catch (Exception e){
        // }
        // return findAvailableTrucks();
        int truckID = truck_alloc.getAndIncrement();
        if (truck_alloc.get() >= TRUCK_CNT) {
            truck_alloc.set(truck_alloc.get() % TRUCK_CNT);
        }
        return truckID;
    }

    public void pickup(int truckID, int warehouseID,long packageID) throws IOException{
        long seqNum = seq.incrementAndGet();
        sendUGoPickUp(seqNum, truckID, warehouseID,packageID);
    }

    public void sendUGoPickUp(long seqNum, int truckID, int warehouseID,long packageID) throws IOException{
        new Thread(() -> {
            try {
                UCommands.Builder uCommandB = UCommands.newBuilder();
                UGoPickup.Builder uGoPickB = UGoPickup.newBuilder();
                uGoPickB.setSeqnum(seqNum).setTruckid(truckID).setWhid(warehouseID);
                uCommandB.addPickups(uGoPickB.build()).setDisconnect(false);

                //putting in the map
                PickUpCommandSender pickUpCommandSender = new PickUpCommandSender(seqNum, truckID, this, warehouseID, packageID);
                pickUpCommandSender.setTimerAndTask();
                seqSenderMap.put(seqNum, pickUpCommandSender);

                UCommands commands = uCommandB.build();
                //LOG.info("sending world -------- " + commands + "-----------\n");
                commands.writeDelimitedTo(worldStream.outputStream);
                // update truck status
                truckDao.updateStatus(truckID, Truck.Status.TRAVELING.getText());
                packageDao.updateStatus(packageID, Package.Status.ROUTING.getText());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void goDeliver(int truckID ,long packageID, int x, int y) throws IOException{
        long seqNum = seq.incrementAndGet();
        sendUGoDeliver(seqNum,truckID,packageID,x,y);
    }

    public void sendUGoDeliver(long seqNum, int truckID, long packageID, int desX, int desY)throws IOException {
        new Thread(() -> {
            try {
                UCommands.Builder uCommandB = UCommands.newBuilder();
                UGoDeliver.Builder uGoDeliverB = UGoDeliver.newBuilder();
                ArrayList<UDeliveryLocation> locations = new ArrayList<>();
                UDeliveryLocation.Builder uDeliveryLocationB = UDeliveryLocation.newBuilder();
                uDeliveryLocationB.setX(desX).setY(desY).setPackageid(packageID);
                locations.add(uDeliveryLocationB.build());
                //build uCommand
                uGoDeliverB.setSeqnum(seqNum).addAllPackages(locations).setTruckid(truckID);
                uCommandB.addDeliveries(uGoDeliverB.build()).setDisconnect(false);

                //putting in the map
                DeliveryCommandSender deliveryCommandSender = new DeliveryCommandSender(seqNum,truckID,this,packageID,desX,desY);
                deliveryCommandSender.setTimerAndTask();
                LOG.info("send UGoDeliver to world " + seqNum);
                seqSenderMap.put(seqNum, deliveryCommandSender);
                UCommands commands = uCommandB.build();

                //LOG.info("sending world -------- " + commands + "-----------\n");
                commands.writeDelimitedTo(worldStream.outputStream);
                worldStream.outputStream.flush();
                // Update DB
                truckDao.updateStatus(truckID,Truck.Status.DELIVERING.getText());
                packageDao.updateStatus(packageID,Package.Status.DELIVERING.getText());

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
//    message UGoDeliver{
//        required int32 truckid = 1;
//        repeated UDeliveryLocation packages = 2;
//        required int64 seqnum = 3;
//    }
//    message UDeliveryLocation{
//        required int64 packageid = 1;
//        required int32 x = 2;
//        required int32 y = 3;
//    }
    // public void RunTest() throws IOException{
//         try{
//             String userEmail = userService.getEmailByUpsID("peicansx");
//             emailService.sendDeliveredEmail(3, "iphone",userEmail);
//         }catch (Exception ignored) {
//        }
//         UConnect.Builder uConnectBuilder = UConnect.newBuilder();
//         truckDao.deleteAll();
//         for (int i = 0; i < TRUCK_CNT; ++i) {
//             UInitTruck.Builder uInitBuilder = UInitTruck.newBuilder();
//             uInitBuilder.setId(i).setX(TRUCK_X).setY(TRUCK_Y);
//             uConnectBuilder.addTrucks(uInitBuilder.build());
//             Truck truck = new Truck();
//             truck.setPosX(TRUCK_X);
//             truck.setPosY(TRUCK_Y);
//             truck.setStatus(Truck.Status.IDLE.getText());
//             truck.setTruckID(i);
//             truckDao.save(truck);
//         }
//         uConnectBuilder.setIsAmazon(false);
//         UConnect request = uConnectBuilder.build();
//         LOG.info("sending world -------- " + request + "-----------\n");
//         request.writeDelimitedTo(worldStream.outputStream);
//
//         receiveConnectedFromWorld();
//         new Thread(()->{
//             while (true) {
//             try {
//                 receiveUResponses();
//             } catch (IOException e) {
//                 LOG.error("world listener error:" + e.getMessage());
//                 System.exit(1);
//             }
//         }}).start();
//         goDeliver(1,0,4,5);
         // goDeliver(2,3,1,8);
         // trackingTruckInfo(0);
         // trackingTruckInfo(1);
         // trackingTruckInfo(2);
         // trackingTruckInfo(3);
         // pickup(0, 7, 6);
         // pickup(4, 2, 5);


    // }
}
