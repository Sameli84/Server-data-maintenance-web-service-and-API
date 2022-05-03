FROM maven:3.8.5-amazoncorretto-17
WORKDIR api
COPY . .
RUN mvn clean install
CMD mvn spring-boot:run