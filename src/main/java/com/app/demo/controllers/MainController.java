package com.app.demo.controllers;


import com.app.demo.repository.RoleRepository;
import com.app.demo.service.UserService;
import com.app.demo.model.Role;
import com.app.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
public class MainController {
    private UserService userService;
    private RoleRepository roleRepository;
    private PasswordEncoder bCryptPasswordEncoder;
    @Autowired
    public void setPasswordEncoder(PasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String userPage(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user",user);
        if (user == null) {
            return "redirect:/login";
        }
        return "user";
    }

    @GetMapping("/admin")
    public String showAllUsers(Model model) {
        List<User> allUsers = userService.listUsers();
        model.addAttribute("allUsers", allUsers);
        return "admin";
    }

    @GetMapping("/admin/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        List<Role> roles =  roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "new";
    }

    @PostMapping("/admin/save")
    public String saveUser(@ModelAttribute("user") User user) {
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userService.saveUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/admin/edit")
    public String editUser(@RequestParam("id") long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        List<Role> roles =  roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "edit";
    }

    @PostMapping("/admin/update")
    public String updateUser(@RequestParam(value = "roles", required = false) String[] roles, @ModelAttribute("user") User user) {
        Set<Role> userRoles = new HashSet<>();
        if (roles != null) {
            for (String roleId : roles) {
                try {
                    long id = Long.parseLong(roleId);
                    Optional<Role> role = roleRepository.findById(id);
                    role.ifPresent(userRoles::add);
                } catch (NumberFormatException e) {
                }
            }
        } else {
            userRoles = Collections.emptySet();
        }
        user.setRoles(userRoles);
        userService.updateUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/admin/delete")
    public String deleteUser(@RequestParam("id") long id) {
        userService.deleteById(id);
        return "redirect:/admin";
    }

    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
}
