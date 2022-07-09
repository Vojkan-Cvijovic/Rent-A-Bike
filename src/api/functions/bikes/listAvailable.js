"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.listAvailableBikes = async () => {
    try {
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
            FilterExpression: 'active = :active and used = :used',
            ExpressionAttributeValues: {
                ':active': true,
                ':used': false
            }
        };
        const bikes = await dynamoDb.scan(params).promise();
        return sendResponse(200, bikes.Items);
    } catch (e) {
        console.log(e);
        return sendResponse(500, { message: "Internal server error" });
    }
};



