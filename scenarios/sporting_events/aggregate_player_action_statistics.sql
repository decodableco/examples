insert into summary
select window_start,
  window_end,
  sum(normal_goal) as normal_goals,
  sum(own_goal) as own_goals,
  sum(penalty) as penalties,
  sum(yellow_card) as yellow_cards,
  sum(red_card) as red_cards,
  sum(substitution) as substitutions
from table (
    hop(
      table extracted,
      descriptor(updated_at),
      interval '1' minute,
      interval '5' minutes
    )
  )
group by window_start,
  window_end