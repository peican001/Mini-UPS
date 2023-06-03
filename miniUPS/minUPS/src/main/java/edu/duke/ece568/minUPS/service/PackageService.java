package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.dao.PackageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class PackageService {
    @Autowired
    private PackageDao packageDao;
    public List<Package> findAllByUpsID(String upsID) {
        return packageDao.findAllByUpsID(upsID);
    }

    @Transactional
    public Package findPackageByIdForUpdate(Long packageID){
        Package pkg = packageDao.findByIdForUpdate(packageID).orElse(null);
        return pkg;

    }

    @Transactional
    public void savePackage(Package pkg){
        packageDao.save(pkg);
    }

//    @Transactional
//    public Package updateDestination(Long packageID, Integer destinationX, Integer destinationY) {
//        Package pkg = packageDao.findByIdForUpdate(packageID).orElse(null);
//        if (pkg != null) {
//            pkg.setDestinationX(destinationX);
//            pkg.setDestinationY(destinationY);
//            return packageDao.save(pkg);
//        }
//        return null;
//    }
    @Transactional
    public Package updateDestination(Package pkg, Integer destinationX, Integer destinationY) {
        //Package pkg = packageDao.findByIdForUpdate(packageID).orElse(null);
        if (pkg != null) {
            pkg.setDestinationX(destinationX);
            pkg.setDestinationY(destinationY);
            return packageDao.save(pkg);
        }
        return null;
    }


}
