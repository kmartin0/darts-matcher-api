// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

/*=================================================================
========================= CLIENT DETAILS ==========================
=================================================================*/

// Create the client details collection and add validator.
db.createCollection("client_details", {
    validator: clientDetailsSchemaValidator
})

// Create unique indexes
db.client_details.createIndex({"client_id": 1}, {unique: true})

/*=================================================================
============================== USERS ==============================
=================================================================*/

// Create the users collection and add validator.
db.createCollection("users", {
    validator: usersSchemaValidator
})

// Create unique indexes
db.users.createIndex({"userName": 1}, {unique: true})
db.users.createIndex({"email": 1}, {unique: true})

// Create text search index
db.users.createIndex({userName: "text", firstName: "text", lastName: "text"})

/*=================================================================
========================= PASSWORD TOKENS =========================
=================================================================*/

// Create the password tokens collection and add validator.
db.createCollection("password_tokens", {
    validator: passwordTokenValidator
})

// Create unique indexes
db.password_tokens.createIndex({"token": 1}, {unique: true})

/*=================================================================
============================= MATCHES =============================
=================================================================*/

// Create the matches collection and add validator.
result = db.createCollection("matches", {
    validator: matchesSchemaValidator
})

print(result)