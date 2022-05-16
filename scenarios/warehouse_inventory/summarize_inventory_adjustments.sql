insert into summary
select window_start,
  window_end,
  reason_id,
  reason,
  count(1) as num_items,
  -- # of records received during tumble interval
  sum(quantity_adjusted) as total_quantity
from table (
    tumble(
      table extracted,
      descriptor(adj_timestamp),
      interval '5' minute
    )
  )
group by window_start,
  window_end,
  reason_id,
  reason