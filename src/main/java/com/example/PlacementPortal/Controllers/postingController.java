package com.example.PlacementPortal.Controllers;

import com.example.PlacementPortal.Entities.Company;
import com.example.PlacementPortal.Entities.Posting;
import com.example.PlacementPortal.Repositories.companyRepository;
import com.example.PlacementPortal.Repositories.postingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;


@Controller
public class postingController {
    @Autowired
    postingRepository postingRepo;

    @Autowired
    companyRepository companyRepo;

    @GetMapping("/addposting")
    public String addpost(Model model){
        model.addAttribute("postingInstance",new Posting());
        return "addposting";
    }

    @PostMapping("/addposting")
    public String addpost(@ModelAttribute Posting postingInstance, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        Company company_session = companyRepo.findCompanyById((Long) session.getAttribute("companyid"));
        postingInstance.setCompanyId(company_session.getId());
        postingInstance.setCompanyName(company_session.getCompanyName());
        postingInstance.setCompanyEmail(company_session.getEmail());
        postingInstance.setPostedOn(java.time.LocalDate.now());
        String deadlineStr = postingInstance.getDeadlineStr();
//        System.out.println(deadlineStr);
        postingInstance.setDeadline(LocalDate.parse(deadlineStr));
        model.addAttribute("postingInstance",postingInstance);
        postingRepo.save(postingInstance);
        return "addSuccess";
    }

    @GetMapping("/viewposting")
    public String viewpost(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        List<Posting> postings = postingRepo.findPostingsByCompanyId((Long) session.getAttribute("companyid"));
        model.addAttribute("postings",postings);
        return "viewposting";
    }

    @GetMapping("/showPost/{id}")
    public String showPost(@PathVariable Long id, Model model){
        Posting posting = postingRepo.findPostingById(id);
        model.addAttribute("posting", posting);
        return "showPost";
    }

    @GetMapping("/updatePost/{id}")
    public String updatePost(@PathVariable Long id, Model model){
//        request.getSession().setAttribute("postId", id);
        Posting posting = postingRepo.findPostingById(id);
        model.addAttribute("posting", posting);
        return "updatePost";
    }

    @PostMapping("/updatePost")
    public String updatePost(@ModelAttribute Posting posting){
        posting.setPostedOn(java.time.LocalDate.now());
        posting.setDeadline(LocalDate.parse(posting.getDeadlineStr()));
        postingRepo.save(posting);
        return "updateSuccess";
    }
//
    @GetMapping("/editposting")
    public String editpost(Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        List<Posting> postings = postingRepo.findPostingsByCompanyId((Long) session.getAttribute("companyid"));
        model.addAttribute("postings",postings);
        return "editposting";
    }

//    @PostMapping("/editposting")
//    public String editpost(@ModelAttribute Posting posting, HttpServletRequest request){
//        return "success";
//    }
//
//    @GetMapping("/addposting")
//    private String addpost(Model model){
//        model.addAttribute("postingInstance",new Posting());
//        return "addposting";
//    }

}
