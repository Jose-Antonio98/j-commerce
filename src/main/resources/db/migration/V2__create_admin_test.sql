UPDATE users
SET role = 'ADMIN'
WHERE email = 'user@example.com';

SELECT email, role
FROM users
WHERE email = 'user@example.com';