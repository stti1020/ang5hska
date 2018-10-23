FROM ubuntu

RUN apt-get update
RUN apt-get -y install apache2
RUN apt-get -y  install curl
RUN apt-get -y  install gnupg
RUN curl -sL https://deb.nodesource.com/setup_8.x |  bash 
RUN apt-get -y  install nodejs
RUN npm install -g @angular/cli --unsafe

ADD code /code
WORKDIR /code

RUN npm install
RUN ng build --prod
RUN rm /var/www/html/index.html	
RUN cp -r dist/* /var/www/html/
RUN /etc/init.d/apache2 start

EXPOSE 80
