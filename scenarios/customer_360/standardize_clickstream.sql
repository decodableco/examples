insert into transformed
select user_id,
  to_timestamp(event_timestamp) as engagement_datetime,
  'website' as engagement_type,
  site_id as engagement_source_id,
  total_seconds_on_site as engagement_seconds
from `clickstream`