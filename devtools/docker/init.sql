CREATE TABLE IF NOT EXISTS public."order" (
                                              id SERIAL PRIMARY KEY,
                                              customer_name VARCHAR(100) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL
    );

INSERT INTO public."order" (customer_name, amount) VALUES
                                                       ('Alice', 100.00),
                                                       ('Bob', 250.50),
                                                       ('Charlie', 75.25);