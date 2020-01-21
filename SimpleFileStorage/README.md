
# SimpleFileStorage
SimpleFileStorage is a REST API service that accepts any type of binary file and supports versioning.  Users can create/edit/delete/list file(s).

Versioning is handled by an incrementing number prefix to the filename. (i.e. 1_filename.txt, 2_filename.txt, ....)

Deleting a file will delete all versions of the file. - might want to refactor this based on requirements.

## Installation
TODO: Describe the installation process

## API End Points

 # Upload File
 - URL - /UploadFile
 - Method - POST
 - URL Params - None
 - Data Params - Single File (file name cannot use ('_') because of versioning)
 - Success Response - 200
 - Error Response - 400
 
 # Download File
 - URL - /DownloadFile/{fileName}
 - Method - GET
 - URL Params - fileName
 - Data Params - None
 - Success Response - 200
 - Error Response - 404
 
 # Delete File
 - URL - /DeleteFile/{fileName}
 - Method - GET
 - URL Params - fileName
 - Data Params - None
 - Success Response - 200
 - Error Response - 404
 
 # List Files
 - URL - /ListFiles
 - Method - GET
 - URL Params - None
 - Data Params - None
 - Success Response - 200
 - Error Response - 400

## Built With
 - JAVA jdk-13.0.1
 - MAVEN apache-maven-3.6.3
 - Eclipse IDE for Java Developers V 2019-12 (4.14.0)
 - Postman V7.16.0

## Technical Details
 - Max File Size: 200MB
 - Upload directory: ./uploads
 - Server port: 8081