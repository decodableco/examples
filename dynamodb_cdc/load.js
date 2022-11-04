// Load the AWS SDK for JS
var AWS = require("aws-sdk");
var randomstring = require("randomstring");
AWS.config.update({ region: "us-west-2" });

// -----------------------------------------
// Create the document client interface for DynamoDB
var documentClient = new AWS.DynamoDB.DocumentClient();

console.log("Loading data into DynamoDB");

for (let i = 0; i < 1000; i++) {

    random = randomstring.generate(7);
    var params = {
        TableName: "hubert_dynamocdc",
        Item: {
            "userid": i,
            "first_name": random,
            "last_name": random,
            "phone": i
        }
    };

    documentClient.put(params, function (err, data) {
        if (err) {
            console.error(err);
        } else {
            console.log("Succeeded adding an item  ");
        }
    });
};
