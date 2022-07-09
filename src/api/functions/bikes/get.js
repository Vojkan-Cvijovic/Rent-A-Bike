"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.getBike = async event => {
    try {
        const { id } = event.pathParameters;
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
            KeyConditionExpression: "id = :id",
            ExpressionAttributeValues: {
                ":id": id
            },
            Select: "ALL_ATTRIBUTES"
        };

        const data = await dynamoDb.query(params).promise();
        if (data.Count > 0) {
            return sendResponse(200, data.Items);
        } else {
            return sendResponse(404, {message: "Bike not found"});
        }
    } catch (e) {
        console.log(e);
        return sendResponse(500, {message: "Internal error"});
    }
};


