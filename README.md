# couchsc
Relax. Domain model based persistence made easy.

CouchSC puts a domain model based REST-wrapper around CouchDB for simple persistence of domain model instances.

# Get it somehow running.

## Preconditions---CouchDB
0. Download a recent copy of CouchDB (https://couchdb.apache.org), install and start it.
0. You need Java 8.

## Precondition---ProcessEditor with WebModeler
0. Download the recent copy of https://github.com/frapu78/processeditor.
0. Build the jar-server executable following the documentation.
0. Start the jar-server executable and browse to http://localhost:1205
0. Login to the WebModeler (default credentials: root/inubit).
0. Import resources/fuhrpark.model.

## Running it!
0. Create a project in an IDE of your choice, add the content of this repository as well as the processeditor.jar file.
0. Open the imported model "Fuhrpark" in the WebModeler and copy the URL, e.g. http://localhost:1205/models/1520138062 - Last id will be different!
0. Open the CouchSCServer source and configure the modelLocation variable accordingly to the copied URL (as a source for the model to be used).
0. Configure the couchdb credentials.
0. Run the CouchSCServer.
0. Point your browser to http://localhost:2107 and relax!


