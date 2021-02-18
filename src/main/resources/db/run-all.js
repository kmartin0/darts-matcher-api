// In mongo shell run this script using load("path/to/script.js")
var projectPath = "C:/Users/kevin/Documents/git-projects/darts-matcher/"

load(projectPath + "src/main/resources/db/client-details-create-collection.js")
load(projectPath + "src/main/resources/db/users-create-collection.js")
load(projectPath + "src/main/resources/db/password-tokens-create-collection.js")
load(projectPath + "src/main/resources/db/matches-create-collection.js")
load(projectPath + "src/main/resources/db/insert-test-data.js")

// load("C:/Users/kevin/Documents/git-projects/darts-matcher/src/main/resources/db/run-all.js")

// load("C:/Users/kevin/Documents/git-projects/darts-matcher/src/main/resources/db/insert-test-data.js")