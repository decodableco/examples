INSERT INTO `demo_day_change_stream`
SELECT
  CAST(envoy['response_code'] AS STRING) AS `status_code`,
  count(*) AS `count`
FROM (
    SELECT
      grok(
        `value`,
        '\[%{TIMESTAMP_ISO8601:timestamp}\] "%{DATA:method} %{DATA:original_path} %{DATA:protocol}" %{DATA:response_code} %{DATA:response_flags} %{NUMBER:bytes_rcvd} %{NUMBER:bytes_sent} %{NUMBER:duration} %{DATA:upstream_svc_time} "%{DATA:x_forwarded_for}" "%{DATA:useragent}" "%{DATA:request_id}" "%{DATA:authority}" "%{DATA:upstream_host}"'
      ) AS envoy
    FROM `demo_day_envoy_raw` 
) group by CAST(envoy['response_code'] AS STRING)