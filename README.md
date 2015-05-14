# Live Chat Engine

Open source system for live chats with visitors (customers) in any site.
Contains server-side (chats storing, users and billing management), operators cabinet for chatting and chat form for site's visitors.

*Chat Form on a site:*
![Operators Cabinet](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/live-chat-client.png)


*Operators Cabinet:*
![Operators Cabinet](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/live-chat-cabinet.png)

## Two work modes 
### Software as a Service

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
- run components by **run-all.bat** or **run-all.sh**

## Engine components

### database-h2-server
Included database server by default. Use for tests. For production use PostgreSQL.

### chat-central-server
Server for caches and synchronization.

### chat-front-server
Web server for site and operators online cabinet. 

### chat-node-server
Storage for chats accounts.

## Demployment
### Simple deployment scheme
Only one instance of front and node servers:
![deploy-scheme-simple](https://raw.githubusercontent.com/edolganov/live-chat-engine/master/extra/images/deploy-scheme-simple.png)

### Production deployment scheme
Load balancer (for example: nginx) for multi front instances, new storages for new accounts: