--https://docs.snowflake.com/en/user-guide/streams-examples.html#differences-between-standard-and-append-only-streams

CREATE STORAGE INTEGRATION customers_int
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = 'S3'
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::657006753396:role/hubert-role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://hubert-demos/customers/')

-- THE STAGE FOR SNOWPIPE
create stage customers_stage
  url = 's3://hubert-demos/customers/'
  storage_integration = customers_int;

-- THE STAGING TABLE
create or replace TABLE SNOWPIPE_DB.PUBLIC.CUSTOMER_CDC (
	SRC VARIANT
);

-- SNOWPIPE
create pipe snowpipe_db.public.customer_cdc_pipe auto_ingest=true as
  copy into SNOWPIPE_DB.PUBLIC.CUSTOMER_CDC
  from @snowpipe_db.public.customers_stage
  file_format = (type = 'JSON');

-- SNOWFLAKE APPEND STREAM
create or replace stream append_only_customers_stream on table customer_cdc append_only=true;

-- EXECUTE CHANGES
CREATE OR REPLACE TASK merge_pg_users
  WAREHOUSE = test
  SCHEDULE = '1 minute'
WHEN
  SYSTEM$STREAM_HAS_DATA('append_only_users_stream')
AS
merge into users_merge c using (
    select
        case 
            when SRC:op = 'd' then SRC:before:userid
            else SRC:after:userid 
        end as userid,
        SRC:after:first_name as first_name,
        SRC:after:last_name as last_name,
        SRC:after:phone as phone,
        SRC:op as op
    from
        append_only_users_stream
    where METADATA$ACTION = 'INSERT' and SRC:op <> 'r'
    order by SRC:ts_ms
) as s on s.userid = c.userid
when matched and s.op='d' then 
    delete
when matched and s.op='u' then
    update set
        first_name = s.first_name,
        last_name = s.last_name,
        phone = s.phone
WHEN NOT matched and s.op='c' THEN -- insert only if op is c
    INSERT
        (
            userid,
            first_name,
            last_name,
            phone
        )
    values
        (
            s.userid,
            s.first_name,
            s.last_name,
            s.phone
        );


alter task merge_cdc_customers resume;

