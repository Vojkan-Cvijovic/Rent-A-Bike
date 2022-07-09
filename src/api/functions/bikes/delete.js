"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.deleteBike = async event => {
    try {
        const body = JSON.parse(event.body);
        const { id } = body;
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
            Key: {
                id
            }
        };
        await dynamoDb.delete(params).promise();
        return sendResponse(200, { message: "Removed bike with id " + id});
    } catch (e) {
        console.log(e);
        return sendResponse(500, {message: "Internal server error"});
    }
};

