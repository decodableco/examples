insert into extracted
select -- each element of the `inventory_requests` array has fields shared by every line item
  request.adjustment.id as adj_id,
  request.reference_number as adj_reference,
  to_timestamp(`request.adjustment.datetime`) as adj_timestamp,
  request.adjustment.reason_id as reason_id,
  request.adjustment.reason as reason,
  -- for each request, each element of the `line_items` array creates a new record
  line_item.id as line_item_id,
  line_item.item_id as item_id,
  line_item.quantity_adjusted as quantity_adjusted,
  line_item.warehouse_id as warehouse_id
from (
    select request.*
    from `inventory-raw`
      cross join unnest(`inventory_requests`) as request
  )
where request.adjustment.reason_id = 2515
  cross join unnest(`request.adjustment.line_items`) as line_item