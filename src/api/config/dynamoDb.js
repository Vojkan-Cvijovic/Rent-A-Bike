const AWS = require("aws-sdk")

const dymano = new AWS.DynamoDB.DocumentClient();

module.exports = dymano