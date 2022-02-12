package com.example.PlacementPortal.Controllers;

import com.example.PlacementPortal.Entities.*;
import com.example.PlacementPortal.Repositories.applicationRepository;
import com.example.PlacementPortal.Repositories.postingRepository;
import com.example.PlacementPortal.Repositories.studentRepository;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

@Controller
public class studentController {
    @Autowired
    studentRepository studentRepo;

    @Autowired
    postingRepository postingRepo;

    @Autowired
    applicationRepository applicationRepo;

    @GetMapping("/student")
    public String student() {
        return "student";
    }

    @GetMapping("/addstudent")
    public String studentSignup(Model model) {
        model.addAttribute("studentInstance", new Student());
        return "addstudent";
    }

    @PostMapping("/addstudent")
    public String studentSignupPost(@ModelAttribute Student studentInstance, Model model) throws IOException, URISyntaxException, MessagingException {
        model.addAttribute("studentInstance", studentInstance);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(studentInstance.getPassword());
        studentInstance.setPassword(encodedPassword);
        String token = createToken();
        studentInstance.setToken(token);
        studentRepo.save(studentInstance);
        sendmail(studentInstance, new Company(), 1);
        return "studentResult";
    }

    public String createToken() {
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = 20;
        Random random = new Random();
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
//        System.out.println(generatedString);
        return generatedString;
    }

