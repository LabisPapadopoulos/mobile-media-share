1. Install PostgreSQL 9.1
sudo apt-get install postgresql

2. Install Tomcat 7
sudo apt-get install tomcat7
sudo apt-get install tomcat7-admin

3. Edit /var/lib/tomcat7/conf/tomcat-users.xml to add users

4. Created keystore and edited /var/lib/tomcat7/conf/server.xml to enable SSL.
	$ sudo keytool -genkeypair -keystore /var/lib/tomcat7/conf/.keystore 

5. Install GWT from http://www.gwtproject.org/

6. Set environment variable GWT_HOME

7. 
