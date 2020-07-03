FROM openjdk:8u232-jre-slim
MAINTAINER im

ENV PARAMS="-Xmx256m -Xms256m -Xmn128m -Xss256k"

ADD app.jar /app.jar
ADD application.properties /application.properties
RUN sh -c 'touch /app.jar'
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar $PARAMS /app.jar" ]
