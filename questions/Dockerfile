FROM java:8

RUN adduser questions

USER questions

RUN mkdir ~/data
RUN mkdir ~/.config
RUN mkdir ~/run

ADD target/questions.jar /home/questions/run/questions.jar
ADD config /home/questions/.config
ADD init.sh /home/questions/init.sh

USER root

RUN chown -R questions /home/questions
RUN chmod +x /home/questions/init.sh

RUN apt-get update

USER questions

ENTRYPOINT ["/home/questions/init.sh"]

