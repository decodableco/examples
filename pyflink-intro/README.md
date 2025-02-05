# A Hands-On Introduction to PyFlink

_If you have a Python background and are stepping into the world of real-time data processing for the first time, you might feel a little intimidated by Apache Flink which, during its earlier days, used to address Java or Scala developers only. Luckily, times have changed significantly in recent years and as a developer who lives and breathes Python, nothing should stop you from building on top of Apache Flink today._

This folder contains everything that's needed to work through the examples discussed in this [blog post](https://www.decodable.co/blog/a-hands-on-introduction-to-pyflink). Find its outline below.

## Outline

* What is PyFlink?
    - Table API
    - DataStream API
    - The API Choice is Yours
* Prerequisites for PyFlink Development
    - Dev Containers Setup
* Running Your First PyFlink Job
    - What about dependencies?
        - Including Python Packages
        - Including Java Dependencies
    - Detour: Bridging between the Two Worlds
* Real-time Vector Ingestion with PyFlink
    - Overview
    - Implementation
        - Setup Table Environment
        - Write and Register UDFs
        - Define Source and Sink Tables
        - Data Processing with SQL
        - Tunneling Traffic to Source and Sink Systems
* Moving Jobs From Development to Production
    - Upstream Apache Flink Deployment
    - Deploying to Decodable
