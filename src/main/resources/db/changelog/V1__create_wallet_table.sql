CREATE TABLE wallet (
                        id UUID PRIMARY KEY,
                        balance NUMERIC(19,2) NOT NULL
);

INSERT INTO wallet(id, balance)
VALUES
    ('11111111-1111-1111-1111-111111111111', 5000.00),
    ('22222222-2222-2222-2222-222222222222', 1000.00);