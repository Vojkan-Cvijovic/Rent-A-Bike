"use strict"

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.updateBikeUsed = async event => {
    try {
        const body = JSON.parse(event.body);
        const {id, used} = body;
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
            Key: {
                id
            },
            ExpressionAttributeValues: {
                ":used": used
            },
            UpdateExpression:
                "SET used = :used",
            ReturnValues: "ALL_NEW"
        };
        const data = await dynamoDb.update(params).promise();
        if (data.Attributes) {
            return sendResponse(200, data.Attributes);
        } else {
            return sendResponse(404, { message: "Could not find bike with id " + id });
        }
    } catch (e) {
        console.log(e);
        return sendResponse(500, { message: "Internal server error" });
    }
};
