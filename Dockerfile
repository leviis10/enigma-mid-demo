FROM openjdk:17-alpine

ADD target/midtrans-0.0.1-SNAPSHOT.jar midtrans.jar

CMD ["java", "-jar", "midtrans.jar"]