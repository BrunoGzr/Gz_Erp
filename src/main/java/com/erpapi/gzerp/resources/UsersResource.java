package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.Exceptions.EmptyResultDataAccessExceptionCustom;
import com.erpapi.gzerp.models.Users;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.erpapi.gzerp.repositories.UsersRepo;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UsersResource {
    private final UsersRepo usersRepo;

    public UsersResource(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        List<Users> users = usersRepo.findAll();
        return !users.isEmpty() ? new ResponseEntity<>(users, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteUsers(@PathVariable Long id) {
        if  (usersRepo.existsById(id)) {
            usersRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        } else throw new EmptyResultDataAccessExceptionCustom("Data not found in database");
    }

}
