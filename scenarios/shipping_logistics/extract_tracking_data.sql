insert into parsed
select -- each element of the `events` array creates a new record
  to_timestamp(e.occurred_at, 'yyyy-MM-dd''T''HH:mm:ss''Z''') as occurred_at,
  e.description as description,
  e.city_locality as city_locality,
  e.state_province as state_province,
  e.postal_code as postal_code,
  e.country_code as country_code,
  e.latitude as latitude,
  e.longitude as longitude,
  -- non-array fields common to each record are also included in the output
  tracking_number,
  status_description,
  carrier_code,
  carrier_status_description
from `tracking-raw`
  cross join unnest(`events`) as e