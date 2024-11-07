FROM --platform=linux/arm64 python:3.11.10-slim-bullseye

RUN apt-get update -y && \
    apt-get install -y make && \
    apt-get install -y wget && \
    apt-get install -y zip && \
    rm -rf /var/lib/apt/lists/*

CMD [ "/home/pyflink-app/build.sh" ]
