CREATE TABLE IF NOT EXISTS tb_user (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    user_type_code INTEGER NOT NULL,
    cpf_cnpj VARCHAR(14) UNIQUE NOT NULL,
    email VARCHAR(320) UNIQUE NOT NULL,
    password VARCHAR(70) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_full_name ON tb_user(full_name);
CREATE INDEX IF NOT EXISTS idx_user_email ON tb_user(email);
CREATE INDEX IF NOT EXISTS idx_user_cpf_cnpj ON tb_user(cpf_cnpj);