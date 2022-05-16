insert into coinbase_exchange_transactions
select to_timestamp(`time`, 'yyyy-MM-dd''T''HH:mm:ss.SSSSSS''Z''') as tx_time,
  `type` as tx_type,
  cast(sequence as bigint) as sequence,
  product_id,
  cast(price as decimal(10, 2)) as price,
  cast(open_24h as decimal(10, 2)) as open_24h,
  cast(volume_24h as decimal(10, 2)) as volume_24h,
  cast(low_24h as decimal(10, 2)) as low_24h,
  cast(high_24h as decimal(10, 2)) as high_24h,
  cast(volume_30d as decimal(10, 2)) as volume_30d,
  cast(best_bid as decimal(10, 2)) as best_bid,
  cast(best_ask as decimal(10, 2)) as best_ask,
  side,
  cast(trade_id as bigint) as trade_id,
  cast(last_size as bigint) as last_size
from `coinbase-btc-usd-ticker`