    public void sendmail(Student student, Company company, int choice) throws AddressException, MessagingException,
            IOException,
            URISyntaxException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("", ""); // Your gmail and password to authenticate with smtp server.
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("", false));//Enter the from mail address.

        if (choice == 1)
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
        else
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(company.getEmail()));
        msg.setSubject("Classifieds Registration");
        msg.setContent("Verify email", "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        if (choice == 1)
            messageBodyPart.setContent("Hello " + student.getName() + ",\nVerify your email by clicking the link " +
                    "below before logging in.\n" + createLink(student.getToken(), student.getUsername(), choice),
                    "text/html");
        else
            messageBodyPart.setContent("Hello " + company.getHR() + ",\nVerify your email by clicking the link " +
                    "below before logging in.\n" + createLink(company.getToken(), company.getCompanyName(), choice),
                    "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
//        MimeBodyPart attachPart = new MimeBodyPart();
//        attachPart.attachFile("/home/dhaya/important.jpg");
//        multipart.addBodyPart(attachPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }

    public URL createLink(String token, String username, int choice) throws URISyntaxException, MalformedURLException {
        String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String path = "";
        if (choice == 1)
            path = "/studentverification";
        else if (choice == 2)
            path = "/companyverification";
        String auth = null;
        String fragment = null;
        String query = token + "&" + username;
        URI uri = new URI(protocol, auth, host, port, path, query, fragment);
        URL url = uri.toURL();
        return url;
    }

    @GetMapping("/verify")
    public String verify() {
        return "verificationSuccess";
    }

    @RequestMapping(value = "/verifyStudentName", method = RequestMethod.POST)
    public @ResponseBody
    boolean verifyName(@RequestBody String name) {
        String str = name.substring(0, name.length() - 1);
        return !studentRepo.existsByUsername(str);
    }

    @RequestMapping(value = "/verifyStudentEmail", method = RequestMethod.POST)
    public @ResponseBody
    boolean verifyEmail(@RequestBody String email) {
        String finalStr = processEmail(email);
        return !studentRepo.existsByEmail(finalStr);
    }

    public String processEmail(String email) {
        int startIndex = email.indexOf("%40");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < startIndex; i++)
            str.append(email.charAt(i));
        str.append('@');
        for (int i = startIndex + 3; i < email.length(); i++)
            str.append(email.charAt(i));
        return str.substring(0, str.length() - 1);
    }

    @GetMapping("/studentverification/**")
    public String verification(HttpServletRequest request) {
        String token = request.getQueryString();
//        System.out.println(token);
        int index = token.indexOf("&");
        String username = token.substring(index + 1);
        token = token.substring(0, index);
        Student student1 = studentRepo.findStudentByUsername(username);
        Student student2 = studentRepo.findStudentByToken(token);
        if (student1.getId() == student2.getId()) {
            student1.setEnabled(true);
            studentRepo.save(student1);
            return "verificationSuccess";
        }
        return "verificationFailed";
    }

    @GetMapping("/studentLogin")
    public String login(Model model) {
        Login studentLoginInstance = new Login();
        model.addAttribute("studentLoginInstance", studentLoginInstance);
        return "studentLogin";
    }

    @PostMapping("/authenticateStudent")
    public String login(@ModelAttribute Login studentLoginInstance, Model model, HttpServletRequest request) {
        model.addAttribute("studentLoginInstance", studentLoginInstance);
        boolean exists = studentRepo.existsByUsername(studentLoginInstance.getUsername());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (exists) {
            Student student = studentRepo.findStudentByUsername(studentLoginInstance.getUsername());
            if (!student.isEnabled()) {
                model.addAttribute("string", "Verify your email by clicking the link in email first");
                return "studentLogin";
            }
            else {
                if (encoder.matches(studentLoginInstance.getPassword(), student.getPassword())) {
                    model.addAttribute("string", "");
                    HttpSession session = request.getSession();
                    session.setAttribute("studentid", student.getId());
                    model.addAttribute("session", session);
                } else {
                    model.addAttribute("forgot", "Wrong password. Enter correct credentials.");
                    return "studentLogin";
                }
            }
        } else {
            model.addAttribute("forgot", "Wrong Username. Enter correct credentials.");
            return "studentLogin";
        }
        studentLoginInstance.setPassword(encoder.encode(studentLoginInstance.getPassword()));
        return "studentIndex";
    }

    @GetMapping("/studentIndex")
    public String studentIndex(){
        return "studentIndex";
    }

    @GetMapping("/studentlogout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute("studentid");
        session.removeAttribute("cgpa");
        return "studentlogoutSuccess";
    }

    @GetMapping("/forgotStudent")
    public String forgot(Model model) {
        model.addAttribute("forgotInstance", new Forgot());
        return "forgotStudent";
    }

    @PostMapping("/resetStudent")
    public String forgot(@ModelAttribute Forgot forgotInstance, Model model) throws MessagingException, IOException,
            URISyntaxException {
        String email = forgotInstance.getEmail();
        if (studentRepo.existsByEmail(email)) {
//            System.out.println("YES");
            sendResetMail(studentRepo.findStudentByEmail(email));
        }
        return "checkStudentMail";
    }

    public URL createResetLink(String token, String message) throws URISyntaxException, MalformedURLException {
        String protocol = "http";
        String host = "localhost";
        int port = 8080;
        String path = "/" + message;
        String auth = null;
        String fragment = null;
        String query = token;
        URI uri = new URI(protocol, auth, host, port, path, query, fragment);
        URL url = uri.toURL();
        return url;
    }

    private void sendResetMail(Student student) throws AddressException, MessagingException, IOException,
            URISyntaxException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("", ""); //Enter the gmail and password.
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("", false)); //Enter the from address.

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(student.getEmail()));
        msg.setSubject("Classifieds Registration");
        msg.setContent("Verify email", "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("Hello " + student.getName() + ",\n To reset password click here " +
                        createResetLink(student.getToken(),"resetpassword")
                + "\n To reset Username click here " + createResetLink(student.getToken(), "resetusername"),
                "text/html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        msg.setContent(multipart);
        Transport.send(msg);
    }

    @GetMapping("/resetpassword/**")
    public String resetPassword(HttpServletRequest request, Model model) {
        String token = request.getQueryString();
        Forgot forgotInstance = new Forgot();
        if (studentRepo.existsByToken(token)) {
            forgotInstance.setToken(token);
            model.addAttribute("forgotInstance", forgotInstance);
            return "resetPassword";
        }
        return "verificationFailed";
    }

    @GetMapping("/resetusername/**")
    public String resetUsername(HttpServletRequest request, Model model) {
        String token = request.getQueryString();
        Forgot forgotInstance = new Forgot();
        if (studentRepo.existsByToken(token)) {
            forgotInstance.setToken(token);
            model.addAttribute("forgotInstance", forgotInstance);
            return "resetUsername";
        }
        return "verificationFailed";
    }

    @PostMapping("/resetpass")
    public String resetPass(@ModelAttribute Forgot forgotInstance, Model model) {
        Student student = studentRepo.findStudentByToken(forgotInstance.getToken());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(forgotInstance.getPassword());
        student.setPassword(encodedPassword);
        student.setToken(createToken());
        studentRepo.save(student);
        model.addAttribute("string", "Password reset successfully.\nTry logging in");
        return "resetSuccess";
    }

    @PostMapping("/resetUser")
    public String resetUser(@ModelAttribute Forgot forgotInstance, Model model) {
        Student student = studentRepo.findStudentByToken(forgotInstance.getToken());
        student.setUsername(forgotInstance.getUsername());
        student.setToken(createToken());
        studentRepo.save(student);
        model.addAttribute("string", "Username reset successfully.\nTry logging in");
        return "resetSuccess";
    }

    @GetMapping("/apply")
    public String apply(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("cgpa", studentRepo.findStudentById((Long) session.getAttribute("studentid")).getCGPA());
        List<Posting> postings = postingRepo.findPostingsByCompanyIdGreaterThanEqual(0l);
        List<Posting> finalPostings = new ArrayList<>();
        for(Posting posting : postings)
            if(notExpired(posting.getDeadline(), LocalDate.now())) finalPostings.add(posting);
        model.addAttribute("postings", finalPostings);
        return "apply";
    }

    @GetMapping("/viewallposting")
    public String viewpost(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("cgpa", studentRepo.findStudentById((Long) session.getAttribute("studentid")).getCGPA());
        List<Posting> postings = postingRepo.findPostingsByCompanyIdGreaterThanEqual(0l);
        List<Posting> finalPostings = new ArrayList<>();
        for(Posting posting : postings)
            if(notExpired(posting.getDeadline(), LocalDate.now())) finalPostings.add(posting);
        model.addAttribute("postings", finalPostings);
        return "viewallposting";
    }

    public boolean notExpired(LocalDate d1, LocalDate d2){
//        System.out.println(d1+", "+d2+", "+d1.compareTo(d2));
        if(d1.compareTo(d2)>=0) return true;
        return false;
    }

    @GetMapping("/viewapplied")
    public String viewapplied(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        List<Application> applications = applicationRepo.findApplicationsByStudentId((Long) session.getAttribute(
                "studentid"));
        List<Posting> postings = new ArrayList<>();
        for(Application application : applications)
            postings.add(postingRepo.findPostingById(application.getPostingId()));
        model.addAttribute("postings", postings);
        return "viewapplied";
    }


    public boolean isEligible(String s1, String s2, Long postingId, String studentId) {
        boolean great = true, applied = true;
        Double l1 = Double.parseDouble(s1);
        Double l2 = Double.parseDouble(s2);
        if (l1 >= l2)
            great = false;
        if(!applicationRepo.existsByPostingIdAndStudentId(postingId, Long.parseLong(studentId)))
            applied = false;
        return great || applied;
    }

    public boolean applied(Long postingId, String studentId){
        boolean applied = true;
        if(!applicationRepo.existsByPostingIdAndStudentId(postingId, Long.parseLong(studentId)))
            applied = false;
//        System.out.println(applied);
        return applied;
    }

    @GetMapping("/applyPost/{id}")
    public String showPost(@PathVariable Long id, Model model){
        Posting posting = postingRepo.findPostingById(id);
        model.addAttribute("posting", posting);
        model.addAttribute("application", new Application());
        return "applyPost";
    }

    @PostMapping("/applyPosting/{id}")
    public String applyPost(@PathVariable Long id, @ModelAttribute Application application,
                            HttpServletRequest request){
            application.setStudentId((Long)request.getSession().getAttribute("studentid"));
            application.setPostingId(id);
            applicationRepo.save(application);
            return "applicationSuccess";
    }

    @GetMapping("/editapplication")
    public String editapplication(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        List<Application> applications = applicationRepo.findApplicationsByStudentId((Long) session.getAttribute(
                "studentid"));
        List<Posting> postings = new ArrayList<>();
        for(Application application : applications){
            Posting posting = postingRepo.findPostingById(application.getPostingId());
            if(notExpired(posting.getDeadline(),LocalDate.now())) postings.add(posting);
        }
        model.addAttribute("postings", postings);
        return "editapplication";
    }

    @GetMapping("/updateApplication/{id}")
    public String updateApplication(@PathVariable Long id, Model model){
//        request.getSession().setAttribute("postId", id);
        Application application = applicationRepo.findApplicationByPostingId(id);
        model.addAttribute("application", application);
        model.addAttribute("postingId", id);
        return "updateApplication";
    }

    @PostMapping("/updateApplication/{id}")
    public String updateApplication(@PathVariable Long id, @ModelAttribute Application application,
                                    HttpServletRequest request){
        Long studentId = (Long) request.getSession().getAttribute("studentid");
        Application application1 = applicationRepo.findApplicationByPostingIdAndStudentId(id, studentId);
        application1.setReason(application.getReason());
        applicationRepo.save(application1);
        return "updateApplicationSuccess";
    }
}
