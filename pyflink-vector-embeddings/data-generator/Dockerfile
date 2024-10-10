FROM alpine:3.20.2

RUN apk add jq curl

RUN mkdir -p /home/data
RUN mkdir -p /home/scripts

COPY ./data_sample.json /home/data/data_sample.json
COPY ./generate.sh /home/scripts/generate.sh

WORKDIR /home

CMD [ "sh", "/home/scripts/generate.sh" ]
