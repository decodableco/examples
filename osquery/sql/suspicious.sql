-- A new stream will be created for the output of this pipeline.
-- The stream name will match the name used in the 'insert' statement.
insert into suspicious_processes
select *
from filtered_processes
where   (  -- cmdline 
            columns['cmdline'] NOT LIKE '%sshd%' and
            columns['cmdline'] NOT LIKE '%init%' and
            columns['cmdline'] NOT LIKE '%systemd%' and
            columns['cmdline'] NOT LIKE '%multipathd%' and
            columns['cmdline'] NOT LIKE '%amazon%' and
            columns['cmdline'] NOT LIKE '%bash%' and
            columns['cmdline'] NOT LIKE '%syslog%' and
            columns['cmdline'] NOT LIKE '%udisksd%' and
            columns['cmdline'] NOT LIKE '%snapd%' and
            columns['cmdline'] NOT LIKE '%chron%'
        ) or
        -- running for more than 1 day
        cast(columns['start_time'] as bigint) - UNIX_TIMESTAMP() > 86400000
        