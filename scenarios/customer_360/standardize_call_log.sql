insert into transformed
select call_log.user_id as user_id,
  to_timestamp(call_log.start_time) as engagement_datetime,
  'sales call' as engagement_type,
  call_log.call_id as engagement_source_id,
  cast(call_log.call_time_seconds as int) as engagement_seconds
from (
    select -- parse XML to a DOM and extract fields using XPath expressions
      xpaths(
        xml,
        'user_id',
        '//call_log/user_id',
        'start_time',
        '//call_log/start_time',
        'call_id',
        '//call_log/call_id',
        'call_time_seconds',
        '//call_log/call_time_seconds'
      ) as call_log
    from `call_log`
  )