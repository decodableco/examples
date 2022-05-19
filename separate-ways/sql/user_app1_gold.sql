insert into UserGolden
select
  cast(REGEXP_REPLACE(id,'\D', ''), integer),
  DATE(bday) as birth_date,
  fname as first_name,
  lname as last_name,
  address
from table UserApp1
group by window_start, window_end, path, status