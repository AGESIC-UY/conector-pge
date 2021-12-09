```
Para ejecutar tomcat en linux se deben ejecutar desde la carpeta bin dentro de la carbeta base de Tomcat los siguientes comendos:

chmod -R 777 startup.sh
chmod -R 777 catalina.sh
chmod -R 777 setenv.sh
chmod -R 777 shutdown.sh
sed -i -e 's/\r$//' startup.sh
sed -i -e 's/\r$//' catalina.sh
sed -i -e 's/\r$//' setenv.sh
```
