insert into summary
select patient_id,
  device_id,
  measurement_type,
  measured_at,
  max(diastolic) as diastolic,
  max(systolic) as systolic,
  max(pulse) as pulse,
  max(irregular) as irregular,
  signal_status,
  battery_status
from parsed
group by patient_id,
  device_id,
  measurement_type,
  measured_at,
  signal_status,
  battery_status