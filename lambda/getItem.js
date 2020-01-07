/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
const AWS = require('aws-sdk');
const db = new AWS.DynamoDB.DocumentClient();
const TABLE_NAME = process.env.TABLE_NAME || '';
const PRIMARY_KEY = process.env.PRIMARY_KEY || '';

exports.handler = async function (event) {

    const requestedItemId = event.pathParameters.id;
    if (!requestedItemId) {
        return {statusCode: 400, body: `Error: You are missing the path parameter id`};
    }

    const params = {
        TableName: TABLE_NAME,
        Key: {
            [PRIMARY_KEY]: requestedItemId
        }
    };

    try {
        const response = await db.get(params).promise();
        return {statusCode: 200, body: JSON.stringify(response.Item)};
    } catch (dbError) {
        return {statusCode: 500, body: JSON.stringify(dbError)};
    }
};