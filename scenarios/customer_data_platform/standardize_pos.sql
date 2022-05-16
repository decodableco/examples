insert into transformed
select payer_id as user_id,
  to_timestamp(`created_at`, 'yyyy-MM-dd''T''HH:mm:ss.SSS''Z''') as engagement_datetime,
  'pos-terminal' as engagement_type,
  terminal_id as engagement_source_id,
  terminal_location as location_id
from `pos-terminal`