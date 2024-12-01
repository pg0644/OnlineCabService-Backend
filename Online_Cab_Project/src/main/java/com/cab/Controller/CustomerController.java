package com.cab.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cab.Exception.CurrentUserSessionException;
import com.cab.Exception.CustomerException;
import com.cab.Model.Customer;
import com.cab.Service.CustomerService;

@RestController
@CrossOrigin()
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private CustomerService customerService;
	
	@PostMapping("/register")
	public ResponseEntity<Customer> registerCustomer(@RequestBody Customer customer) throws CustomerException{
		return new ResponseEntity<Customer>(customerService.insertCustomer(customer),HttpStatus.CREATED);
	}
	
	@PutMapping("/update")
	public ResponseEntity<Customer> updateCustomer(@RequestBody Customer customer, @RequestParam("uuid") String uuid) throws CustomerException, CurrentUserSessionException{
		return new ResponseEntity<Customer>(customerService.updateCustomer(customer, uuid),HttpStatus.OK);
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<Customer> deleteCustomer(@RequestParam("customerId") Integer customerId,@RequestParam("uuid") String uuid) throws CustomerException, CurrentUserSessionException{
		return new ResponseEntity<Customer>(customerService.deleteCustomer(customerId, uuid),HttpStatus.OK);
	}
	
	@GetMapping("/viewAllCustomer")
	public ResponseEntity<List<Customer>> viewCustomer(@RequestParam("uuid") String uuid) throws CustomerException, CurrentUserSessionException{
		return new ResponseEntity<List<Customer>>(customerService.viewCustomer(uuid),HttpStatus.OK);
	}
	
	@GetMapping("/viewCustomer")
	public ResponseEntity<Customer> viewCustomer(@RequestParam("customerId") Integer customerId,@RequestParam("uuid") String uuid) throws CustomerException, CurrentUserSessionException{
		return new ResponseEntity<Customer>(customerService.viewCustomer(customerId, uuid),HttpStatus.OK);
	}
}
