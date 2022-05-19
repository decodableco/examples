insert into UserGolden
select
  id as userid,
  first_name as firstname,
  last_name as lastname
from table UserApp1
group by window_start, window_end, path, status