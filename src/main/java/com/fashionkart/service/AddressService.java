package com.fashionkart.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fashionkart.entity.Address;
import com.fashionkart.entity.User;
import com.fashionkart.repository.AddressRepository;

@Service
public class AddressService {
	
	private final AddressRepository addressRepository;
	
	public AddressService(AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
		// TODO Auto-generated constructor stub
	}
	
	public Address saveAddress(Address address) {
		return addressRepository.save(address);
	}
	
	public List<Address> getuserAddresses(User user){
		return addressRepository.findByUser(user);
	}
	
	public Address updateAddress(Address address, User user) {
		Address existingAddress = addressRepository.findById(address.getId()).
				orElseThrow(() -> new RuntimeException("Address not found"));
		
		if(!existingAddress.getUser().getId().equals(user.getId())) {
			throw new RuntimeException("You are not allowed to update this address");
		}
		
		existingAddress.setAddressType(address.getAddressType());
		existingAddress.setFullName(address.getFullName());
		existingAddress.setPhone(address.getPhone());
		existingAddress.setAddress(address.getAddress());
		existingAddress.setCity(address.getCity());
		existingAddress.setState(address.getState());
		existingAddress.setPincode(address.getPincode());
		existingAddress.setDefaultaddress(address.isDefaultaddress());
		
		return addressRepository.save(existingAddress);
	}
	
	public void deleteAddress(Long addressId, User user) {
		
		Address existingAddress = addressRepository.findById(addressId).
				orElseThrow(() -> new RuntimeException("Address not found"));
		
		if (!existingAddress.getUser().getId().equals(user.getId())) {
	        throw new RuntimeException("You are not allowed to delete this address");
	    }
		
		addressRepository.delete(existingAddress);
	}
}
