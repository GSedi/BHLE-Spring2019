FROM openjdk:8

# configs and logback
RUN mkdir -p /root/config/

COPY ./src/main/resources/*logback.xml /root/config/
COPY ./src/main/resources/*.conf /root/config/

WORKDIR /root
COPY ./target/universal/app.zip /root/
RUN unzip -q app.zip
WORKDIR /root/app/bin

# clean zip
RUN rm /root/app.zip

CMD chmod +x docker-bhle-lab-7

CMD ["/bin/bash", "-c", "./docker-bhle-lab-7 -Dconfig.file=/root/config/application.conf -Dlogback.configurationFile=/root/config/logback.xml"]