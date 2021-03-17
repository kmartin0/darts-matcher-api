// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")


resultEnum = {
    'enum': [
        "WIN",
        "DRAW",
        "LOSE"
    ]
}

result = {
    score: {bsonType: 'number'},
    result: resultEnum
}

statistics = {
    average: {
        bsonType: 'number'
    },
    tonPlus: {
        bsonType: 'number'
    },
    tonForty: {
        bsonType: 'number'
    },
    tonEighty: {
        bsonType: 'number'
    },
    checkoutHighest: {
        bsonType: 'number'
    },
    checkoutTonPlus: {
        bsonType: 'number'
    },
    checkoutPercentage: {
        bsonType: 'number'
    },
    checkoutsMissed: {
        bsonType: 'number'
    },
    checkoutsHit: {
        bsonType: 'number'
    }
}

timelineItem = {
    properties: {
        set: {
            bsonType: 'number'
        },
        result: resultEnum,
        legs: {
            bsonType: 'array',
            items: {
                properties: {
                    leg: {
                        bsonType: 'number'
                    },
                    result: resultEnum,
                    dartsUsedFinalThrow: {
                        bsonType: 'number'
                    },

                    doublesMissed: {
                        bsonType: 'number'
                    },
                    scoring: {
                        bsonType: 'array',
                        items: {
                            bsonType: 'number'
                        }
                    }
                }
            }
        }
    }
}

// The schema validator for the matches collection
matchesSchemaValidator = {
    $jsonSchema: {
        additionalProperties: false,
        properties: {
            _id: {
                bsonType: 'objectId'
            },
            startDate: {
                bsonType: 'date'
            },
            endDate: {
                bsonType: 'date'
            },
            matchType: {
                'enum': [
                    'MATCH_101',
                    'MATCH_201',
                    'MATCH_301',
                    'MATCH_401',
                    'MATCH_501'
                ]
            },
            matchStatus: {
                'enum': [
                    'IN_PLAY',
                    'CONCLUDED'
                ]
            },
            throwFirst: {
                bsonType: [
                    'objectId',
                    'string'
                ]
            },
            bestOf: {
                bsonType: 'object',
                properties: {
                    legs: {
                        bsonType: 'number'
                    },
                    sets: {
                        bsonType: 'number'
                    },
                    type: {
                        'enum': [
                            'SETS',
                            'LEGS'
                        ]
                    }
                }
            },
            players: {
                bsonType: 'object',
                properties: {
                    registered: {
                        bsonType: 'array',
                        items: {
                            bsonType: 'object',
                            properties: {
                                playerId: {
                                    bsonType: 'objectId'
                                },
                                result: {
                                    bsonType: 'object',
                                    properties: result
                                },
                                statistics: {
                                    bsonType: 'object',
                                    properties: statistics
                                },
                                timeline: {
                                    bsonType: 'array',
                                    items: timelineItem
                                }
                            }
                        }
                    },
                    anonymous: {
                        bsonType: 'array',
                        items: {
                            bsonType: 'object',
                            properties: {
                                playerId: {
                                    bsonType: 'string'
                                },
                                result: {
                                    bsonType: 'object',
                                    properties: result
                                },
                                statistics: {
                                    bsonType: 'object',
                                    properties: statistics
                                },
                                timeline: {
                                    bsonType: 'array',
                                    items: timelineItem
                                }
                            }
                        }
                    }
                }
            },
            _class: {
                bsonType: 'string'
            }
        }
    }
}

// Create the matches collection and add validator.
result = db.createCollection("matches", {
    validator: matchesSchemaValidator
})

print(result)