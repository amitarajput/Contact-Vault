package com.smartContact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartContact.dao.ContactRepository;
import com.smartContact.dao.UserRepository;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

	
	//we need user for add contact means which user is logged in create a method will run for index and add contact and all handler
	
	@ModelAttribute//(run for all handlers)
	public void addCommondata(Model model, Principal principal) {
		String userName = principal.getName();// you will see user name in console from which you loggedin
		System.out.println("USERNAME "+ userName);
		
		//get the user using user name(email) for this we need dao user repo
		User user =  userRepository.getUserByUserName(userName);
		System.out.println("USER" + user);
		
		//now send this user to user_dashboard.html file
		model.addAttribute("user", user);
		
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashBoard(Model model, Principal principal ) {
		model.addAttribute("title", "User DashBoard");
		
		return "normal/user_dashboard"; // after this... create this page go to template then normal then create .html file
	}
	
	//add contact form after logged in
	
	@GetMapping("/add_contact")
	public String opeAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		
		return "normal/add_contact_form";
	}
		
	//add contact form after adding all the data 
		
	@PostMapping("/process-contact")	// save data after adding contact
	public String processContact(@ModelAttribute Contact contact,// form field
			@RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session){// session for send alert messages
		try {
		  String name = principal.getName();// get the loggedin username
		  User user=this.userRepository.getUserByUserName(name); //get username from user repo
		  
//		  if(3>2)
//		  {
//			  throw new Exception();
//		  }
		 
		  //processing and uploading image when we add contact
		  //first check file is empty or not then try our message
		  // if photo is not uploaded then we add contact.png in show contacts
		  if(file.isEmpty())
		  {
			  System.out.println("File is Empty");
			  contact.setImage("contact.png");
		  }else {// if something in file then extract that file and upload into folder then update the name of the contact
			  contact.setImage(file.getOriginalFilename());
			  
			  File saveFile = new ClassPathResource("static/img").getFile();
			  
			  Path path =  Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			  
			  Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			  
			  System.out.println("Image is Uploaded");
			  
		  }
		  
		  
		  user.getContacts().add(contact);// will get list and add contact to that list
		  contact.setUser(user);// bidirectional mapping
		  
		  this.userRepository.save(user);// save data into db
		  
		System.out.println("Data" + contact);
		System.out.println("Added to data base");
		//message success alert after adding the contact
		session.setAttribute("message", new Message("Your friend is added!! Add more friends..", "success"));
			
		}catch (Exception e) {
			e.printStackTrace();
			////message not success alert means danger after adding the contact
			session.setAttribute("message", new Message("Something Went Wrong!! Try Again..", "danger"));
		}
			return "normal/add_contact_form";

}
	//now creating view contacts page 
	
	@GetMapping("/show_contacts/{page}")// for page numbers
	public String showContacts(@PathVariable("page") Integer page , Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		//create page objects after contact repo
		
	Pageable pageable = PageRequest.of(page, 5);
		
	  Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable );
	  model.addAttribute("contacts", contacts);
	  model.addAttribute("currentPage", page);
	  
	  //total pages after break the pages
	  model.addAttribute("totalPages", contacts.getTotalPages());
			
		return "normal/show_contacts";
		
	}
	
	//showing handler when we click on email of any contact in show contacts
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		System.out.println("CID" +cId);
		Optional<Contact> contactOptional= this.contactRepository.findById(cId);// when click on email will show only that user data
		Contact contact = contactOptional.get();
		
		//add security so that another user cant access contact details of logged in user
		
		String userName =principal.getName();// get username
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId())
		{
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());// will see contact name into browser title
		}
		return "normal/contact_detail";
	}
	
	//delete contact form show contacts 
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session, Principal principal) {
   
		System.out.println("CID" +cId);
		
		 Contact contact = this.contactRepository.findById(cId).get();
	      System.out.println("Contact "+contact.getcId());
	      
//	      contact.setUser(null);// if not delete then unlink it and set it null but it will not delete the data from database
//	      this.contactRepository.delete(contact);
	      
	      //how to delete data from database also
	      
	      User user = this.userRepository.getUserByUserName(principal.getName());//get user
	      user.getContacts().remove(contact);// get contacts form user then remove contact which we want
	      
	      //then remove contact from list
	      this.userRepository.save(user);
	      
	      
	      session.setAttribute("message", new Message("Contact Deleted Successfully...", "success"));
		return "redirect:/user/show_contacts/0";
		
	}
	
	//update contact form show contacts for that we need to create open update handler
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m)// model use to send data here
	{
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return"normal/update_form";
	}
	
	//Save button click after updating 
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, HttpSession session, Principal principal) {
		
		try {
			//get old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				//delete old photo it shows in server
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
						
				//upload new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				   Path path =  Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				   Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
				  
				  contact.setImage(file.getOriginalFilename()); // add photo in contact database
				
			}else {
				contact.setImage(oldContactDetails.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());//extract current user
			contact.setUser(user);// save user in contact
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			//session.setAttribute("message", new Message("An unexpected error occured", "danger"));
		}
		System.out.println("Contact name" +contact.getName());
		
		return "redirect:/user/" +contact.getcId()+"/contact";
	}
	
	//method for your profile handler
	
	@GetMapping("/profile")//user./profile
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
		
		//settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	//change password handler
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		System.out.println("OLD PASSWORD" + oldPassword);
		System.out.println("NEW PASSWORD" + newPassword);
		
		//now check old password match with new password first get user(principal)
		String userName = principal.getName();
	    User currentUser= this.userRepository.getUserByUserName(userName);
	    System.out.println(currentUser.getPassword());
	    
	    if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())){
	    	//change password
	    	currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
	    	this.userRepository.save(currentUser);
	    	session.setAttribute("message", new Message("Your password is successfully changed", "success"));
	    	
	    }else {
	    	session.setAttribute("message", new Message("Your old password is wrong", "danger"));
	    	return"redirect:/user/settings";
	    	
	    }
		
	
		return"redirect:/user/index";
	}
	//for about
	@RequestMapping("/about")
	public String aboutPage(Model model, Principal principal ) {
		model.addAttribute("title", "About DashBoard");
		
		return "normal/about"; // after this... create this page go to template then normal then create .html file
	}
	
}
