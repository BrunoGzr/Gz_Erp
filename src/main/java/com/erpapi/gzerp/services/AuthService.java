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
        if (cpf == null || cpf.isEmpty()){
            throw new InvalidCredentialsException("Cpf is blank, please enter a valid one");
        }

        String numberOnly = cpf.replaceAll("[^0-9]","");

        if (numberOnly.length() != 11 || numberOnly.matches("(\\d)\\1{10}")) {
            throw new InvalidCredentialsException("Invalid CPF, please enter a valid one");
        }

        String[] cpfArray = numberOnly.split("");



        boolean firstDigit = digitValidatorCpf(cpfArray,"first");
        boolean secondDigit = digitValidatorCpf(cpfArray,"second");

        if (!firstDigit || !secondDigit ){
            throw new InvalidCredentialsException("Invalid CPF, please enter a valid one");
        } else {
            return true;
        }
    }

    private boolean digitValidatorCpf(String[] numbersCpf, String digit){

        if (numbersCpf == null || numbersCpf.length < 10 ){
            return false;
        }
        int sum = 0;
        int multiplyer = (digit.equals("first")) ? 10 : 11;
        int loop = (digit.equals("first") ? 8 :9 );
        int verify = (digit.equals("first") ? 9 : 10);
        for (int i = 0; i <= loop ;i++ ){
            sum = (Integer.parseInt(numbersCpf[i]) * multiplyer) + sum;
            multiplyer --;
        }
        int rest = sum % 11;

        int firstDigit = (rest< 2) ? 0 : (11- rest);

        return  firstDigit == Integer.parseInt(numbersCpf[verify]);

    }


}


