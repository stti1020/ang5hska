# README #

#DEVELOPMENT
1. Terminal in Ordner öffnen, indem das Readme liegt und in den Ordner Code navigieren
2. Nodejs installieren → 
	1. *yum install epel-release*
	2. *curl --silent --location https://rpm.nodesource.com/setup_8.x | sudo bash -*
	3. *yum install -y nodejs*
3. Globale Pakete installieren 
	1.Angular installieren → *npm install -g @angular/cli*
	2.Bower installieren → *npm install -g bower*
4. In den Ordner dlt-routing-webapp navigieren 
	1. Node-Pakete installieren → *npm install*
	2. Anwendung starten → *ng serve --open --poll=2000* 
	3. Browser öffnen → localhost:4200



#Production
1. Terminal in Ordner öffnen, indem das Readme liegt
2. Docker-Image bauen *docker build -t dlt-routing-webapp-image .*
3. Docker-Container erstellen *docker run -dit --name dlt-routing-webapp-container -p 4200:80 dlt-routing-webapp-image:latest*
4. Auf den Docker-Container verbinden *docker exec -it dlt-routing-webapp-container bash*
5. Appache2 starten */etc/init.d/apache2 start*
6. Container wieder verlassen *exit*
7. IP-Ad2resse des Containers herausfinden *ifconfig* (In der Regel beginnt die IP-Adresse mit 172.X.X.X)
8. Web-Server im Browser aufrufen *172.X.X.X:4200* (Nun sollte die Oberfläche erscheinen)

ToDo:
Beim Restservice muss die URL des SDK's angepasst werden

