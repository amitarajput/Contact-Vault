package com.smartContact.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartContact.dao.UserRepository;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;




@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/")
	public String Home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "Home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup/")
	public String signup(Model model) {
		model.addAttribute("title", "signup - Smart Contact Manager");
		model.addAttribute("user", new User());// blank field will go and new field will come
		return "signup";
	}

	//this handle for registering user
	
	@RequestMapping(value = "/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value="agreement", defaultValue = "false") 
	boolean agreement, Model model,  HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			if(result1.hasErrors())
			{
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("defaultImage");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement " + agreement);
			System.out.println("USER" + user);
			
			User result = this.userRepository.save(user);// save data in database
			model.addAttribute("user", new User());// data shows in form columns
			session.setAttribute("message", new Message("User Successfully Registered!!", "alert-success"));
			return"signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!" + e.getMessage(), "alert-danger"));
			return"signup";
		}
		
		
	}

	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "login-page ");
		return "login";
	}
}

