-- A new stream will be created for the output of this pipeline.
-- The stream name will match the name used in the 'insert' statement.
insert into customers_masked
select 
    id,
    first_name,
    last_name,
    email,
    gender,
    ip_address, 
    OVERLAY(CAST(cc as STRING) PLACING '*************' FROM 1 FOR 12) as cc
from customers