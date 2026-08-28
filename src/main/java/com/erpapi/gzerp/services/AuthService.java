package com.erpapi.gzerp.services;


import com.erpapi.gzerp.exceptions.InvalidCredentialsException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class AuthService {

    private ArrayList<String> chars;

    public AuthService() {
        this.chars = new ArrayList<>(Arrays.asList("1","2","3","4","5","6","7","8","9","0"));

    }

    public String cpfFormater(String cpf) throws InvalidCredentialsException {
        String numberOnly = cpf.replaceAll("[^0-9]","");
        String[] cpfArray = numberOnly.split("");

        if (cpfArray.length != 11) {
            throw new InvalidCredentialsException("Invalid cpf, please send a valid one reason: More then 11 digits");
        }

        StringBuilder formatedCpf = new StringBuilder();
        for (int i = 0; i < cpfArray.length; i++){
            if (chars.contains(cpfArray[i])){
                if (i == 3 || i == 6){
                    formatedCpf.append(".");
                }
                if (i == 9){
                    formatedCpf.append("-");
                }
                formatedCpf.append(cpfArray[i]);
            }
        }
        return formatedCpf.toString();
    }

    public boolean cpfValid(String cpf) throws InvalidCredentialsException {
        String numberOnly = cpf.replaceAll("[^0-9]","");
        String[] cpfArray = numberOnly.split("");
        



    }
}
