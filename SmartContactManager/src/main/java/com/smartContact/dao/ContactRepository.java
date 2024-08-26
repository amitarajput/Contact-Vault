package com.smartContact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	
	//now fetch data from contactrepo....use pagination method
	
	//create custom method
	// find contact by user call the give user id it will come to where clause when contact id match with given id then it will give list of contacts 
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);// pageable has 2 info, current page(variable page) and contact per page
	

	// for search
public List<Contact> findByNameContainingAndUser(String name, User user);
}