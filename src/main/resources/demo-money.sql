-- Add demo money to all accounts
UPDATE accounts SET balance = balance + 5000;

-- Or set specific amounts based on account type
UPDATE accounts 
SET balance = 
    CASE 
        WHEN account_type = 'SAVINGS' THEN balance + 10000
        WHEN account_type = 'CHECKING' THEN balance + 2500
        ELSE balance + 5000
    END;

-- Add demo transactions for the deposits
INSERT INTO transactions (from_account_id, to_account_id, amount, transaction_type, description, transaction_date, status) 
SELECT 
    NULL as from_account_id,
    id as to_account_id,
    5000 as amount,
    'DEPOSIT' as transaction_type,
    'Demo money deposit' as description,
    NOW() as transaction_date,
    'COMPLETED' as status
FROM accounts;