package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.UserCreateDto;
import com.erpapi.gzerp.dto.UserResponseDto;
import com.erpapi.gzerp.event.ResourceCreatedEvent;
import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.repositories.UsersRepo;
import com.erpapi.gzerp.services.UsersService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/register")
public class RegisterResource {

    private final UsersRepo usersRepo;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private final UsersService usersService;


    public RegisterResource(UsersRepo usersRepo, UsersService usersService) {
        this.usersRepo = usersRepo;
        this.usersService = usersService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto userDto, HttpServletResponse response) {
        UserResponseDto savedUser = usersService.RegisterUser(userDto);
        eventPublisher.publishEvent(new ResourceCreatedEvent(this, response, savedUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        Optional<Users> userFound = usersRepo.findById(id);
        return userFound.isPresent() ? ResponseEntity.ok(userFound.get()) : ResponseEntity.notFound().build();
    }
}
