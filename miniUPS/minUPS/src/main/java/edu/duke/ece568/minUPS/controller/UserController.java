package edu.duke.ece568.minUPS.controller;

import edu.duke.ece568.minUPS.entity.Package;
import edu.duke.ece568.minUPS.entity.Users;
import edu.duke.ece568.minUPS.entity.Truck;
import edu.duke.ece568.minUPS.service.UserService;
import edu.duke.ece568.minUPS.service.PackageService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.PersistenceException;
import java.security.Principal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Controller
public class UserController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private PackageService packageService;

//    @GetMapping("/image/{filename:.+}")
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//        Resource resource = new ClassPathResource("static/image/" + filename);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_JPEG)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") Users users, Model model) {
        try {
            userService.registerUser(users);
            return "redirect:/login";
        }catch (DataIntegrityViolationException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException) {
                ConstraintViolationException constraintException = (ConstraintViolationException) cause;
                String errorMessage = constraintException.getSQLException().getMessage();

                if (errorMessage.contains("Key (upsid)=")) {
                    model.addAttribute("error", "UpsID already exists!");
                } else if (errorMessage.contains("Key (email)=")) {
                    model.addAttribute("error", "This email is already used!");
                }
            }
            return "register";
        }

    }



    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping({"/", "/main"})
    public String mainPage() {
        return "mainpage";
    }

    private String computeDistance(Package aPackage, Truck truck) {
        if (aPackage.getStatus().equals(Package.Status.DELIVERING.getText())) {
            int deltaX = aPackage.getDestinationX() - truck.getPosX();
            int deltaY = aPackage.getDestinationY() - truck.getPosY();
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            return String.format("%.2f", distance);
        } else if (aPackage.getStatus().equals(Package.Status.DELIVERED.getText())) {
            return "Check your mailbox!";
        } else {
            return "Still in warehouse!";
        }
    }

    @GetMapping("/searchPackage")
    public String searchPackage(@RequestParam("packageId") Long packageId, Model model) {
        Package foundPackage = userService.findPackageById(packageId);
        if (foundPackage == null) {
            model.addAttribute("errorMessage", "Sorry, the Tracking number is not valid!");
            return "mainpage";
        }
        model.addAttribute("foundPackage", foundPackage);
        Truck atruck = foundPackage.getTruck();
        model.addAttribute("atruck", atruck);
        model.addAttribute("distance", computeDistance(foundPackage, atruck));

        String location;
        Package.Status status = Package.Status.fromString(foundPackage.getStatus());
        if (status == Package.Status.DELIVERED) {
            location = "(" + foundPackage.getDestinationX() + ", " + foundPackage.getDestinationY() + ")";
        } else if (status == Package.Status.DELIVERING) {
            Truck truck = userService.findTruckById(foundPackage.getTruck().getTruckID());
            location = "(" + truck.getPosX() + ", " + truck.getPosY() + ")";
        } else {
            location = "Wait for delivery";
        }
        model.addAttribute("location", location);

        return "packageInfo";
    }

    @GetMapping("/userPackages")
    public String userPackages(Model model, Principal principal) {
        Users user = userService.findByEmail(principal.getName()).orElse(null);
        if (user != null) {
            List<Package> userPackages = packageService.findAllByUpsID(user.getUpsID());
            userPackages.sort(Comparator.comparing(Package::getPackageID));
            List<Truck> trucks = userPackages.stream().map(Package::getTruck).collect(Collectors.toList());
            List<String> distances = new ArrayList<>();
            for (int i = 0; i < userPackages.size(); i++) {
                distances.add(computeDistance(userPackages.get(i), trucks.get(i)));
            }
            model.addAttribute("trucks", trucks);
            model.addAttribute("distances", distances);
            model.addAttribute("userPackages", userPackages);
        }
        return "userPackages";
    }

    @GetMapping("/changedest/{id}")
    public String showChangeDestForm(@PathVariable("id") Long packageID, Model model) {
        model.addAttribute("packageID", packageID);
        return "changedest";
    }

//    @PostMapping("/changedest/{id}")
//    public String changeDestination(@PathVariable("id") Long packageID,
//                                    @RequestParam("destinationX") Integer destinationX,
//                                    @RequestParam("destinationY") Integer destinationY,
//                                    RedirectAttributes redirectAttributes) {
//
//            packageService.updateDestination(packageID, destinationX, destinationY);
//            redirectAttributes.addFlashAttribute("message", "Destination updated successfully!");
//
//        return "redirect:/userPackages";
//    }
//    @PostMapping("/changedest/{id}")
//    public ModelAndView changeDestination(@PathVariable("id") Long packageID,
//                                          @RequestParam("destinationX") Integer destinationX,
//                                          @RequestParam("destinationY") Integer destinationY) {
//        Package foundPackage = userService.findPackageById(packageID);
//        ModelAndView modelAndView = new ModelAndView();
//
//        if (foundPackage != null && (foundPackage.getStatus().equals(Package.Status.DELIVERED.getText()) || foundPackage.getStatus().equals(Package.Status.DELIVERING.getText()))) {
//            modelAndView.addObject("errorMessage", "Sorry, the package is out for delivery, you cannot change its destination now!");
//            modelAndView.setViewName("changedest");
//        } else {
//            packageService.updateDestination(packageID, destinationX, destinationY);
//            modelAndView.setViewName("redirect:/userPackages");
//        }
//
//        return modelAndView;
//}
    @PostMapping("/changedest/{id}")
    public ModelAndView changeDestination(@PathVariable("id") Long packageID,
                                          @RequestParam("destinationX") Integer destinationX,
                                          @RequestParam("destinationY") Integer destinationY) {
        Package foundPackage = packageService.findPackageByIdForUpdate(packageID);
        ModelAndView modelAndView = new ModelAndView();

        if (foundPackage != null && (foundPackage.getStatus().equals(Package.Status.DELIVERED.getText()) || foundPackage.getStatus().equals(Package.Status.DELIVERING.getText()))) {
            modelAndView.addObject("errorMessage", "Sorry, the package is out for delivery, you cannot change its destination now!");
            packageService.savePackage(foundPackage);
            modelAndView.setViewName("changedest");
        } else {
            packageService.updateDestination(foundPackage, destinationX, destinationY);
            modelAndView.setViewName("redirect:/userPackages");
        }

        return modelAndView;
    }


    @GetMapping("/userProfile")
    public String showUserProfile(Model model, Principal principal) {
        if (principal != null) {
            Users user = userService.findByEmail(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            }

        }
        return "userProfile";
    }


    @GetMapping("/changePassword")
    public String showChangePasswordForm() {
        return "changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        Users user = userService.findByEmail(principal.getName()).orElse(null);
        if (user != null) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("passwordError", "New password and confirm password do not match!");
                return "changePassword";
            }
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                model.addAttribute("passwordError", "Old password is incorrect!");
                return "changePassword";
            }
            userService.updatePassword(user, newPassword);
            redirectAttributes.addFlashAttribute("message", "Password updated successfully!");
        }
        return "redirect:/userProfile";
    }





}