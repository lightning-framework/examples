# rethinkdb-demo

An application which demonstrates the use of Lightning with the popular distributed NoSQL database RethinkDB.

This application is a distributed, maximally-efficient real-time chat application. Clients establish a web socket connection to an application node and may send messages by writing to the web socket. New messages will be automatically pushed to the web sockets of all connected clients in real time without any polling needed thanks to the power of RethinkDB's change feed subscriptions.

## Requirements

* Lightning Framework >= 0.1
* RethinkDB (localhost:28015 w/ database "chatapp" and table "messages")

## Usage

* Import the project into your IDE as an existing Maven project.
* Ensure you have correctly installed and configured RethinkDB including creating the needed database.
* Modify ChatAppLauncher to configure any parameters you don't want to use the defaults for.
* Run ChatAppLauncher.
  * Arguments: NONE
  * Working Directory: rethinkdb-demo (the folder containing src/main/java)
