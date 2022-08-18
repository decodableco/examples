insert into crypto_changes_s3
select 
    id,
    currency,
    symbol,
    name,
    logo_url,
    status,
    price,
    TO_TIMESTAMP(price_timestamp, 'yyyy-MM-dd''T''HH:mm:ss.SSS''Z''') as price_date,
    price_timestamp,
    circulating_supply,
    max_supply,
    market_cap_dominance,
    high,
    high_timestamp
from crypto_raw