// In mongo shell run this script using load("projectPath/src/main/resources/db/run-all.js")
var projectPath = "PATH_TO_PROJECT"

load(projectPath + "src/main/resources/db/create-collections.js")
load(projectPath + "src/main/resources/db/insert-test-data.js")
