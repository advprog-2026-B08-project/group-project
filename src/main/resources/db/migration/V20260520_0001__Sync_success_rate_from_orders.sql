UPDATE users u SET tried_to_sell = (
    SELECT COUNT(*) FROM orders o WHERE o.jastiper_id = u.id
) WHERE u.role = 'ROLE_JASTIPER';

UPDATE users u SET successfully_sold = (
    SELECT COUNT(*) FROM orders o WHERE o.jastiper_id = u.id AND o.status = 'COMPLETED'
) WHERE u.role = 'ROLE_JASTIPER';
