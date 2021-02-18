// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

// ========================= CLIENT DETAILS =========================

// Insert Client Details (clientSecret = secret)
clientDetailsResult = db.client_details.insert([
    {
        "clientId": "darts-matcher-web",
        "clientSecret": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "scope": "all",
        "authorizedGrantTypes": "password,refresh_token,client_credentials",
        "authorities": "ROLE_CLIENT",
        "accessTokenValidity": 172800,
        "refreshTokenValidity": 604800,
        "_class": "ClientDetailsImpl"
    }
])
print(clientDetailsResult)

// // ========================= USERS =========================

// Create reference user id's
user1ObjectId = ObjectId()
user2ObjectId = ObjectId()

// Insert users (password = secret)
usersResult = db.users.insert([
    {
        "_id": user1ObjectId,
        "userName": "JohnDoe1",
        "firstName": "John",
        "lastName": "Doe",
        "email": "johndoe1@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "_class": "User"
    },
    {
        "_id": user2ObjectId,
        "userName": "JaneDoe1",
        "firstName": "Jane",
        "lastName": "Doe",
        "email": "janedoe1@email.com",
        "password": "$2a$12$o3dmbF3ElqPL1ApJ.9R/Qu7cVBMyV8pn80.HPFPdKO/jerqGJiXZe",
        "_class": "User"
    }
])
print(usersResult)

// ========================= PASSWORD TOKENS =========================

// Insert Password Tokens
passwordResult = db.password_tokens.insert([
    {
        "user": user1ObjectId,
        "token": new UUID('7f019466-3845-443c-abf6-09780ca64fc2'),
        "expiration": new Date(ISODate().getTime() - 1000 * 3600 * 24 * 7),
        "_class": "PasswordToken"
    }
])

print(passwordResult)

// ========================= MATCHES =========================

// Insert Matches
matchesResult = db.matches.insert([
    {
        "player1": user1ObjectId,
        "player2": user2ObjectId,
        "startDate": new Date("2021-01-02T14:12:00Z"),
        "endDate": new Date("2021-01-02T14:32:00Z"),
        "gameType": "501",
        "gameStatus": "concluded",
        "throwFirst": "player1",
        "bestOf": {
            "legs": 3,
            "sets": 1
        },
        "result": {
            "winner": "player1",
            "player1": 2,
            "player2": 1
        },
        "statistics": {
            "player1": {
                "average": 113,
                "tonPlus": 3,
                "tonFourty": 4,
                "tonEighty": 1,
                "checkoutHighest": 31,
                "checkoutTonPlus": 0,
                "checkoutPercentage": 40,
                "checkoutsMissed": 5,
                "checkoutsHit": 2
            },
            "player2": {
                "average": 113,
                "tonPlus": 2,
                "tonFourty": 0,
                "tonEighty": 3,
                "checkoutHighest": 141,
                "checkoutTonPlus": 1,
                "checkoutPercentage": 100,
                "checkoutsMissed": 0,
                "checkoutsHit": 1
            }
        },
        "timeline": [{
            "set": 1,
            "leg": 1,
            "winner": "player1",
            "doublesMissed": {
                "player1": 3,
                "player2": 0
            },
            "matchDart": 2,
            "scoring": [{
                "player1": 140,
                "player2": 120
            }, {
                "player1": 100,
                "player2": 88
            }, {
                "player1": 90,
                "player2": 27
            }, {
                "player1": 140,
                "player2": 180
            }, {
                "player1": 31
            }]
        }, {
            "set": 1,
            "leg": 2,
            "winner": "player2",
            "doublesMissed": {
                "player1": 0,
                "player2": 0
            },
            "matchDart": 3,
            "scoring": [{
                "player1": 140,
                "player2": 180
            }, {
                "player1": 100,
                "player2": 180
            }, {
                "player2": 141
            }]
        }, {
            "set": 1,
            "leg": 3,
            "winner": "player1",
            "doublesMissed": {
                "player1": 2,
                "player2": 0
            },
            "matchDart": 2,
            "scoring": [{
                "player1": 121,
                "player2": 120
            }, {
                "player1": 180,
                "player2": 88
            }, {
                "player1": 90,
                "player2": 27
            }, {
                "player1": 90,
                "player2": 43
            }, {
                "player1": 20
            }]
        }],
        "_class": "Match"
    }
])
print(matchesResult)