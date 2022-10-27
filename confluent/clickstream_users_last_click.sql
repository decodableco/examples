insert into customer_last_url_visit
select 
    c.userid,
    c.first_name,
    c.last_name,
    c.phone,
    e.ip,
    e.remote_user,
    e.`time`,
    cast(NOW() as timestamp) as _time,
    e.request,
    SPLIT_INDEX(e.request, ' ', 1) as url,
    e.status,
    e.`bytes`,
    e.referrer,
    e.agent
from clickstream_avro e
inner join customers_pg c on c.userid=e.userid