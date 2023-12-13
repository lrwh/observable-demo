FROM openjdk:8-jdk-alpine

# 其效果是在主机 /var/lib/docker 目录下创建了一个临时文件，并链接到容器的/tmp
VOLUME /tmp

WORKDIR /data
RUN mkdir logs
# 这个目录自行修改
ADD springboot-server.jar app.jar
ADD ../opentelemetry-javaagent/opentelemetry-javaagent-1.13.1.jar /opentelemetry-javaagent.jar
# 修改时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# 解决中文乱码
ENV LANG en_US.UTF-8
ENV jar app.jar

ENTRYPOINT ["sh", "-ec", "exec java ${JAVA_OPTS} -jar ${jar} ${PARAMS} 2>&1"]
