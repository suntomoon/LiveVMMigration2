Introduction
This program is a simulation of live virtual machine migration.
1 Manager, 2 Servers, user can control servers by manager, for example, create vm, stop vm, etc

How to build?
This program is implemented by Java with Eclipse, load the project into Eclipse, and build them together.

How to run?
1. Open a command line console, type "java Manager"
2. Open two command line consoles, input "java Server" in each of them
3. Go to manager console, type "create -s ServerX -v vm1"
4. Go to manager console, type "migrate -s ServerX -v vm1 -t ServerXX"
5. Go to manager console, type "h" for more commands as the following:

Usage:
create -s <Server> -v <VM>
start -s <Server> -v <VM>
stop -s <Server> -v <VM>
set -p <MigrationPolicy>
get -p mp
migrate -s <Server> -v <VM> -t <Server>