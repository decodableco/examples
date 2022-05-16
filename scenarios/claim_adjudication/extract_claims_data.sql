insert into extracted
select -- each element of the `claims` array creates a new record
  to_timestamp(claim.date_of_consultation) as date_of_consultation,
  claim.member_id as member_id,
  claim.procedure_code as procedure_code,
  -- non-array fields common to each record are also included in the output
  to_timestamp(concat(claim_date, ' 00:00:00')) as claim_date,
  facility_id
from `claims-raw`
  cross join unnest(`claims`) as claim