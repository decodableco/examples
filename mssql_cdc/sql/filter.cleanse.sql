insert into mssql_cdc_partitioned
select
    payload.after.userid,
    payload.after.first_name,
    payload.after.last_name,
    payload.after.phone,
    payload.op,
    TO_TIMESTAMP_LTZ(payload.ts_ms, 3) as ts_ms
from mssql_cdc
where payload.op <> 'd' --ignoring deletes
