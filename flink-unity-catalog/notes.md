https://docs.databricks.com/en/delta/uniform.html#read-using-the-unity-catalog-iceberg-catalog-endpoint

> Unity Catalog provides a read-only implementation of the Iceberg REST catalog API for Delta tables with UniForm enabled

https://docs.databricks.com/en/delta/uniform.html#limitations

> Iceberg clients can only read from UniForm. Writes are not supported.


```bash
❯ docker exec unity bash -c "./bin/uc catalog list"
┌────────────┬───────────────────────────────────┬───────────────────────────────────┬───────────────────────────────────┬───────────────────────────────────┬──────────────────────────────────┐
│    NAME    │              COMMENT              │            PROPERTIES             │            CREATED_AT             │            UPDATED_AT             │            ID                    │
├────────────┼───────────────────────────────────┼───────────────────────────────────┼───────────────────────────────────┼───────────────────────────────────┼──────────────────────────────────┤
│unity       │Main catalog                       │{}                                 │1718028783345                      │null                               │17562adb-e6f8-463e-b82a-22264b06..│
└────────────┴───────────────────────────────────┴───────────────────────────────────┴───────────────────────────────────┴───────────────────────────────────┴──────────────────────────────────┘
```

```bash
❯ docker exec unity bash -c "./bin/uc catalog create --name foo"
┌────────────────────┬──────────────────────────────────────────────────┐
│        KEY         │                      VALUE                       │
├────────────────────┼──────────────────────────────────────────────────┤
│NAME                │foo                                               │
├────────────────────┼──────────────────────────────────────────────────┤
│COMMENT             │null                                              │
├────────────────────┼──────────────────────────────────────────────────┤
│PROPERTIES          │{}                                                │
├────────────────────┼──────────────────────────────────────────────────┤
│CREATED_AT          │1719937771074                                     │
├────────────────────┼──────────────────────────────────────────────────┤
│UPDATED_AT          │null                                              │
├────────────────────┼──────────────────────────────────────────────────┤
│ID                  │e91c6e7f-f144-491e-a88d-877a42904268              │
└────────────────────┴──────────────────────────────────────────────────┘
```

```sql
CREATE CATALOG c_iceberg_unity WITH (
    'connector'='type',
    'catalog-name'='rest',
    'catalog-impl'='org.apache.iceberg.rest.RESTCatalog',
    'uri'='http://unity:8080/api/2.1/unity-catalog/iceberg',
    'warehouse'='s3://wareshouse/');

USE CATALOG c_iceberg_unity;

Flink SQL> SHOW DATABASES;
+---------------+
| database name |
+---------------+
|           foo |
|         unity |
+---------------+
2 rows in set

Flink SQL> use foo;
[INFO] Execute statement succeed.

Flink SQL> show tables;
[ERROR] Could not execute SQL statement. Reason:
org.apache.iceberg.exceptions.ServiceFailureException: Server error: null: 
{
    "error_code": "INTERNAL",
    "details": [
        {
            "reason": "INTERNAL",
            "metadata": {},
            "@type": "google.rpc.ErrorInfo"
        }
    ],
    "stack_trace": "[io.unitycatalog.server.service.IcebergRestCatalogService.listTables(IcebergRestCatalogService.java:210), com.linecorp.armeria.internal.server.annotation.AnnotatedService.invoke(AnnotatedService.java:382), com.linecorp.armeria.internal.server.annotation.AnnotatedService.serve0(AnnotatedService.java:298), com.linecorp.armeria.internal.server.annotation.AnnotatedService.serve(AnnotatedService.java:272), com.linecorp.armeria.internal.server.annotation.AnnotatedService.serve(AnnotatedService.java:78), com.linecorp.armeria.internal.server.annotation.AnnotatedService$ExceptionHandlingHttpService.serve(AnnotatedService.java:536), com.linecorp.armeria.server.HttpServerHandler.serve0(HttpServerHandler.java:463), com.linecorp.armeria.server.HttpServerHandler.handleRequest(HttpServerHandler.java:398), com.linecorp.armeria.server.HttpServerHandler.channelRead(HttpServerHandler.java:281), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), com.linecorp.armeria.server.Http1RequestDecoder.channelRead(Http1RequestDecoder.java:282), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), com.linecorp.armeria.server.HttpServerUpgradeHandler.channelRead(HttpServerUpgradeHandler.java:227), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:444), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:436), io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:346), io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:318), io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:251), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), io.netty.handler.logging.LoggingHandler.channelRead(LoggingHandler.java:280), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), io.netty.handler.flush.FlushConsolidationHandler.channelRead(FlushConsolidationHandler.java:152), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:442), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:412), io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:440), io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:420), io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919), io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:801), io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:509), io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:407), io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997), io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74), io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30), java.base/java.lang.Thread.run(Thread.java:829)]",
    "message": "invalid namespace foo"
}
```

https://github.com/unitycatalog/unitycatalog/issues/10

