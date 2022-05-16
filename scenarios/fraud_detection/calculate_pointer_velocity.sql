insert into moonsense_pointer_velocity
select session_id,
  server_time,
  determined_at,
  dx,
  dy,
  size,
  t_delta,
  d_delta,
  case
    when t_delta is null then 0
    when t_delta = 0 then 0
    else d_delta / t_delta
  end as velocity
from (
    select *,
      abs(determined_at - determined_at_prev) as t_delta,
      sqrt(power(dx - dx_prev, 2) + power(dy - dy_prev, 2)) as d_delta
    from (
        select *,
          lag(dx, 1) over (
            partition by session_id
            order by server_time
          ) as dx_prev,
          lag(dy, 1) over (
            partition by session_id
            order by server_time
          ) as dy_prev,
          lag(determined_at, 1) over (
            partition by session_id
            order by server_time
          ) as determined_at_prev
        from moonsense_pointer_records
      )
  )