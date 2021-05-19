package com.example.PlacementPortal.Controllers;

import com.example.PlacementPortal.Entities.*;
import com.example.PlacementPortal.Repositories.applicationRepository;
import com.example.PlacementPortal.Repositories.companyRepository;
import com.example.PlacementPortal.Repositories.postingRepository;
import com.example.PlacementPortal.Repositories.studentRepository;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Controller
public class companyController{
    @Autowired
    companyRepository companyRepo;

    @Autowired
    postingRepository postingRepo;

    @Autowired
    applicationRepository applicationRepo;

    @Autowired
    studentRepository studentRepo;

    studentController studentControllerInstance = new studentController();

    @GetMapping("/company")
    public String student(){
        return "company";
    }
    @GetMapping("/addcompany")
    public String companySignup(Model model){
        model.addAttribute("companyInstance", new Company());
        return "addcompany";
    }

    @PostMapping("/addcompany")
    public String companySignupPost(@ModelAttribute Company companyInstance, Model model) throws MessagingException, IOException, URISyntaxException {
        model.addAttribute("companyInstance",companyInstance);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(companyInstance.getPassword());
        companyInstance.setPassword(encodedPassword);
        String token = studentControllerInstance.createToken();
        companyInstance.setToken(token);
        companyRepo.save(companyInstance);
        studentControllerInstance.sendmail(new Student(), companyInstance, 2);
        return "companyResult";
    }

    @RequestMapping(value = "/verifyCompanyBranch", method = RequestMethod.POST)
    public @ResponseBody boolean verifyName(@RequestBody String argument) throws ParseException {
//        System.out.println(argument);
        int index = argument.indexOf('+');
        String name = argument.substring(0,index);
        String branch = argument.substring(index+1,argument.length()-1);
        if(companyRepo.existsByCompanyName(name)){
            List<Company> companies = companyRepo.findCompaniesByCompanyName(name);
            for(Company company : companies)
                if(company.getBranch().equals(branch)) return true;
        }
        return false;
    }

    @RequestMapping(value="/verifyCompanyEmail", method = RequestMethod.POST)
    public @ResponseBody boolean verifyEmail(@RequestBody String email){
        email = studentControllerInstance.processEmail(email);
        return !companyRepo.existsByEmail(email);
    }

    @GetMapping("/companyverification/**")
    public String verification(HttpServletRequest request){
        String token = request.getQueryString();
//        System.out.println(token);
        int index = token.indexOf("&");
        String username = token.substring(index+1);
        token = token.substring(0,index);
        Company company1 = companyRepo.findCompanyByToken(token);
        if(company1.getCompanyName().equals(username)) {
            company1.setEnabled(true);
            companyRepo.save(company1);
            return "verificationSuccess";
        }
        return "verificationFailed";
    }

    @GetMapping("/companyLogin")
    public String login(Model model){
        Login companyLoginInstance = new Login();
        model.addAttribute("companyLoginInstance",companyLoginInstance);
        return "companyLogin";
    }

    @PostMapping("/authenticateCompany")
    public String login(@ModelAttribute Login companyLoginInstance, Model model, HttpServletRequest request){
        model.addAttribute("companyLoginInstance",companyLoginInstance);
        model.addAttribute("postingInstance", new Posting());
        boolean exists = companyRepo.existsByEmail(companyLoginInstance.getEmail());
        if(exists) {
            Company company = companyRepo.findCompanyByEmail(companyLoginInstance.getEmail());
            if(!company.isEnabled()) {
                model.addAttribute("remind", "Verify email before logging in. Check email for verification link");
                return "companyLogin";
            }
            else {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                if (encoder.matches(companyLoginInstance.getPassword(), company.getPassword())) {
                    model.addAttribute("string", "");
                    HttpSession session = request.getSession();
                    session.setAttribute("companyid", company.getId());
                    model.addAttribute("session", session);
                }
                else{
                    model.addAttribute("string", "Wrong password. Enter correct credentials.");
                    return "companyLogin";
                }
            }
        }
        else{
            model.addAttribute("string","Wrong email. Enter correct credentials.");
            return "companyLogin";
        }
        return "companyIndex";
    }

    @GetMapping("/forgotCompany")
    public String forgot(Model model){
        model.addAttribute("forgotInstance",new Forgot());
        return "forgotCompany";
    }

    @PostMapping("/resetCompany")
    public String forgot(@ModelAttribute Forgot forgotInstance, Model model) throws MessagingException, IOException,
            URISyntaxException {
        String email = forgotInstance.getEmail();
        if(companyRepo.existsByEmail(email)){
            System.out.println("YES");
            sendResetMail(companyRepo.findCompanyByEmail(email));
        }
        return "checkCompanyMail";
    }

    private void sendResetMail(Company company) throws AddressException, MessagingException, IOException,
            URISyntaxException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("dhayalaraj69@gmail.com", "Dhayalarajat1");
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("dhayalaraj69@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(company.getEmail()));
        msg.setSubject("Classifieds Registration");
        msg.setContent("Verify email", "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("Hello "+company.getHR()+",\n To reset password click here "
                +studentControllerInstance.createResetLink(company.getToken(),
                "resetcompanypassword"),"text" +
                "/html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
//        MimeBodyPart attachPart = new MimeBodyPart();
//        attachPart.attachFile("/home/dhaya/important.jpg");
//        multipart.addBodyPart(attachPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }

    @GetMapping("/resetcompanypassword/**")
    public String resetPassword(HttpServletRequest request, Model model){
        String token = request.getQueryString();
        Forgot forgotInstance = new Forgot();
        if(companyRepo.existsByToken(token)){
            forgotInstance.setToken(token);
            model.addAttribute("forgotInstance",forgotInstance);
            return "resetCompanyPassword";
        }
        return "verificationFailed";
    }

    @PostMapping("/resetcompanypass")
    public String resetPass(@ModelAttribute Forgot forgotInstance, Model model){
        Company company = companyRepo.findCompanyByToken(forgotInstance.getToken());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(forgotInstance.getPassword());
        company.setPassword(encodedPassword);
        company.setToken(studentControllerInstance.createToken());
        companyRepo.save(company);
        model.addAttribute("string","Password reset successfully.\nTry logging in");
        return "resetSuccess";
    }

    @GetMapping("/companyIndex")
    public String companyIndex(){
        return "companyIndex";
    }

    @GetMapping("/companylogout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("companyid");
        return "companylogoutSuccess";
    }

    @GetMapping("/applicants")
    public String viewApplicants(Model model, HttpServletRequest request){
        Long companyId = (Long)request.getSession().getAttribute("companyid");
        List<Posting> postings = postingRepo.findPostingsByCompanyId(companyId);
//        List<Student> students = new ArrayList<>();
//        for(Posting posting : postings){
//            List<Application> applications = applicationRepo.findApplicationsByPostingId(posting.getId());
//            for(Application application : applications)
//                students.add(studentRepo.findStudentById(application.getStudentId()));
//        }
        model.addAttribute("postings",postings);
        return "applicants";
    }

    @GetMapping("/showapplicants/{id}")
    public String showApplicants(@PathVariable Long id, Model model, HttpServletRequest request){
        List<Application> applications = applicationRepo.findApplicationsByPostingId(id);
        List<Student> students = new ArrayList<>();
        for(Application application : applications)
                students.add(studentRepo.findStudentById(application.getStudentId()));
        model.addAttribute("students",students);
        return "viewapplicants";
    }
}
