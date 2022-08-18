insert into crypto_changes
select 
    crypto_raw_table.id,
    crypto_raw_table.currency,
    crypto_raw_table.symbol,
    crypto_raw_table.name,
    crypto_raw_table.logo_url,
    crypto_raw_table.status,
    crypto_raw_table.price,
    crypto_raw_table.price_date,
    crypto_raw_table.price_timestamp,
    crypto_raw_table.circulating_supply,
    crypto_raw_table.max_supply,
    crypto_raw_table.market_cap_dominance,
    crypto_raw_table.high,
    crypto_raw_table.high_timestamp,
    crypto_desc.description
from crypto_raw_table
left join crypto_desc on crypto_desc.id=crypto_raw_table.id
