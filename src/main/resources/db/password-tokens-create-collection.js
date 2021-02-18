// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

// The schema validator for the password tokens collection
passwordTokenValidator = {
    $jsonSchema: {
        additionalProperties: false,
        properties: {
            _id: {
                bsonType: 'objectId'
            },
            user: {
                bsonType: 'objectId'
            },
            token: {
                bsonType: 'binData'
            },
            expiration: {
                bsonType: 'date'
            },
            _class: {
                bsonType: 'string'
            }
        }
    }
}

// Create the password tokens collection and add validator.
db.createCollection("password_tokens", {
    validator: passwordTokenValidator
})

// Create unique indexes
db.password_tokens.createIndex({ "token": 1 }, { unique: true })
