insert into coinbase_transaction_summary
select *,
  price_min - price_min_prev as price_min_change,
  price_max - price_max_prev as price_max_change,
  price_avg - price_avg_prev as price_avg_change
from (
    select *,
      lag(price_min, 1) over (
        order by window_time
      ) as price_min_prev,
      lag(price_max, 1) over (
        order by window_time
      ) as price_max_prev,
      lag(price_avg, 1) over (
        order by window_time
      ) as price_avg_prev
    from (
        select window_start,
          window_end,
          window_time,
          product_id,
          side,
          count(1) as volume,
          -- # records received during tumble interval
          min(price) as price_min,
          max(price) as price_max,
          avg(price) as price_avg
        from table (
            tumble(
              table coinbase_exchange_transactions,
              descriptor(tx_time),
              interval '5' second
            )
          )
        where product_id = 'BTC-USD'
          and side = 'buy'
        group by window_start,
          window_end,
          window_time,
          product_id,
          side
      )
  )