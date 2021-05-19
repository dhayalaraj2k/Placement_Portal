package com.example.PlacementPortal.Controllers;

import com.example.PlacementPortal.Entities.Admin;
import com.example.PlacementPortal.Repositories.adminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class adminController {
    @Autowired
    adminRepository adminRepo;

    @GetMapping("/addadmin")
    public String adminSignup(Model model){
        model.addAttribute("adminInstance",new Admin());
        return "addadmin";
    }

    @PostMapping("/addadmin")
    public String adminSignupPost(@ModelAttribute Admin adminInstance, Model model){
        model.addAttribute("adminInstance", adminInstance);
        adminRepo.save(adminInstance);
        return "adminResult";
    }
}
