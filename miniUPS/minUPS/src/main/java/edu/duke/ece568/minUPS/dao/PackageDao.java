package edu.duke.ece568.minUPS.dao;

import edu.duke.ece568.minUPS.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageDao extends JpaRepository<Package,Long> {
    List<Package> findByTruck_TruckID(int truckID);

    @Modifying
    @Transactional
    @Query("UPDATE Package p SET p.truck.truckID = :truckID WHERE p.packageID = :packageID")
    int updateTruckID(@Param("packageID") Long packageID, @Param("truckID") Integer truckID);

    @Modifying
    @Transactional
    @Query("UPDATE Package p SET p.status = :status WHERE p.packageID = :packageID")
    int updateStatus(@Param("packageID") Long packageID, @Param("status") String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Package e WHERE e.packageID = :id")
    Optional<Package> findByIdForUpdate(@Param("id") Long id);



    List<Package> findAllByUpsID(String upsID);
}
