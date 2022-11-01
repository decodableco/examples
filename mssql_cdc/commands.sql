create database test
use test
EXEC sys.sp_cdc_enable_db

create table customers (
	userid int primary key,
	first_name varchar(63) not null,
	last_name varchar(63) not null,
	phone varchar(63)
);

SELECT 
*
FROM sys.filegroups;

exec sys.sp_cdc_enable_table
	@source_schema = N'dbo',
	@source_name   = N'customers',
	@capture_instance=N'instance_customers',
	@role_name     = NULL,
	@filegroup_name = N'PRIMARY',
	@supports_net_changes=1

exec sys.sp_cdc_help_change_data_capture

exec sys.sp_cdc_disable_table   
	@source_schema ='dbo' ,   
 	@source_name = 'customers',
	@capture_instance = 'all'
	
select * from  msdb.dbo.cdc_jobs

SELECT 
	CASE 
		WHEN dss.status=4 THEN 1 
		ELSE 0 
	END AS isRunning 
FROM sys.dm_server_services dss 
WHERE dss.servicename LIKE N'SQL Server Agent (%';

EXEC sys.sp_cdc_start_job 

SELECT s.name AS Schema_Name, tb.name AS Table_Name
, tb.object_id, tb.type, tb.type_desc, tb.is_tracked_by_cdc
FROM sys.tables tb
INNER JOIN sys.schemas s on s.schema_id = tb.schema_id
WHERE tb.is_tracked_by_cdc = 1

select name, is_cdc_enabled from sys.databases


DECLARE @i int = 0
WHILE @i < 50
BEGIN
    SET @i = @i + 1
    insert into customers values (@i, concat('foo',@i),concat('bar',@i), @i)
END

DECLARE @i int = 0
WHILE @i < 1000
BEGIN
    SET @i = @i + 1
    update customers set first_name = concat('foo',@i), phone = @i where userid = (@i % 50)
    WAITFOR DELAY '00:00:05'
END


select * from customers

delete from customers where userid = 1