```sql
Flink SQL> CREATE TABLE t_foo (c1 varchar, c2 int);
[ERROR] Could not execute SQL statement. Reason:
org.apache.iceberg.exceptions.RESTException: Unable to process: Status: 405
Description: Method Not Allowed
```


```bash
❯ docker exec unity bash -c "./bin/uc schema create --catalog foo --name bar"
┌────────────────────┬──────────────────────────────────────────────────┐
│        KEY         │                      VALUE                       │
├────────────────────┼──────────────────────────────────────────────────┤
│NAME                │bar                                               │
├────────────────────┼──────────────────────────────────────────────────┤
│CATALOG_NAME        │foo                                               │
├────────────────────┼──────────────────────────────────────────────────┤
│COMMENT             │null                                              │
├────────────────────┼──────────────────────────────────────────────────┤
│PROPERTIES          │{}                                                │
├────────────────────┼──────────────────────────────────────────────────┤
│FULL_NAME           │foo.bar                                           │
├────────────────────┼──────────────────────────────────────────────────┤
│CREATED_AT          │1719938500612                                     │
├────────────────────┼──────────────────────────────────────────────────┤
│UPDATED_AT          │null                                              │
├────────────────────┼──────────────────────────────────────────────────┤
│SCHEMA_ID           │ad86c745-6c59-4462-9b98-98b6794f76ec              │
└────────────────────┴──────────────────────────────────────────────────┘

❯ docker exec unity bash -c "./bin/uc table list --catalog foo --schema bar"
[]

❯ docker exec unity bash -c "./bin/uc table create \
                            --full_name foo.bar.t_test --columns col1 STRING \
                            --storage_location s3://warehouse/ \
                            --format DELTA"
┌────────────────────┬──────────────────────────────────────────────────┐
│        KEY         │                      VALUE                       │
├────────────────────┼──────────────────────────────────────────────────┤
│NAME                │t_test                                            │
├────────────────────┼──────────────────────────────────────────────────┤
│CATALOG_NAME        │foo                                               │
├────────────────────┼──────────────────────────────────────────────────┤
│SCHEMA_NAME         │bar                                               │
├────────────────────┼──────────────────────────────────────────────────┤
│TABLE_TYPE          │EXTERNAL                                          │
├────────────────────┼──────────────────────────────────────────────────┤
│DATA_SOURCE_FORMAT  │DELTA                                             │
├────────────────────┼──────────────────────────────────────────────────┤
│COLUMNS             │                                                  │
├────────────────────┼──────────────────────────────────────────────────┤
│STORAGE_LOCATION    │s3://warehouse/                                   │
├────────────────────┼──────────────────────────────────────────────────┤
│COMMENT             │null                                              │
├────────────────────┼──────────────────────────────────────────────────┤
│PROPERTIES          │{}                                                │
├────────────────────┼──────────────────────────────────────────────────┤
│CREATED_AT          │1719938901456                                     │
├────────────────────┼──────────────────────────────────────────────────┤
│UPDATED_AT          │1719938901456                                     │
├────────────────────┼──────────────────────────────────────────────────┤
│TABLE_ID            │f777e824-0706-424a-9de6-cfd41b949030              │
└────────────────────┴──────────────────────────────────────────────────┘

❯ docker exec unity bash -c "./bin/uc table list --catalog foo --schema bar"
┌────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┬────────────────┐
│    NAME    │  CATALOG_NAME  │  SCHEMA_NAME   │   TABLE_TYPE   │DATA_SOURCE_FORM│    COLUMNS     │STORAGE_LOCATION│    COMMENT     │   PROPERTIES   │   CREATED_AT   │   UPDATED_AT   │    TABLE_ID    │
│            │                │                │                │       AT       │                │                │                │                │                │                │                │
├────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┼────────────────┤
│t_test      │foo             │bar             │EXTERNAL        │DELTA           │[]              │s3://warehouse/ │null            │{}              │1719938901456   │1719938901456   │f777e824-0706...│
└────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┴────────────────┘

❯ docker exec unity bash -c "./bin/uc table write \
                            --full_name foo.bar.t_test"

Error occurred while executing the command. Failed to write sample data to delta table s3://warehouse/
generateTemporaryTableCredentials call failed with: 400 - 
{
    "error_code": "FAILED_PRECONDITION",
    "details": [
        {
            "reason": "FAILED_PRECONDITION",
            "metadata": {},
            "@type": "google.rpc.ErrorInfo"
        }
    ],
    "stack_trace": null,
    "message": "S3 bucket configuration not found."
}
```

```bash
❯ docker compose exec pyiceberg bash -c "pyiceberg --uri http://unity:8080/api/2.1/unity-catalog/iceberg list"
unity
foo
❯ docker compose exec pyiceberg bash -c "pyiceberg --uri http://unity:8080/api/2.1/unity-catalog/iceberg list unity"
unity.unity.default
❯ docker compose exec pyiceberg bash -c "pyiceberg --uri http://unity:8080/api/2.1/unity-catalog/iceberg list foo"
foo.foo.bar
