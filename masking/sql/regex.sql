insert into new_output_stream
select 
    id,
    first_name,
    last_name,
    email,
    gender,
    REGEXP_REPLACE(ip_address, '\d', '*') fn_regex,
    cc
from customers