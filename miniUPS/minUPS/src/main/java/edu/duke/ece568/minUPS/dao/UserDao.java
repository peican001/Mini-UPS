package edu.duke.ece568.minUPS.dao;

import edu.duke.ece568.minUPS.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<Users,Integer> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByUpsID(String upsID);
}
