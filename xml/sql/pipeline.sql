insert into demo_day_parsed 
select
  employee['fname'] first_name,
  employee['lname'] last_name
from (
  select
    -- Parse `xml` to a DOM and extract fields using XPath expressions.
    -- xpaths() returns a map.
    xpaths(
      xml,
      -- General
      'fname', '/employee/fname/text()',
      'lname', '/employee/lname/text()'
      ) as employee
  from demo_day_xml_raw
)