insert into summary
select *
from (
    select window_start,
      window_end,
      claim_date,
      facility_id,
      member_id,
      procedure_code,
      count(1) as duplicate_claims -- # of claims per member per procedure
    from table (
        tumble(
          table extracted descriptor(claim_date),
          interval '1' day
        )
      )
    group by window_start,
      window_end,
      claim_date,
      facility_id,
      member_id,
      procedure_code
  )
where duplicate_claims > 1