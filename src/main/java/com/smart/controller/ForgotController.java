package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {

    Random random = new Random(1000);

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //email id from open handler
    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    //email id from open handler
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, HttpSession session) {
        System.out.println("Email:: " + email);
        //generating otp for four digit

        int otp = random.nextInt(999999);
        System.out.println("OTP:: " + otp);
        String subject = "OTP from SCM";
        String message = ""
                + "<div style='border:1px; solid #008000; padding:20px;'>"
                + "<h1>"
                + "Your OTP is "
                + "<b>" + otp
                + "</n>"
                + "</h1>"
                + "</div>";
        String to = email;
        //write code for send otp
        boolean flag = this.emailService.sendEmail(subject, message, to);
        if (flag) {
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        } else {
            session.setAttribute(message, "Check your email id!!");
            return "forgot_email_form";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp") int otp, HttpSession session) {
        int myOtp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");
        if (myOtp == otp) {
            //change password

           User user =  this.userRepository.getUserByUserName(email);
           if(user==null){
               //send error message
               session.setAttribute("message","User Does not exist with this email id!!");
               return "forgot_email_form";
           }else {
               //send change password form
           }
            return "password_change_form";
        } else {

            session.setAttribute("message", "You have entered wrong otp");
            return "verify_otp";
        }
    }
//change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword")String newpassword,HttpSession session){
        String email = (String) session.getAttribute("email");
        User user =  this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepository.save(user);
        return "redirect:/signin?change=password changed successfully !!";
    }
}
