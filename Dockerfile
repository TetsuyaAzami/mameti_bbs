FROM alpine:3.15.4 as builder

ARG SBT_VERSION=1.6.2
RUN apk update && \apk add --no-cache openjdk11-jre-headless curl bash && \curl -L -o sbt-${SBT_VERSION}.tgz https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz && \tar -xvzf sbt-${SBT_VERSION}.tgz && \rm sbt-${SBT_VERSION}.tgz

ENV PATH $PATH:/sbt/bin

WORKDIR /app

RUN sbt -V

COPY . /app

RUN sbt dist


FROM openjdk:11-jre-slim


COPY --from=builder /app/target/universal/mameti_bbs-1.0-SNAPSHOT.zip mameti_bbs.zip

RUN apt update && \
	apt install -y unzip && \
	unzip mameti_bbs.zip

EXPOSE 9000

CMD [ "mameti_bbs-1.0-SNAPSHOT/bin/mameti_bbs" ]
