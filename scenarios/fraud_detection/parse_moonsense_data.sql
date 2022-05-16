insert into moonsense_parsed
select app_id,
  session_id,
  user_id,
  to_timestamp_ltz(cast(left(server_time_millis, 13) as bigint), 3) as server_time,
  to_timestamp_ltz(cast(client_time.wall_time_millis as bigint), 3) as wall_time,
  cast(client_time.timer_millis as bigint) as timer_millis,
  cast(client_time.timer_realtime_millis as bigint) as timer_realtime_millis,
  bundle.location_data as location_data,
  bundle.accelerometer_data as accelerometer_data,
  bundle.magnetometer_data as magnetometer_data,
  bundle.gyroscope_data as gyroscope_data,
  bundle.battery as battery,
  bundle.activity_data as activity_data,
  bundle.orientation_data as orientation_data,
  bundle.pointer_data as pointer_data
from moonsense_raw