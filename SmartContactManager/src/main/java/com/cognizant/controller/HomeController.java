package com.cognizant.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.cognizant.dao.UserRepository;
import com.cognizant.entity.User;
import com.cognizant.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	

	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","SignUp-Smart Contact Manager");
		model.addAttribute("user", new User());
		
		return "signup";
	}
	@RequestMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title","SignIn-Smart Contact Manager");
		
		
		return "login";
	}
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@ModelAttribute("user") User user, @RequestParam(value="agreement", defaultValue="false") boolean agreement, Model model,HttpSession session) {
	
		try {
			if(!agreement) {
				
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("Agreement "+agreement);
			System.out.println("USER "+user);
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message",new Message("Successfully Registered!!","alert-success"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", new User());
			session.setAttribute("message",new Message("Something went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
	}
	
}
