package com.cognizant.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.cognizant.dao.ContactRepository;
import com.cognizant.dao.UserRepository;
import com.cognizant.entity.Contact;
import com.cognizant.entity.User;
import com.cognizant.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		

		String userName = principal.getName();
		System.out.println("USERNAME "+userName);
		
		//get user by using username(email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER "+user);
		model.addAttribute("user",user);
		
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	//Open add form Handler
	@GetMapping("/add-contact")
	public String OpenAddFormHandler(Model model) {
		
		model.addAttribute("title","add-contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//process add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				//if the file is empty try our message
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}
			else {
				
				//fill the file to folder and update the name to the contact
				
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File is uploaded");
				
				
				
			}
			contact.setUser(user);
			user.getContact().add(contact);
			this.userRepository.save(user);
			
			//Message success
			session.setAttribute("message", new Message("Your contact added successfully!! want to add more?","success"));
			
		}catch(Exception e){
			System.out.println("Error"+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong!!! Try again","danger"));
		}
		return "normal/add_contact_form";
	}
	//show contact handler
	// per page = 5[n]
	// current page = 0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "show contacts");
		
		//to send list of contacts
		String userName  = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		//CurrentPage- page
		//contact per page - 5
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		return "normal/show_contacts";
	}
	//show particular contact
	
	@RequestMapping("/{cId}/contact")	
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model,Principal principal) {
		
		System.out.println("CID "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId() == contact.getUser().getId()) {
			
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
			
		}
		return "normal/contact_detail";
	}
	
	//delete specific contact
	@GetMapping("/delete/{cid}")
	public String deletecontact(@PathVariable("cid") Integer cId,
			Model model,HttpSession session, Principal principal) {
		
		Contact contact = this.contactRepository.findById(cId).get();
//		Contact contact = optionalContact.get();
//		contact.setUser(null);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		user.getContact().remove(contact);
		this.userRepository.save(user);
		//remove image
		//contact.getImage()
//		this.contactRepository.delete(contact);
		session.setAttribute("message", new Message("Contact deleted successfully...","success"));
		return "redirect:/user/show-contacts/0";
	}
	//update form handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cId, Model model) {
		
		model.addAttribute("title","update-contact");
		Contact contact = this.contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		return "normal/update-contact";
	}
	
	//update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,Model m,
			HttpSession session, Principal principal ) {
		
		try {
			
			//old Contact Details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				//work
				//rewrite
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/images").getFile();
				File file1 = new File(deleteFile,oldContactDetail.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/images").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact updated.... ", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CONTACT "+contact.getcId());
		System.out.println("Name "+contact.getName());
		return "redirect:/user/"+contact.getcId()+"/contact";
		
	}

}
