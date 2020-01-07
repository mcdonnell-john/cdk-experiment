/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
const AWS = require('aws-sdk');
const db = new AWS.DynamoDB.DocumentClient();
const TABLE_NAME = process.env.TABLE_NAME || '';

exports.handler = async function (event) {

    const params = {
        TableName: TABLE_NAME
    };

    try {
        const response = await db.scan(params).promise();
        return {statusCode: 200, body: JSON.stringify(response.Items)};
    } catch (dbError) {
        return {statusCode: 500, body: JSON.stringify(dbError)};
    }
};
