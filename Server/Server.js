async function beginDataBaseConnection(){
    //get the database connection set up
    const {MongoClient} = require('mongodb');

    const uri = "mongodb://localhost:27017"

    const client = new MongoClient(uri);


    try {
        await client.connect(); 
        beginServerFunctions(client);

    } catch (error) {
    
    }finally{
        await client.close
    }
}

async function beginServerFunctions(client){
    //start performing server functions
    var express = require('express'),
    //set an instance of exress
    app = express(),
    //require the body-parser nodejs module
    bodyParser = require('body-parser'),
    //require the path nodejs module
    path = require("path");

    //support parsing of application/json type post data
    app.use(bodyParser.json());
 
    const port = 8080;

    app.post("/api/setUserInformation", async function(req, res){
    //get the user info, and then send back a response indicating that we got the info
        const resultFind = await client.db("ProjectVigilant").collection("Users").findOne({
            _id: req.body.phoneId
        });

        if(resultFind){
            //found a user with this phone id
            const resultUpdate = await client.db("ProjectVigilant").collection("Users").updateOne({
                _id: req.body.phoneId
            }, {$set:{
                fullName:req.body.fullName
            }});
        }else{
            const resultUpdate = await client.db("ProjectVigilant").collection("Users").insertOne({
                _id: req.body.phoneId,
                fullName: req.body.fullName
            });
        }
        res.write("{response : ok}");
        res.send();
    });

    app.post("/api/shouldIActivate", async function(req, res) {
    //open up the JSON and query the database for the city and figure out if there is an emergency declared there
        if(req.method === "POST"){
            const resultFind = await client.db("ProjectVigilant").collection("Cities").findOne({
                Country: req.body.country,
                State: req.body.state,
                City: req.body.city
            });

            if(resultFind){
                res.write("{code : " + resultFind.emergency + "}");
                res.send();
            }else{
                console.log("No such City")
            }
        }
    });

    app.post("/api/updateLocation", async function(req, res){
         //open up JSON and write to the database the new location for the phone Id that was sent
        if(req.method === "POST"){
            const resultFind = await client.db("ProjectVigilant").collection("Users").findOne({
                _id: req.body.phoneId
            });
    
            if(resultFind){
                //found a user with this phone id
                const resultUpdate = await client.db("ProjectVigilant").collection("Users").updateOne({
                    _id: req.body.phoneId
                }, {$set:{
                    latitude: req.body.y,
                    longitude: req.body.x,
                    safe: false
                }});
            }else{
                console.log("No such User");
            }
            res.write("{response : ok}");
            res.send();
        }
    });

    app.post("/api/deactivateUser", async function(req, res){
        //open up JSON and write to the database the new location for the phone Id that was sent
       if(req.method === "POST"){
           const resultFind = await client.db("ProjectVigilant").collection("Users").findOne({
               _id: req.body.phoneId
           });
   
           if(resultFind){
               //found a user with this phone id
               const resultUpdate = await client.db("ProjectVigilant").collection("Users").updateOne({
                   _id: req.body.phoneId
               }, {$set:{
                    safe: true
               }});
               
           }else{
               console.log("No such User");
           }
           res.write("{response : ok}");
           res.send();
       }
   });


    app.listen(port, () => {
        console.log('Server is running on port ' + port);
    });
}

//start the server
beginDataBaseConnection();