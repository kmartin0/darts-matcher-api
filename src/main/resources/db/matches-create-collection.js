// Database connection
conn = new Mongo()
db = conn.getDB("darts-matcher")

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
                    'X01'
                ]
            },
            x01: {
                bsonType: 'number'
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
                bsonType: 'array',
                items: {
                    bsonType: 'object',
                    properties: {
                        playerId: {
                            bsonType: ['objectId', 'string']
                        },
                        playerType: {
                            enum: [
                                'REGISTERED',
                                'ANONYMOUS'
                            ]
                        },
                        result: {
                            'enum': [
                                "WIN",
                                "DRAW",
                                "LOSE"
                            ]
                        },
                        statistics: {
                            bsonType: 'object',
                            properties: {
                                averageStats: {
                                    bsonType: 'object',
                                    properties: {
                                        dartsThrown: {
                                            bsonType: 'number'
                                        },
                                        pointsThrown: {
                                            bsonType: 'number'
                                        },
                                        average: {
                                            bsonType: 'number'
                                        },
                                        dartsThrownFirstNine: {
                                            bsonType: 'number'
                                        },
                                        pointsThrownFirstNine: {
                                            bsonType: 'number'
                                        },
                                        averageFirstNine: {
                                            bsonType: 'number'
                                        }
                                    }
                                },
                                scoreStats: {
                                    bsonType: 'object',
                                    properties: {
                                        tonPlus: {
                                            bsonType: 'number'
                                        },
                                        tonForty: {
                                            bsonType: 'number'
                                        },
                                        tonEighty: {
                                            bsonType: 'number'
                                        }
                                    }
                                },
                                checkoutStats: {
                                    bsonType: 'object',
                                    properties: {
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
                                }
                            }
                        },
                        timeline: {
                            bsonType: 'array',
                            items: {
                                properties: {
                                    set: {
                                        bsonType: 'number'
                                    },
                                    result: {
                                        'enum': [
                                            "WIN",
                                            "DRAW",
                                            "LOSE"
                                        ]
                                    },
                                    legs: {
                                        bsonType: 'array',
                                        items: {
                                            properties: {
                                                leg: {
                                                    bsonType: 'number'
                                                },
                                                result: {
                                                    'enum': [
                                                        "WIN",
                                                        "DRAW",
                                                        "LOSE"
                                                    ]
                                                },
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