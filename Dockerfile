FROM alpine:3.15.4 as sbt

ARG SBT_VERSION=1.6.2
RUN apk update && \apk add --no-cache openjdk11-jre-headless curl bash && \curl -L -o sbt-${SBT_VERSION}.tgz https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz && \tar -xvzf sbt-${SBT_VERSION}.tgz && \rm sbt-${SBT_VERSION}.tgz

ENV PATH $PATH:/sbt/bin

WORKDIR /app

RUN sbt -V


FROM sbt as play-dev

COPY . /app/

CMD [ "bash" ]

FROM play-dev as builder

RUN sbt dist

RUN unzip target/universal/mameti_bbs-1.0-SNAPSHOT.zip


FROM openjdk:11-jre-slim as executor

COPY --from=builder /app/mameti_bbs-1.0-SNAPSHOT mameti_bbs

EXPOSE 9000

CMD [ "mameti_bbs/bin/mameti_bbs" ]
