
# Decodable + Snowpipe + Snowflake
This demo outlines the work needed to integrate Decodable with Snowflake using the S3 Sink and Snowpipe.

## Configure S3 sink
Follow the instructions in this link.

https://docs.decodable.co/docs/connector-reference-s3

## Create the integration

```sql
 CREATE STORAGE INTEGRATION decodable_s3_sink
   TYPE = EXTERNAL_STAGE
   STORAGE_PROVIDER = 'S3'
   ENABLED = TRUE
   STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::123412341234:role/your-role'
   STORAGE_ALLOWED_LOCATIONS = ('s3://my-bucket/my-dir/')
```

### Get the SF creds

```sql
DESC INTEGRATION decodable_s3_sink
```

## Edit the trust policy

```json
{
	"Version": "2012-10-17",
	"Statement": [
		{
            "Version": "2012-10-17",
            "Statement": [
                {
                "Sid": "",
                "Effect": "Allow",
                "Principal": {
                    "AWS": "<snowflake_user_arn>"
                },
                "Action": "sts:AssumeRole",
                "Condition": {
                    "StringEquals": {
                    "sts:ExternalId": "<snowflake_external_id>"
                    }
                }
                }
            ]
            }
	]
}
```

## Create database and table

```sql
CREATE STORAGE INTEGRATION decodable_s3_sink
  TYPE = EXTERNAL_STAGE
  STORAGE_PROVIDER = 'S3'
  ENABLED = TRUE
  STORAGE_AWS_ROLE_ARN = 'arn:aws:iam::12341234:role/MY-role'
  STORAGE_ALLOWED_LOCATIONS = ('s3://my-bucket/my-dir/')

DESC INTEGRATION decodable_s3_sink

use schema snowpipe_db.public;

create or replace TABLE SNOWPIPE_DB.PUBLIC.CRYPTO_JSON (
	SRC VARIANT
);

create stage mystage
  url = 's3://my-bucket/my-dir/'
  storage_integration = decodable_s3_sink;

create pipe snowpipe_db.public.mypipe auto_ingest=true as
  copy into snowpipe_db.public.crypto_json
  from @snowpipe_db.public.mystage
  file_format = (type = 'JSON');
  
create warehouse test
  
show pipes
-- get value from notification_channel column
--https://docs.aws.amazon.com/AmazonS3/latest/userguide/enable-event-notifications.html
--https://docs.snowflake.com/en/user-guide/data-load-snowpipe-auto-s3.html#option-1-creating-a-new-s3-event-notification-to-automate-snowpipe

select src:id as id, src:name as name, src:price as price from crypto_json
```