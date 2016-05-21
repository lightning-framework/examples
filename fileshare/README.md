# fileshare

A simple file sharing application which allows users to upload a file, store the file in a database, get a sharing link, and use the sharing link to download a copy of the uploaded file.

## Requirements

* Lightning Framework >= 0.1
* MySQL >= 5


## Usage

* Import the project into your IDE as an existing Maven project.
* Create a new MySQL database and import the schema provided in this project and the schema provided by lightning.
* Modify the configuration template to fit your database and port requirements.
* Run the launcher (FileShareApp).
  * Arguments: --config /path/to/config.json
  * Working Directory: fileshare (the folder containing src/main/java)

