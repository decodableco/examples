insert into clickstream_change
select 
    UUID() as id,
    ip,
    userid,
    remote_user,
    `time`,
    NOW() as _time,
    request,
    status,
    `bytes`,
    referrer,
    agent
from clickstream_append
