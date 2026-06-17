package com.erpapi.gzerp.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateReceivedDto {

    @Size(min = 3, max = 20)
    private String userName;


    private String email;

    @Size(min = 3, max = 100)
    private String name;
    private String password;

}
