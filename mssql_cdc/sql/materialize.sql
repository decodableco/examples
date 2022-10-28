insert into mssql_cdc_materialized
select *
from table(to_change(mssql_cdc_partitioned))
