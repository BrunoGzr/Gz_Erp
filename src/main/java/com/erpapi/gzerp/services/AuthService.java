package com.erpapi.gzerp.services;


import com.erpapi.gzerp.exceptions.InvalidCredentialsException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class AuthService {

    public AuthService() {
    }

    public String cpfFormater(String cpf) throws InvalidCredentialsException {
        String numberOnly = cpf.replaceAll("[^0-9]","");
        String[] cpfArray = numberOnly.split("");

        if (cpfArray.length != 11) {
            throw new InvalidCredentialsException("Invalid cpf, please send a valid one reason: More then 11 digits");
        }

        StringBuilder formatedCpf = new StringBuilder();
        for (int i = 0; i < cpfArray.length; i++){
                if (i == 3 || i == 6){
                    formatedCpf.append(".");
                }
                if (i == 9){
                    formatedCpf.append("-");
                }
                formatedCpf.append(cpfArray[i]);
            }
        return formatedCpf.toString();
    }

    public String cnpjFormater(String cnpj) throws InvalidCredentialsException{
        String numberOnly = cnpj.replaceAll("[^0-9]","");
        String[] cnpjArray = numberOnly.split("");
        StringBuilder cnpjString = new StringBuilder();
        if (cnpjArray.length != 14) {
            throw new InvalidCredentialsException("Invalid Cnpj, please send a valid one reason: More/less then 14 digits");
        }

        for (int i = 0; i < 14 ; i++ ){

            if (i == 2 || i == 5 ) {
                cnpjString.append(".");
            }
            if (i == 8 ){
                cnpjString.append("/");
            }
            if (i == 12){
                cnpjString.append("-");
            }
            cnpjString.append(cnpjArray[i]);
        }

        return cnpjString.toString();
    }

    public void cpfValid(String cpf) throws InvalidCredentialsException {
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
        }
    }

    public void cnpjValid(String cnpj) throws InvalidCredentialsException{
        if (cnpj == null || cnpj.isEmpty() ){
            throw new InvalidCredentialsException("Cnpj is blank, please enter a valid one");
        }


        String numberOnly = cnpj.replaceAll("[^0-9]","");

        if (numberOnly.length() != 14 || numberOnly.matches("(\\d)\\1{13}")) {
            throw new InvalidCredentialsException("Invalid Cnpj , please enter a valid one");
        }

        String[] cnpjArray = numberOnly.split("");

        boolean first = digitValidatorCnpj(cnpjArray,"first");
        boolean second = digitValidatorCnpj(cnpjArray, "second");

        if (!first || !second ){
            throw new InvalidCredentialsException("Invalid cnpj, please enter a valid one");
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

    private boolean digitValidatorCnpj(String[] numbersCnpj,String digit){
        int sum = 0;
        int[] multiplayer = digit.equals("first")
                ? new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[] {6,5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int digitToValidate = digit.equals("first") ? 12 : 13;

        for (int i = 0; i < digitToValidate ; i++){
            sum += Integer.parseInt(numbersCnpj[i]) * multiplayer[i];
        }
        int cnpjDigit = ((sum % 11) < 2)? 0 : 11 -  (sum % 11) ;

        if (Integer.parseInt(numbersCnpj[digitToValidate]) == cnpjDigit){
            return true;
        }
        return false;
    }


}


