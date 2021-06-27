package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.MyOrder;
import com.smart.entity.User;
import com.smart.helper.Message;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.razorpay.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private MyOrderRepository myOrderRepository;

    //method for adding common data for response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String username = principal.getName();
        System.out.println("Username:: " + username);
        User user = this.userRepository.getUserByUserName(username);
        System.out.println("USER:==>" + user);
        model.addAttribute("user", user);
    }

    //Dashboard home page
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    //open add handlers
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    //Processing add contact form
    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file,
            Principal principal, HttpSession session) {
        try {
            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);
            //processing and uploading files
            if (file.isEmpty()) {
                //if the file is empty then try to message
                System.out.println("File is Empty");
                contact.setImage("contact.png");
            } else {
                //upload the file into the folder
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("images is uploaded");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);
            System.out.println("Data:: " + contact);
            System.out.println("Added to database!!");
            session.setAttribute("message", new Message("Your Contact is add !! Add more", "success"));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong!! Try again", "danger"));
        }
        return "normal/add_contact_form";
    }

    //show contact handlers
    @GetMapping("/show-contact/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
        m.addAttribute("title", "show user contacts");
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        Pageable pageable = PageRequest.of(page, 8);
        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show-contact";
    }

    @RequestMapping("/{cId}/contact")
    public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        System.out.println("CID::" + cId);
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }
        return "normal/contact_details";
    }

    //Delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
        Contact contact = this.contactRepository.findById(cId).get();
        System.out.println("Contact:: " + contact.getcId());
        //check
        // contact.setUser(null);
        // this.contactRepository.delete(contact);
        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);
        System.out.println("DELETED");
        session.setAttribute("message", new Message("Contact deleted Successfully", "success"));
        return "redirect:/user/show-contact/0";
    }

    //open update form handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model m) {
        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cId).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    //update contact handler
    @RequestMapping(value = "/process-update", method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage")
            MultipartFile file, Model m, HttpSession session, Principal principal) {

        try {
            //images
            //old contact delails

            Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

            if (!file.isEmpty()) {
                //delete old img
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1 = new File(deleteFile, oldContactDetails.getImage());
                file1.delete();

                //file works
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldContactDetails.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your contact is updated...", "success"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Contact" + contact.getName());
        System.out.println("Contact" + contact.getcId());
        return "redirect:/user/" + contact.getcId() + "/contact";
    }

    //your profile handlers
    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    //open settings handler
    @GetMapping("/settings")
    public String openSettings() {
        return "normal/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(Principal principal, HttpSession session,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword) {
        System.out.println("OldPassword:: " + oldPassword);
        System.out.println("NewPassword:: " + newPassword);

        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);
        System.out.println(currentUser.getPassword());
        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            //change the password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your Password is  Successfully Changed", "success"));
        } else {
            ////error
            session.setAttribute("message", new Message("Please Enter correct old password !!", "danger"));
            return "redirect:/user/settings";
        }
        return "redirect:/user/index";
    }

    //creating order for payments
    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
        //System.out.println("Hey order function is executed !! ");
        System.out.println(data);
        int amt = Integer.parseInt(data.get("amount").toString());

        var client = new RazorpayClient("rzp_test_6K9lVshaJG2edT", "qtXS6IoG0a8spPnezklX0es3");
        JSONObject ob = new JSONObject();
        ob.put("amount", amt * 100);
        ob.put("currency", "INR");
        ob.put("receipt", "txn_235425");
        //creating new order
        Order order = client.Orders.create(ob);
        System.out.println("Order:: " + order);
        //if u want to save the order in database
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount") + "");
        myOrder.setOrderId(order.get("id"));
        myOrder.setPaymentId(null);
        myOrder.setStatus("created");
        myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
        myOrder.setReceipt(order.get("receipt"));
        this.myOrderRepository.save(myOrder);
        return order.toString();
    }

    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {
        System.out.println("Data::" + data);
        MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
        myOrder.setPaymentId(data.get("payment_id").toString());
        myOrder.setStatus(data.get("status").toString());
        this.myOrderRepository.save(myOrder);
        return ResponseEntity.ok(Map.of("msg", "updated"));
    }
}
