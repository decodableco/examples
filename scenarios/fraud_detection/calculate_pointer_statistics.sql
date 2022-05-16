insert into moonsense_pointer_stats
select window_start,
  window_end,
  session_id,
  count(1) as record_count,
  -- # records received during tumble interval
  -- calculate a distribution matrix for the pointer velocities
  min(inst_velocity) as velocity_p0,
  approx_percentile(inst_velocity, 0.25) as velocity_p25,
  approx_percentile(inst_velocity, 0.5) as velocity_p50,
  approx_percentile(inst_velocity, 0.75) as velocity_p75,
  max(inst_velocity) as velocity_p100,
  avg(inst_velocity) as velocity_mean,
  sum(d_delta) as total_distance,
  sum(t_delta) as total_duration,
  sum(d_delta) / sum(t_delta) as total_velocity
from table (
    tumble(
      table moonsense_pointer_velocity,
      descriptor(server_time),
      interval '10' seconds
    )
  )
group by window_start,
  window_end,
  session_id