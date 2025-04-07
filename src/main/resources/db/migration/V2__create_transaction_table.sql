CREATE TABLE IF NOT EXISTS tb_transaction (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    payer_id UUID NOT NULL,
    payee_id UUID NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payer FOREIGN KEY (payer_id) REFERENCES tb_user(id),
    CONSTRAINT fk_payee FOREIGN KEY (payee_id) REFERENCES tb_user(id)
);