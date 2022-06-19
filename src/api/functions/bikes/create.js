"use strict";

const dynamoDb = require("../../config/dynamoDb");
const { sendResponse } = require("../../utils/common");
const uuid = require('uuid');

module.exports.createBike = async event => {
    const body = JSON.parse(event.body);
    const id = uuid.v1();
    try {
        const { manufacturer, location, active } = body;
        const used = false
        const TableName = process.env.BIKES_TABLE_NAME;
        const params = {
            TableName,
            Item: {
                id,
                manufacturer,
                location,
                active,
                used
            },
            ConditionExpression: "attribute_not_exists(id)"
        };
        await dynamoDb.put(params).promise();
        return sendResponse(200, { message: 'Bike created successfully with id ' + id });
    } catch (e) {
        return sendResponse(500, { message: 'Could not create the bike with id ' + id });
    }
};