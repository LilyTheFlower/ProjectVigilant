# ProjectVigilant
A smartphone app and server designed to make search and rescue operations a lot more straight-forward 

Introduction:
Welcome to Project Vigilant, a system desgined to alert the public in the event of natural disaster
or catastrophe and enable civilians to automatically register their identity, automatically log 
their location,and report whether or not they have reached safety.

Currently the app is only for Android, and the server is written in Javascript to interact with
a mongoDB database. The app communicates with the server via http request and the server updates
the database accordingly.

The use of the app is designed to be very simple and require minimum effort on the part of the user
so that they can focus on other tasks while their phone logs their location. In the event that 
a user becomes trapped, injured or incapacitated, during a catastrophe the application will have their last known 
location or possibly even current location logged so that search and rescue teams will have a 
better understanding of where survivors are located. By adding the ability for survivors to be 
able to self-report when they have made it to safety the search for survivors can be narrowed and
have a much more targeted approach to focus on those who are still listed as in danger.

The app does not always track user location, only in the event of emergency
and specifically requests permission to track the user's location when the feature is 
first activated. The unique identifier for a user is not based on the typical account and
password structure, and instead is reliant on the unique id of each android smartphone.
In the future this could possibly be updated to be based on phone number instead.
The system only includes the self-reported full name, and phone Id of each user as well
as their latitude and longitude and whether or not they are currently reported as safe.

###WARNING###
This iteration of the app and server are designed to be a proof of concept, not 
a final, fool-proof product. There are no security measures in place or user authentication
other than the use of android phone ids. This is partially intentional as the purpose is 
for any user to download the app and quickly be ready for natural disaster/catastrophe. 
However this also leaves the current system open to some kinds of malicious behaviour.
The system is not inherently dangerous to use, just not prepared for wide-scale deployment yet.


Configure the App:
-Go to the strings.xml file and place in the ip address and port of the server that 
the app will be communicating with.

Configure the Server:
-change the value of "port" to be whatever port the server will be listening on
-set the value of "uri" to the uri from the database

Configure the database: 
-Create a mongoDB database called "ProjectVigilant" 
-Add two collections, one called "Users" and the other called "Cities"


From here the app should be able to communicate to the server and the server
can communicate with the database.
