FROM ubuntu

RUN mkdir /home/questions
RUN mkdir /home/questions/data
RUN mkdir /home/questions/run

ADD target/questions.jar /home/questions/run/questions.jar
ADD config /home/questions/config

RUN apt-get update

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN yes Y | apt-get install r-base r-cran-rserve
RUN yes Y | apt-get install nodejs

ENTRYPOINT ["java -jar /home/questions/run/questions.jar"]