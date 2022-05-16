insert into summary
select to_timestamp(
    convert_tz(
      replace(left(`flight.live.updated`, 19), 'T', ' '),
      'UTC',
      `flight.arrival.timezone`
    )
  ) as update_timestamp,
  `flight.airline.name` as airline_name,
  `flight.airline.iata` as airline_iata,
  `flight.flight.number` as flight_number,
  `flight.departure.airport` as departuring_from,
  `flight.departure.iata` as departuring_iata,
  `flight.arrival.airport` as arriving_at,
  `flight.arrival.iata` as arriving_iata,
  `flight.arrival.gate` as arrival_gate,
  to_timestamp(
    convert_tz(
      replace(left(`flight.arrival.estimated`, 19), 'T', ' '),
      'UTC',
      `flight.arrival.timezone`
    )
  ) as arrival_time,
  case
    when `e.departure.delay` + `e.arrival.delay` > 15 then 'DELAYED'
    else 'ON TIME'
  end as flight_status,
  case
    when cast(`flight.live.is_ground` as int) = 0 then 'IN FLIGHT'
    when cast(`flight.live.is_ground` as int) = 1
    and coalesce(`flight.arrival.actual`, '') = '' then 'DEPARTING'
    else 'ARRIVED'
  end as flight_stage
from `flights-raw`
  cross join unnest(flights) as flight