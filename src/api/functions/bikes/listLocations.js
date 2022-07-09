"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");
const Set = require("collections/set");

module.exports.listLocations = async () => {
    try {
        const params = {
            TableName: process.env.BIKES_TABLE_NAME
        };
        const bikes = await dynamoDb.scan(params).promise();
        const locations = new Set();
        bikes.Items.forEach(function (bike) {
            // console.log(bike);
            if (!locations.has(bike.location)) {
                locations.add(bike.location)
            }
        })
        return sendResponse(200, Array.from(locations));
    } catch (e) {
        console.log(e);
        return sendResponse(500, { message: "Internal server error" });
    }
};



