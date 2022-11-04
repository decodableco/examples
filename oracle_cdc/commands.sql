
-- CREATE USER decoder IDENTIFIED BY "!!D3moDay!!";
-- DROP USER decoder;

-- USE database admin


begin
    rdsadmin.rdsadmin_util.alter_supplemental_logging(p_action => 'ADD');
end;
/

EXEC rdsadmin.rdsadmin_util.switch_logfile;

create table admin.customers (
	userid int primary key,
	first_name varchar(63) not null,
	last_name varchar(63) not null,
	phone varchar(63)
);
ALTER TABLE admin.customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;

SELECT SUPPLEMENTAL_LOG_DATA_MIN FROM V$DATABASE;

--TRUNCATE TABLE customers

BEGIN
FOR i IN 1..50 LOOP
	insert into customers values (i, concat('foo',i),concat('bar',i), i);

END LOOP;
COMMIT;
END;

BEGIN
FOR i IN 1..50 LOOP
	update customers set first_name = concat('foo-rand',DBMS_RANDOM.RANDOM), phone = i where userid = i;

END LOOP;
COMMIT;
END;


-- ORACLE_SID=ORACLCDB dbz_oracle sqlplus /nolog

-- CONNECT sys/top_secret AS SYSDBA
-- alter system set db_recovery_file_dest_size = 10G;
-- alter system set db_recovery_file_dest = '/opt/oracle/oradata/recovery_area' scope=spfile;
-- shutdown immediate
-- startup mount
-- alter database archivelog;
-- alter database open;
-- -- Should now "Database log mode: Archive Mode"
-- archive log list

-- exit;

-- exec rdsadmin.rdsadmin_util.alter_supplemental_logging('ADD');
-- ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;
