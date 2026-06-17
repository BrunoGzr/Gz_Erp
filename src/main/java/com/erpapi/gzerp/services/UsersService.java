package com.erpapi.gzerp.services;

import com.erpapi.gzerp.Exceptions.UserAlreadyExistException;
import com.erpapi.gzerp.dto.UserCreateDto;
import com.erpapi.gzerp.dto.UserResponseDto;
import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.repositories.UsersRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {

    @Autowired
    private UsersRepo usersRepo;

//    public ResponseEntity<?> UpdateUsers(Users userToUpdated, Long id) {
//
//    }

    public UserResponseDto RegisterUser(UserCreateDto userDto) throws UserAlreadyExistException {
        List<String> conflictedFields = new ArrayList<String>();
        if (usersRepo.existsByEmail(userDto.getEmail(), userDto.getTenantId())) {
            conflictedFields.add("User with this email already exists");}
        if (usersRepo.existsByUserName(userDto.getUserName(), userDto.getTenantId())) {
            conflictedFields.add("User with this username already exists");}
        if (!conflictedFields.isEmpty()) {
            throw new UserAlreadyExistException(conflictedFields);}

        Users newUser = new Users();
        newUser.setEmail(userDto.getEmail());
        newUser.setTenantId(userDto.getTenantId());
        newUser.setUserName(userDto.getUserName());
        newUser.setPassword(userDto.getPassword());
        newUser.setName(userDto.getName());
        newUser.setAdmin(false);

        Users savedUser = usersRepo.save(newUser);
        return new UserResponseDto(savedUser);
    }

}