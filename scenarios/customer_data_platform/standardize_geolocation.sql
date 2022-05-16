insert into transformed
select advertising_id as user_id,
  to_timestamp(`timestamp`) as engagement_datetime,
  'geolocation' as engagement_type,
  device_id as engagement_source_id,
  location_guid as location_id
from `geolocation`