# Mirroring Postgres to Snowflake
These instructions provide you a solution to mirror a source table in Postgres (or any other Debezium supported database) to a table in Snowflake.

The solution requires the use of Snowpipe. The instructions for setting up Snowpipe can be found [here](README.md)

You will need to use Decodable to configure a CDC connection to a Postgres database. Since Decodable automatically creates a **change stream** (a materialized view) from the connection, you will need create a pipeline (SQL) to transform that change stream into an **append stream** by using the function **TO_APPEND()**. (see below)

```sql
insert into customer_append
select *
from table(TO_APPEND(customers))
```

This function just takes the underlying CDC stream that the materialized view uses to derive its view and serves it as an append stream. 

---
**NOTE**

The TO_APPEND function **dematerializes** the change stream into an append stream.

---


## Step 1: Create a Postgres source
 Create a Postgres CDC source connection. Instructions are [here](https://docs.decodable.co/docs/connector-reference-postgres-cdc)

## Step 2: Dematerialize the change stream
Create a pipeline following the syntax below replacing **{{your_change_stream_from_postgres}}** to the change stream created from the Postgres CDC source. Change **{{your_append_stream_from_postgres}}** to you preferred output stream name.

```sql
insert into {{your_append_stream_from_postgres}}
select *
from table(TO_APPEND({{your_change_stream_from_postgres}}))
```

## Step 3: Create an S3 sink
Create an S3 sink reading from the stream **{{your_append_stream_from_postgres}}** from step 2.

## Step 4: Configure Snowpipe
Configure Snowpipe using the instructions [here](README.md).

## Step 5: Create an Append Stream in Snowflake
This creates an append stream in Snowflake. Append streams in Snowflake emulate topics like in Kafka/RedPanda/Pulsar.

This stream will capture changes occurring to the staging table. We'll use this stream to merge data into a destination table that will serve as the mirrored Postgres table in Snowflake.

```sql
create or replace stream {{append_stream_name}} on table {{the_staging_table_name}} append_only=true;
```

## Step 5: Create a Task to Merge changes into the Mirror Table
Create a merge statement following the syntax below. Logic:

1. create a task that runs every minute only if the stream is not empty
2. merge the data in the stream
   1. If operation is 'd', delete the record from the merge table
   2. If operation is 'u' or 'c', update the record in the merge table with the latest values in the **after** property of the debezium record.
   3. If the record did not exist in the merge table, insert it. 

```sql
CREATE OR REPLACE TASK customers_pg_snowpipe_task -- creates task
  WAREHOUSE = test
  SCHEDULE = '1 minute' -- task runs every minute
WHEN
  SYSTEM$STREAM_HAS_DATA('customers_pg_snowpipe_stream') -- task runs only if the stream has data
AS
merge into CUSTOMERS_PG_SNOWPIPE_MERGE c using (
    select
        case 
            when SRC:op = 'd' then SRC:before:userid
            else SRC:after:userid 
        end as userid,
        SRC:after:first_name as first_name,
        SRC:after:last_name as last_name,
        SRC:after:phone as phone,
        SRC:op as op
    from customers_pg_snowpipe_stream c1
    join ( -- ensure we're reading a single record per ID and it's the latest record
        select 
          SRC:after:userid as userid, 
          max(SRC:ts_ms) as ts
        from customers_pg_snowpipe_stream 
        group by SRC:after:userid
    ) c2
    on c1.SRC:after:userid=c2.userid and c1.SRC:ts_ms=c2.ts
    where 
      METADATA$ACTION = 'INSERT' and -- only want insert events to the staging table
      SRC:op in ('d', 'c', 'u', 'r') -- only process these operations
    order by SRC:ts_ms
    
) as s on s.userid = c.userid
when matched and s.op='d' then -- delete record
    delete
when matched and (s.op='u') then -- update record
    update set
        first_name = s.first_name,
        last_name = s.last_name,
        phone = s.phone
WHEN NOT matched THEN -- insert record
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

```

## View Tasks
Go to Data->YOUR_DATABASE->YOUR_SCHEMA->Tasks to see your tasks. Click on the **Run History** tab to see the history of your runs. Alternatively you can run `show tasks`. If your task is at a `suspended` state, you'll need to resume it:

```sql
ALTER TASK IF EXISTS  {{YOUR_STREAM_NAME}} RESUME
```

## Troubleshooting

- Check the status of your Task to make sure it's not `suspended`.
- Make sure your `merge` statement is selecting the stream and NOT the staging table.
- If you are not seeing your changes, go to the data view/database/tasks to see your tasks. Check the **Run History** column to see if the task is running. If it failed, hover over the "Failed i" to see the error message.
- If you get a duplicate row error: `Duplicate row detected during DML action Row Values: [11, "foo", "bar", "111"]` , you may need to delete this record from the merge table.
- If you change the same record in Postgres multiple times causing multiple transactions to merge, make sure that you are ordering by a timestamp.