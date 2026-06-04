package com.erpapi.gzerp.resources;


import com.erpapi.gzerp.event.ResourceCreatedEvent;
import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.repositories.UsersRepo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/register")
public class RegisterUserResource {

    private final UsersRepo usersRepo;
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    public RegisterUserResource(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Users> registerUser(@Valid @RequestBody Users user, HttpServletResponse response) {
        Users savedUser = usersRepo.save(user);
        eventPublisher.publishEvent(new ResourceCreatedEvent(this,response, savedUser.getId()));
        return  ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<Users> userFound = usersRepo.findById(id);
        return userFound.isPresent() ? ResponseEntity.ok(userFound.get()) : ResponseEntity.notFound().build();




    }

}
