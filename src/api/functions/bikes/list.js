"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");

module.exports.listBikes = async () => {
    try {
        const params = {
            TableName: process.env.BIKES_TABLE_NAME,
        };
        const bikes = await dynamoDb.scan(params).promise();
        return sendResponse(200, bikes.Items);
    } catch (e) {
        console.log(e);
        return sendResponse(500, { message: "Internal server error" });
    }
};



