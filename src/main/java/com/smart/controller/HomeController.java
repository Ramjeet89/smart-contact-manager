package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.helper.Message;
import org.dom4j.rule.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home-Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About-Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("title", "Register -Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    //This handler for registering users
    @RequestMapping(value = "/do_register", method = RequestMethod.POST)
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
                               @RequestParam(value = "agreement", defaultValue = "false")
                                       boolean agreement, Model model, HttpSession session) {
        try {
            if (!agreement) {
                System.out.println("You have not agreed T&C");
                throw new Exception("You have not agreed T&C");
            }
            if (result1.hasErrors()) {
                System.out.println("ERROR " + result1);
                model.addAttribute("user", user);
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User result = this.userRepository.save(user);
            System.out.println("Agreement" + agreement);
            System.out.println("User" + user);
            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    //handler for custom logins

    @GetMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title","Login Page");
        return "login";
    }
}
