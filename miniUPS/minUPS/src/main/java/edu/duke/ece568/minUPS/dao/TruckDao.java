package edu.duke.ece568.minUPS.dao;

import edu.duke.ece568.minUPS.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TruckDao extends JpaRepository<Truck,Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Truck s SET s.status = :status WHERE s.truckID = :id")
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("UPDATE Truck s SET s.posX = :posX, s.posY = :posY WHERE s.truckID = :id")
    int updatePosition(@Param("id") Integer id, @Param("posX") Integer posX, @Param("posY") Integer posY);

    List<Truck> findByStatus(String status);
}
