insert into moonsense_pointer_records
select -- each element of the `pointer_data` array creates a new record
  cast(pointer.determined_at as bigint) as determined_at,
  pointer.pos.dx as dx,
  pointer.pos.dy as dy,
  pointer.radius_major as radius,
  pointer.size as size,
  -- non-array fields common to each record are also included in the output
  app_id,
  session_id,
  user_id,
  server_time,
  wall_time,
  timer_millis,
  timer_realtime_millis
from moonsense_parsed
  cross join unnest(pointer_data) as pointer