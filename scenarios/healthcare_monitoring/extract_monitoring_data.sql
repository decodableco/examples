insert into parsed
select -- each element of the `values` array creates a new record
  -- assign each value type to its own field
  case
    when v.type = 'diastolic' then v.value
    else -1
  end as diastolic,
  case
    when v.type = 'systolic' then v.value
    else -1
  end as systolic,
  case
    when v.type = 'pulse' then v.value
    else -1
  end as pulse,
  case
    when v.type = 'irregular' then v.value
    else -1
  end as irregular,
  -- non-array fields common to each record are also included in the output
  patient_id,
  device_id,
  measurement_type,
  to_timestamp(measured_at, 'yyyy-MM-dd''T''HH:mm:ss.sss''Z''') as measured_at,
  case
    when signal_strength_percentage < 90 then 'CHECK SIGNAL'
    else 'GOOD SIGNAL'
  end as signal_status,
  case
    when battery_percentage > 80 then 'BATTERY GOOD'
    when battery_percentage > 50 then 'BATTERY OKAY'
    else 'BATTERY LOW'
  end as battery_status
from `device-raw`
  cross join unnest(`values`) as v