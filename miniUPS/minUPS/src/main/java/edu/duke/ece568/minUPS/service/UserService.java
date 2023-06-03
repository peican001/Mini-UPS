package edu.duke.ece568.minUPS.service;

import edu.duke.ece568.minUPS.entity.Users;
import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.entity.Truck;
import edu.duke.ece568.minUPS.dao.UserDao;
import edu.duke.ece568.minUPS.dao.PackageDao;
import edu.duke.ece568.minUPS.dao.TruckDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PackageDao packageDao;

    @Autowired
    private TruckDao truckDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

//    private final UserDao userDao;
//    private final BCryptPasswordEncoder passwordEncoder;
//
//    @Autowired
//    public UserService(UserDao userDao, BCryptPasswordEncoder passwordEncoder) {
//        this.userDao = userDao;
//        this.passwordEncoder = passwordEncoder;
//    }

    public Users registerUser(Users user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    public Optional<Users> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public Package findPackageById(Long packageId) {
        return packageDao.findById(packageId).orElse(null);
    }

    public Truck findTruckById(Integer truckId) {
        return truckDao.findById(truckId).orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Users> user = findByEmail(email);
        if (!user.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return new org.springframework.security.core.userdetails.User(user.get().getEmail(), user.get().getPassword(), Collections.singletonList(authority));
    }

    public String getEmailByUpsID(String upsID)throws NoSuchFieldException {
        Optional<Users> userOptional = userDao.findByUpsID(upsID);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            return user.getEmail();
        } else {
            throw new NoSuchElementException("No user found with upsID: " + upsID);
        }
    }
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Optional<Users> user = findByEmail(email);
//        if (!user.isPresent()) {
//            throw new UsernameNotFoundException("User not found");
//        }
//
//        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
//        return new CustomUserDetails(user.get(), Collections.singletonList(authority));
//    }

    public List<Package> findAllByUpsID(String upsID) {
        return packageDao.findAllByUpsID(upsID);
    }

    public void updateEmail(String currentEmail, String newEmail) {
        Users user = userDao.findByEmail(currentEmail).orElse(null);
        if (user != null) {
            user.setEmail(newEmail);
            userDao.save(user);
        }
    }

    public void updatePassword(Users user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userDao.save(user);
    }



}

