package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    AuthUtil authUtil;


    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {


        Address address = modelMapper.map(addressDTO, Address.class);

        List<Address> addressList = user.getAddresses();

        addressList.add(address);

        user.setAddresses(addressList);

        address.setUser(user);

        Address savedAddress = addressRepository.save(address);


        return modelMapper.map(savedAddress, AddressDTO.class);


    }

    @Override
    public List<AddressDTO> getAddresses() {

        List<Address> addresses = addressRepository.findAll();


        return addresses.stream()
                .map(myadddress -> modelMapper.map(myadddress, AddressDTO.class))
                .toList();

    }


    @Override
    public AddressDTO getAddressById(Long addressId) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));


        return modelMapper.map(address, AddressDTO.class);

    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {

        //note here
        List<Address> addresses = user.getAddresses();


        return addresses.stream()
                .map(myadddress -> modelMapper.map(myadddress, AddressDTO.class))
                .toList();


    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {


        Address addressFromDatabase = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));


        addressFromDatabase.setCity(addressDTO.getCity());


        addressFromDatabase.setPincode(addressDTO.getPincode());

        addressFromDatabase.setState(addressDTO.getState());

        addressFromDatabase.setCountry(addressDTO.getCountry());


        addressFromDatabase.setStreet(addressDTO.getStreet());


        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());


        //note here
        Address updatedAddress = addressRepository.save(addressFromDatabase);


        User user = addressFromDatabase.getUser();

        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));

        user.getAddresses().add(updatedAddress);

        userRepository.save(user);


        return modelMapper.map(updatedAddress, AddressDTO.class);
    }
}
