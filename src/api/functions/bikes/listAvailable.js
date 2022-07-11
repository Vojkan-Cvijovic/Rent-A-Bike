"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.listAvailableBikes = async event => {
    try {
        const { location } = event.pathParameters;
        const selectedLocation = decodeURI(location);
        console.log("Fetching bikes for location " + selectedLocation);
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
            // location is reserved keyword, using #dynobase_location instead
            FilterExpression: 'active = :active and used = :used and #dynobase_location = :location',
            ExpressionAttributeValues: {
                ':active': true,
                ':used': false,
                ':location': selectedLocation
            },
            ExpressionAttributeNames: { "#dynobase_location": "location" }
        };
        const bikes = await dynamoDb.scan(params).promise();
        return sendResponse(200, bikes.Items);
    } catch (e) {
        console.log(e);
        return sendResponse(500, { message: "Internal server error" });
    }
};



