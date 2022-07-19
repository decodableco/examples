insert into mqtt_location_enriched
select 
    _type as `type`,
    acc as accuracy,
    alt as altitude,
    batt as battery_level,
    bs as battery_status,
    cog as course_over_ground, --the actual direction of progress of a vessel, between two points, with respect to the surface of the earth
    lat,
    lon,
    m as monitoring_mode,
    p as barometric_pressure,
    SSID as wlan,
    BSSID as access_point,
    tid as tracker_id,
    TO_TIMESTAMP_LTZ(tst, 3) as `timestamp`,
    vac as vertical_accuracy,
    vel as velocity, -- velocity (iOS,Android/integer/kmh/optional)
    CASE 
        WHEN vel BETWEEN 5 and 11 THEN 'running'
        WHEN vel BETWEEN 10 and 501 THEN 'driving'
        WHEN vel > 500 THEN 'flying'
        WHEN DAYOFWEEK(TO_TIMESTAMP_LTZ(tst, 3)) BETWEEN 1 and 7 THEN 'working'
        ELSE 'resting'
    end AS status,
    CASE 
        WHEN SSID = 'lon3r-5G' THEN 'at home'
        ELSE 'somewhere else'
    end AS location
from mqtt_location

insert into stream_220714071518110
select 
--"_type":"location","acc":5,"alt":120,"batt":33,"bs":1,"BSSID":"dc:ef:9:e5:78:36","cog":31,
-- "conn":"w","lat":41.702987,"lon":-74.082472,"m":2,"p":100.138,"SSID":"lon3r-5G","t":"u","tid":"0E","tst":1657840848,"vac":3,"vel":0}
    topic,



insert into mqtt_cleanse_enrich

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



