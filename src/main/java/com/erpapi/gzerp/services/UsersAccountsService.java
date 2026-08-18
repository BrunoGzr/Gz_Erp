package com.erpapi.gzerp.services;


import com.erpapi.gzerp.repositories.UsersAccountRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersAccountsService {

    private final UsersAccountRepo usersRepo;

    public UsersAccountsService(UsersAccountRepo usersRepo) {
        this.usersRepo = usersRepo;
    }


}
