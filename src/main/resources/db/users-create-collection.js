// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

// The schema validator for the users collection
usersSchemaValidator = {
    $jsonSchema: {
        additionalProperties: false,
        properties: {
            _id: {
                bsonType: 'objectId'
            },
            userName: {
                bsonType: 'string'
            },
            firstName: {
                bsonType: 'string'
            },
            lastName: {
                bsonType: 'string'
            },
            email: {
                bsonType: 'string'
            },
            password: {
                bsonType: 'string'
            },
            _class: {
                bsonType: 'string'
            }
        }
    }
}

// Create the users collection and add validator.
db.createCollection("users", {
    validator: usersSchemaValidator
})

// Create unique indexes
db.users.createIndex({ "userName": 1 }, { unique: true })
db.users.createIndex({ "email": 1 }, { unique: true })

// Create text search index
db.users.createIndex({userName: "text", firstName: "text", lastName: "text"})