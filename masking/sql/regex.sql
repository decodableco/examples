insert into customers_masked
select 
    id,
    first_name,
    last_name,
    email,
    gender,
    REGEXP_REPLACE(ip_address, '\d', '*') ip_address,
    'removed' as cc
from customers