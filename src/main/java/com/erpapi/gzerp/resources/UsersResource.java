package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.models.Users;
import com.erpapi.gzerp.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    public UsersResource(UsersRepo usersRepo, UsersService usersService) {
        this.usersRepo = usersRepo;
        this.usersService = usersService;
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

    @PutMapping("/{id}")
    public ResponseEntity<?> UpdateUsers(@PathVariable Long id, @Valid @RequestBody Users user){


        return usersService.UpdateUsers(user,id);
    }



}


