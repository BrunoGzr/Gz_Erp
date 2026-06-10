package com.erpapi.gzerp.services;

import com.erpapi.gzerp.Exceptions.UserAlreadyExistException;
import com.erpapi.gzerp.dto.UserCreateDto;
import com.erpapi.gzerp.dto.UserResponseDto;
import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.repositories.UsersRepo;
import jakarta.transaction.TransactionScoped;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {

    @Autowired
    private UsersRepo usersRepo;

    public ResponseEntity<?> UpdateUsers(Users userToUpdated, Long id) {
        Users savedUser = usersRepo.findById(id).orElse(null);
        if ( savedUser == null) {
            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "User with id '" + id + "' not found");
            problemDetail.setTitle("User not found");
            problemDetail.setProperty("id = ", id);
            problemDetail.setProperty("timestamp = ", Instant.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
        }
        BeanUtils.copyProperties(userToUpdated, savedUser,"id");
        usersRepo.save(savedUser);
        return ResponseEntity.status(HttpStatus.OK).body("User with id '" + id + "' successfully updated");
    }

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