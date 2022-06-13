-- A new stream will be created for the output of this pipeline.
-- The stream name will match the name used in the 'insert' statement.
insert into filtered_processes
select *
from osquery_processes
where columns['cmdline'] NOT LIKE '%osquery%' and -- remove osquery related processes
        columns['cmdline'] <> ''
        