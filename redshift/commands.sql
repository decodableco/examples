
create table customers (
	userid int primary key,
	first_name varchar(63) not null,
	last_name varchar(63) not null,
	phone varchar(63)
);


select * from customers c 
truncate customers 

-- CREATES AN EXTERNAL SCHEMA THAT PROVIDES ACCESS TO KINESIS STREAMS
CREATE EXTERNAL SCHEMA postgres_users_schema
FROM KINESIS
IAM_ROLE 'arn:aws:iam::657006753396:role/hubert-redshift-kinesis';
-- drop SCHEMA postgres_users_schema

-- CREATES THE MATERIALIZED VIEW
CREATE MATERIALIZED VIEW postgres_users_schema_view AS
SELECT 
	approximate_arrival_timestamp, 
	partition_key, 
	shard_id,
	sequence_number,
	JSON_PARSE(from_varbyte(kinesis_data , 'utf-8')) as Data
FROM postgres_users_schema."hubert-redshift"
--WHERE is_utf8(Data) AND is_valid_json(from_varbyte(Data, 'utf-8'));
--drop materialized view postgres_users_schema_view

create table customers_merged (
	userid int primary key,
	first_name varchar(63) not null,
	last_name varchar(63) not null,
	phone varchar(63)
);

REFRESH MATERIALIZED VIEW postgres_users_schema_view;
select * from customers_merged


-- DELETE
with customers_flattened as (
	select 
		case 
			when data.op = 'd' then data.before.userid
			else data.after.userid
		end as userid,
		data.after.first_name,
		data.after.last_name,
		data.after.phone,
		data.op,
		approximate_arrival_timestamp ts_ms
	from postgres_users_schema_view
),
customers_latest as (
	select 
		B.userid,
		A.first_name,
		A.last_name,
		A.phone,
		A.op,
		A.ts_ms ts_ms
	from customers_flattened A
	join (
		select 
			userid,
			max(ts_ms) ts_ms
		from customers_flattened
		group by userid
	) B on A.userid = B.userid and A.ts_ms = B.ts_ms
	ORDER BY USERID
)
delete from customers_merged
where userid in ( 
	select userid from customers_latest
)
;


-- INSERT
insert into customers_merged
with customers_flattened as (
	select 
		case 
			when data.op = 'd' then data.before.userid
			else data.after.userid
		end as userid,
		data.after.first_name,
		data.after.last_name,
		data.after.phone,
		data.op,
		approximate_arrival_timestamp ts_ms
	from postgres_users_schema_view
),
customers_latest as (
	select 
		B.userid,
		A.first_name,
		A.last_name,
		A.phone,
		A.op,
		A.ts_ms ts_ms
	from customers_flattened A
	join (
		select 
			userid,
			max(ts_ms) ts_ms
		from customers_flattened
		group by userid
	) B on A.userid = B.userid and A.ts_ms = B.ts_ms
	ORDER BY USERID
)
select 
	cast(A.userid as int), 
	cast(A.first_name as varchar), 
	cast(A.last_name as varchar), 
	cast(A.phone as varchar)
from customers_latest A
left join customers_merged B on A.userid=B.userid
where B.userid is null and op <> 'd'

