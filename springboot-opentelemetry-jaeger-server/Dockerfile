FROM openjdk:8-jdk-alpine

# 其效果是在主机 /var/lib/docker 目录下创建了一个临时文件，并链接到容器的/tmp
VOLUME /tmp

WORKDIR /data
RUN mkdir logs
# 这个目录自行修改
ADD ../target/springboot-opentelemetry-jaeger-client.jar app.jar
ADD ../../opentelemetry-javaagent/opentelemetry-javaagent.jar opentelemetry-javaagent.jar
# 修改时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 解决中文乱码
ENV LANG en_US.UTF-8

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.jar --spring.profiles.active=docker --client=false
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar","--spring.profiles.active=docker"]
