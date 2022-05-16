insert into summary
select window_start,
  window_end,
  site_id,
  user_id,
  count(1) as pages_visited,
  sum(engagement.seconds_on_data) as total_seconds_on_site,
  avg(engagement.percent_viewed) as avg_percent_viewed,
  from table (
    tumble(
      table clickstream,
      descriptor(to_timestamp(event_datetime)),
      interval '1' hour
    )
  )
group by window_start,
  window_end,
  site_id,
  user_id