ALTER TABLE users_accounts
    ADD CONSTRAINT uk_user_accounts_email UNIQUE (email),
    ADD CONSTRAINT uk_user_accounts_username UNIQUE (username);

