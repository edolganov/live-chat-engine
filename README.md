# Live Chat Engine

Open source system for live chats with visitors (customers) in any site.
Contains server-side (chats storing, users and billing management), operators cabinet for chatting and chat form for site's visitors.

*Chat Form on a site:*
![Operators Cabinet](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/live-chat-client.png)


*Operators Cabinet:*
![Operators Cabinet](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/live-chat-cabinet.png)

## Two work modes 

### Software as a Service (SaaS mode)

In [SaaS mode](https://en.wikipedia.org/wiki/Software_as_a_service) clients create accounts for themselves and pay monthly bills.
This mode is ready for production usage. 

Example of implementation: [cheapchat.me](http://cheapchat.me)

Other popular examples: [olark](https://www.olark.com/), [livezilla](http://www.livezilla.net/), [boldchat](https://www.boldchat.com/) and others

### Tool for Other System (Tool mode)
In Tool Mode admin creates accounts, user can be only a operator, no monthly bills. You can use Tool Mode for live chats for single site with own hosting for example.

**Tool mode is not completed yet! Implementation is in progress...**

## Requirements
- Java 8
- PostgreSQL or default H2DB (included to the build)
- Windows (tested on Win7) / Linux (tested on Ubuntu)

## How to build
- download last version
- check Java version (print "java -version" in a console)
- start **build.bat** (win) or **build.sh** (linux)
- on BUILD SUCCESSFUL message see **/build/_servers** dir
- copy all files from **/build/_servers** to your work dir
- run all components by single runner: **run-all.bat** (win) or **run-all.sh** (linux)
- open browser with url [127.0.0.1:10280](http://127.0.0.1:10280/) for access to index page
- sign in to cabinet with **admin** / **admin** login and password

## Engine components

### database-h2-server
Included database server by default. Use for tests. For production use PostgreSQL.

Run by /run.bat (run.sh).

By default H2 creates **~/livechat** db file.

----------

### chat-central-server
Server for caches and synchronization.

Run by /run.bat (run.sh).

Configs and logs:

- /config.properties - configs for central server
- /net-props/* - common configs for all servers
- /server.log - logs


----------

### chat-front-server
Web server for site and operators online cabinet. 
Powered by [Tomcat 7 Web Server](http://tomcat.apache.org/). 

Run by /bin/run.bat (run.sh).


Configs and logs:

- /conf - web server conf ([see Tomcat docs](http://tomcat.apache.org/tomcat-7.0-doc/introduction.html))
- /webapps/ROOT/WEB-INF/front.properties - special front app configs
- /logs/catalina.log - logs


----------

### chat-node-server
Storage for chats accounts.
Powered by [Tomcat 7 Web Server](http://tomcat.apache.org/).  

Run by /bin/run.bat (run.sh).

Configs and logs:

- /conf - web server conf ([see Tomcat docs](http://tomcat.apache.org/tomcat-7.0-doc/introduction.html))
- /webapps/ROOT/WEB-INF/chat.properties - special node app configs
- /logs/catalina.log - logs
- /data/accounts - created chat accounts

## Demployment
### Simple deployment scheme
Only one instance of front and node servers:

![deploy-scheme-simple](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/deploy-scheme-simple.png)

### Production deployment scheme
Load balancer (for example: nginx) for multi front instances, new storages for new accounts:

![deploy-scheme-prod](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/deploy-scheme-prod.png)