insert into clickstream_users_last_click
select 
    u.userid as userid,
	u.first_name as first_name,
	u.last_name as last_name,
	u.phone as phone,
	c.ip as ip,
	c.remote_user as remote_user,
	c.`time` as `time`,
	c._time as _time,
	c.request as request,
	c.status as status,
	c.`bytes` as `bytes`,
	c.referrer as referrer,
	c.agent as agent
from clickstream_change c
join users u on c.userid=u.userid
