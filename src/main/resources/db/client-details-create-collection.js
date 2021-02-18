// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

// The schema validator for the client details collection
clientDetailsSchemaValidator = {
    $jsonSchema: {
        additionalProperties: false,
        properties: {
            _id: {
                bsonType: 'objectId'
            },
            clientId: {
                bsonType: 'string'
            },
            clientSecret: {
                bsonType: 'string'
            },
            scope: {
                bsonType: 'string'
            },
            authorizedGrantTypes: {
                bsonType: 'string'
            },
            authorities: {
                bsonType: 'string'
            },
            accessTokenValidity: {
                bsonType: 'number'
            },
            refreshTokenValidity: {
                bsonType: 'number'
            },
            _class: {
                bsonType: 'string'
            }
        }
    }
}

// Create the client details collection and add validator.
db.createCollection("client_details", {
    validator: clientDetailsSchemaValidator
})

// Create unique indexes
db.client_details.createIndex({ "client_id": 1 }, { unique: true })