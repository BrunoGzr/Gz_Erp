ALTER TABLE users RENAME TO employees;

ALTER TABLE employees
    ADD salary DECIMAL(10,2) NOT NULL,
    ADD cpf VARCHAR(11) NOT NULL,
    ADD hire_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD roles varchar(1) DEFAULT NULL;

ALTER TABLE partners
    ADD password TEXT NOT NULL;
