# couchsc
Relax. Domain model based persistence made easy.

CouchSC puts a domain model based REST-wrapper around CouchDB for simple persistence of domain model instances.

# Get it somehow running.

## Preconditions---CouchDB
0. Download a recent copy of CouchDB (https://couchdb.apache.org), install and start it.

## Precondition---ProcessEditor with WebModeler
1. Download the recent copy of https://github.com/frapu78/processeditor.
2. Build the jar-server executable following the documentation.
3. Start the jar-server executable and browse to http://localhost:1205
4. Login to the WebModeler (default credentials: root/inubit).
5. Import resources/fuhrpark.model.

## Running it!
6. Open the imported model "Fuhrpark" and copy the URL, e.g. http://localhost:1205/models/1520138062 - Last id will be different!
7. Open the CouchSCServer class and configure the modelLocation variable accordingly to the copied URL (as a source for the model to be used).
8. Point your browser to http://localhost:2107 and relax!


