insert into summary
select *,
  timestampdiff(
    MINUTE,
    occurred_at,
    occurred_at_prev
  ) as elapsed_minutes,
  12742 * asin(
    sqrt(
      power(sin((latitude_prev - latitude) * 0.008725), 2) + cos(latitude * 0.01745) * cos(latitude_prev * 0.01745) * power(sin((longitude_prev - longitude) * 0.008725), 2)
    )
  ) as distance_traveled -- via haversine formula (in km)
from (
    select *,
      lag(occurred_at, 1) over (
        partition by tracking_number
        order by occurred_at
      ) as occurred_at_prev,
      lag(latitude, 1) over (
        partition by tracking_number
        order by occurred_at
      ) as latitude_prev,
      lag(longitude, 1) over (
        partition by tracking_number
        order by occurred_at
      ) as longitude_prev
    from parsed
  )