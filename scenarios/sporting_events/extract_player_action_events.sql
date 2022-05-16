insert into extracted
select -- each element of the `events` array creates a new record
  to_timestamp(
    `e.updated_at`,
    'yyyy-MM-dd''T''HH:mm:ss.SSS''Z'''
  ) as updated_at,
  case
    when e.detail = 'Normal Goal' then 1
    else 0
  end as normal_goal,
  case
    when e.detail = 'Own Goal' then 1
    else 0
  end as own_goal,
  case
    when e.detail = 'Penalty' then 1
    else 0
  end as penalty,
  case
    when e.detail like '%Yellow%' then 1
    else 0
  end as yellow_card,
  case
    when e.detail = 'Red card' then 1
    else 0
  end as red_card,
  case
    when e.type like 'Subst' then 1
    else 0
  end as substitution,
  from `events-raw`
  cross join unnest(`events`) as e