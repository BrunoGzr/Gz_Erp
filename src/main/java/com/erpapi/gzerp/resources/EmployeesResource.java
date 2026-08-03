package com.erpapi.gzerp.resources;

import com.erpapi.gzerp.dto.EmployeeRegisterDto;
import com.erpapi.gzerp.dto.EmployeeResponseDto;
import com.erpapi.gzerp.models.Employees;
import com.erpapi.gzerp.services.EmployeesService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.erpapi.gzerp.repositories.EmployeesRepo;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeesResource {

    private final EmployeesRepo employeesRepo;
    private final EmployeesService employeesService;

    public EmployeesResource(EmployeesRepo employeesRepo, EmployeesService employeesService, ApplicationEventPublisher eventPublisher) {
        this.employeesRepo = employeesRepo;
        this.employeesService = employeesService;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EmployeeResponseDto> registerUser(@Valid @RequestBody EmployeeRegisterDto userDto, HttpServletResponse response) {
        EmployeeResponseDto savedUser = employeesService.RegisterUserWithTenantCreated(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @GetMapping
    public ResponseEntity<?> getUsers() {
        List<Employees> users = employeesRepo.findAll();
        return !users.isEmpty() ? new ResponseEntity<>(users, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUsers(@PathVariable Long id) {
      if  (employeesRepo.existsById(id)) {
           employeesRepo.deleteById(id);
           return ResponseEntity.status(HttpStatus.ACCEPTED).body("User '" + id + "' deleted successfully");
       }
        return ResponseEntity.notFound().build();
    }




}


