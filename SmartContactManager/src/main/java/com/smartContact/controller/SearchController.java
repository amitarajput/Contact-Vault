package com.smartContact.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartContact.dao.ContactRepository;
import com.smartContact.dao.UserRepository;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

@RestController
public class SearchController {
	
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private UserRepository userRepository;
	
	
	@GetMapping(value = "/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal)
	{
		System.out.println(query);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
	  List<Contact> contacts	= this.contactRepository.findByNameContainingAndUser(query, user);
	  return ResponseEntity.ok(contacts);
	}

}
