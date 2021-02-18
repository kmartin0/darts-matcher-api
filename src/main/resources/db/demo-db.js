// Database connection
conn = new Mongo()
db = conn.getDB("demo-db")

//**************************************************/
// CLIENT_DETAILS
//**************************************************/

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

// Create the users collection and add validator.
db.createCollection("client_details", {
    validator: clientDetailsSchemaValidator
})

// Create unique indexes
db.client_details.createIndex({ "client_id": 1 }, { unique: true })

// Insert demo data (clientSecret = secret)
db.client_details.insert([
    {
        "clientId": "demo-client",
        "clientSecret": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "scope": "all",
        "authorizedGrantTypes": "password,refresh_token,client_credentials",
        "authorities": "ROLE_CLIENT",
        "accessTokenValidity": 172800,
        "refreshTokenValidity": 604800,
        "_class": "ClientDetailsImpl"
    }
])

//**************************************************/
// USERS
//**************************************************/

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

// Insert users (password = secret)
johnDoeObjectId = ObjectId()
janeDoeObjectId = ObjectId()

usersResult = db.users.insert([
    {
        "_id": johnDoeObjectId,
        "userName": "Johndoe1",
        "firstName": "John",
        "lastName": "Doe",
        "email": "johndoe1@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "_class": "User"
    },
    {
        "_id": janeDoeObjectId,
        "userName": "Janedoe1",
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "janedoe1@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "_class": "User"
    }
])

//**************************************************/
// PASSWORD_TOKENS
//**************************************************/

// The schema validator for the password token collection
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

// Create the users collection and add validator.
db.createCollection("password_tokens", {
    validator: passwordTokenValidator
})

// Create unique indexes
db.password_tokens.createIndex({ "token": 1 }, { unique: true })

// Insert Password Tokens
usersResult = db.password_tokens.insert([
    {
        "user": johnDoeObjectId,
        "token": new UUID('7f019466-3845-443c-abf6-09780ca64fc2'),
        "expiration": new Date(ISODate().getTime() - 1000 * 3600 * 24 * 7),
        "_class": "PasswordToken"
    }
])