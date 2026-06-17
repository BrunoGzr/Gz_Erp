package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.UserCreateDto;
import com.erpapi.gzerp.dto.UserResponseDto;
import com.erpapi.gzerp.event.ResourceCreatedEvent;
import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.services.UsersService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.erpapi.gzerp.repositories.UsersRepo;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersResource {

    private final UsersRepo usersRepo;
    private final UsersService usersService;
    private final ApplicationEventPublisher eventPublisher;

    public UsersResource(UsersRepo usersRepo, UsersService usersService, ApplicationEventPublisher eventPublisher) {
        this.usersRepo = usersRepo;
        this.usersService = usersService;
        this.eventPublisher = eventPublisher;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto userDto, HttpServletResponse response) {
        UserResponseDto savedUser = usersService.RegisterUser(userDto);
        eventPublisher.publishEvent(new ResourceCreatedEvent(this, response, savedUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        List<Users> users = usersRepo.findAll();
        return !users.isEmpty() ? new ResponseEntity<>(users, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUsers(@PathVariable Long id) {
      if  (usersRepo.existsById(id)) {
           usersRepo.deleteById(id);
           return ResponseEntity.status(HttpStatus.ACCEPTED).body("User '" + id + "' deleted successfully");
       }
        return ResponseEntity.notFound().build();
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<?> UpdateUsers(@PathVariable Long id, @Valid @RequestBody Users user){
//
//
//        return usersService.UpdateUsers(user,id);
//    }



}


