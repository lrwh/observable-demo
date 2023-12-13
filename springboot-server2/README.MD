# springboot-server2

基于 springboot-server，调整了项目部署方式，将应用jar与依赖分离，缩小了应用jar体积，提升了部署效率。

## 调整

主要做如下调整

```xml
<plugins>
<!--            <plugin>-->
<!--                <groupId>org.springframework.boot</groupId>-->
<!--                <artifactId>spring-boot-maven-plugin</artifactId>-->
<!--            </plugin>-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <!-- lib依赖包输出目录，打包的时候不打进jar包里 -->
                            <outputDirectory>${project.build.directory}/lib</outputDirectory>
                            <excludeTransitive>false</excludeTransitive>
                            <stripVersion>false</stripVersion>
                            <includeScope>runtime</includeScope>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <!-- 压缩jar包，打出来的jar中没有了lib文件夹 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <layout>ZIP</layout>
                    <includes>
                        <include>
                            <groupId>nothing</groupId>
                            <artifactId>nothing</artifactId>
                        </include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
```
## 打包

> mvn clean package

## RUN

进入target 目录执行以下命令

>java -jar -Dloader.path="lib/" springboot-server2.jar

```shell
liurui@liurui:~/code/observable-demo/springboot-server2/target$ java -jar -Dloader.path="lib/" springboot-server2.jar 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v1.3.5.RELEASE)

2023-12-13 10:21:56.845 [main] INFO  c.z.o.s.ServerApplication - [logStarting,48] [traceId= spanId=] - Starting ServerApplication v0.0.1-SNAPSHOT on liurui with PID 7625 (/home/liurui/code/observable-demo/springboot-server2/target/springboot-server2.jar started by liurui in /home/liurui/code/observable-demo/springboot-server2/target)
2023-12-13 10:21:56.854 [main] INFO  c.z.o.s.ServerApplication - [logStartupProfileInfo,670] [traceId= spanId=] - The following profiles are active: dev
2023-12-13 10:21:57.461 [background-preinit] INFO  o.h.v.i.util.Version - [<clinit>,30] [traceId= spanId=] - HV000001: Hibernate Validator 5.2.4.Final
2023-12-13 10:21:58.658 [main] INFO  o.a.c.c.StandardService - [log,180] [traceId= spanId=] - Starting service Tomcat
2023-12-13 10:21:58.660 [main] INFO  o.a.c.c.StandardEngine - [log,180] [traceId= spanId=] - Starting Servlet Engine: Apache Tomcat/8.0.33
2023-12-13 10:21:58.771 [localhost-startStop-1] INFO  o.a.c.c.C.[.[.[/] - [log,180] [traceId= spanId=] - Initializing Spring embedded WebApplicationContext
2023-12-13 10:21:59.923 [main] INFO  o.a.c.h.Http11NioProtocol - [log,180] [traceId= spanId=] - Initializing ProtocolHandler ["http-nio-8090"]
2023-12-13 10:21:59.936 [main] INFO  o.a.c.h.Http11NioProtocol - [log,180] [traceId= spanId=] - Starting ProtocolHandler ["http-nio-8090"]
2023-12-13 10:21:59.945 [main] INFO  o.a.t.u.n.NioSelectorPool - [log,180] [traceId= spanId=] - Using a shared selector for servlet write/read
2023-12-13 10:21:59.989 [main] INFO  c.z.o.s.ServerApplication - [logStarted,57] [traceId= spanId=] - Started ServerApplication in 3.767 seconds (JVM running for 4.366)

```
