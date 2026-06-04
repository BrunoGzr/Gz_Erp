CREATE TABLE users(
    id BIGINT UNSIGNED primary key auto_increment,
    tenant_id int default 0,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(20) NOT NULL,
    register_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE
)ENGINE= InnoDB DEFAULT CHARSET=UTF8;






