insert into status
select *,
  floor(
    (branch_distance - driver_distance) / branch_distance * 100
  ) as percent_complete
from (
    select order_id,
      branch_id,
      customer_id,
      state_human_readable,
      notification,
      earliest,
      latest,
      case
        when coalesce(dispatched_at, '') = '' then 1
        when coalesce(completed_at, '') <> '' then 1
        else 12742 * asin(
          sqrt(
            power(sin((branch_lat - customer_lat) * 0.008725), 2) + cos(customer_lat * 0.01745) * cos(branch_lat * 0.01745) * power(sin((branch_lon - customer_ lon) * 0.008725), 2)
          )
        )
      end as branch_distance,
      case
        when coalesce(timestamps.dispatched_at, '') = '' then 1
        when coalesce(timestamps.completed_at, '') <> '' then 0
        else 12742 * asin(
          sqrt(
            power(sin((driver_lat - customer_lat) * 0.008725), 2) + cos(customer_lat * 0.01745) * cos(driver_lat * 0.01745) * power(sin((driver_lon - customer_lon) * 0.008725), 2)
          )
        )
      end as driver_distance
    from parsed
  )