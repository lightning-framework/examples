# tinyurl

A simple URL-shortener that requires users to log in using CAS in order to create, manage, and delete their short URLs.

## Requirements

* Lightning Framework >= 0.1
* MySQL >= 5
* CAS Server

## Usage

* Import the project into your IDE as an existing Maven project.
* Create a new MySQL database and import the schema provided in this project and the schema provided by lightning.
* Modify the configuration template to fit your database and port requirements.
* Run the launcher (TinyUrlApp).
  * Arguments: --config /path/to/config.json
  * Working Directory: tinyurl (the folder containing src/main/java)
