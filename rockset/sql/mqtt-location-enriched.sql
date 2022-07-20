
insert into mqtt_enriched

select *,
    CASE 
        WHEN velocity BETWEEN 5 and 11 THEN 'running'
        WHEN velocity BETWEEN 10 and 501 THEN 'driving'
        WHEN velocity > 500 THEN 'flying'
        WHEN DAYOFWEEK(`timestamp`) BETWEEN 1 and 7 THEN 'working'
        ELSE 'resting'
    end AS status,
    SQRT(POWER(lat - 40.75199317462868, 2) + POWER(lon + 73.98781215507576, 2)) as euclidean_distance, -- Empire State Building
from (
    select 
        topic,
        split_index(topic,'/',1) as `user`,
        split_index(topic,'/',2) as `device`,
        -- JSON_VALUE(payload, '$._type') as `type`,
        CAST(JSON_VALUE(payload, '$.acc') as int) as accuracy, -- Accuracy of the reported location in meters without unit (iOS,Android/integer/meters/optional)
        CAST(JSON_VALUE(payload, '$.alt') as int) as altitude, -- Altitude measured above sea level (iOS,Android/integer/meters/optional)
        CAST(JSON_VALUE(payload, '$.batt') as int) as battery_level, -- Device battery level (iOS,Android/integer/percent/optional)
        CAST(JSON_VALUE(payload, '$.bs') as int) as battery_status, -- Battery Status 0=unknown, 1=unplugged, 2=charging, 3=full (iOS, Android)
        JSON_VALUE(payload, '$.BSSID') as BSSID,
        CAST(JSON_VALUE(payload, '$.cog') as double) as course_over_ground, -- The actual direction of progress of a vessel, between two points, with respect to the surface of the earth
        JSON_VALUE(payload, '$.conn') as conn,
        CAST(JSON_VALUE(payload, '$.lat') as double) as lat,
        CAST(JSON_VALUE(payload, '$.lon') as double) as lon,
        -- CAST(JSON_VALUE(payload, '$.m') as int) as monitoring_mode,
        CAST(JSON_VALUE(payload, '$.p') as double) as barometric_pressure,
        JSON_VALUE(payload, '$.SSID') as SSID,
        JSON_VALUE(payload, '$.t') as `trigger`,
        JSON_VALUE(payload, '$.tid') as tracker_id,
        TO_TIMESTAMP_LTZ(CAST(JSON_VALUE(payload, '$.tst') as bigint) * 1000, 3) as `timestamp`, -- convert unix time to epoch
        CAST(JSON_VALUE(payload, '$.tst') as bigint) * 1000 as ts, -- convert to milliseconds
        CAST(JSON_VALUE(payload, '$.vac') as int) as vertical_accuracy,
        CAST(JSON_VALUE(payload, '$.vel') as int) as velocity -- velocity (iOS,Android/integer/kmh/optional)
    from mqtt_raw
)



