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
            player1: {
                bsonType: 'objectId'
            },
            player2: {
                bsonType: [
                    'objectId',
                    'null'
                ]
            },
            startDate: {
                bsonType: 'date'
            },
            endDate: {
                bsonType: 'date'
            },
            gameType: {
                'enum': [
                    '101',
                    '201',
                    '301',
                    '401',
                    '501'
                ]
            },
            gameStatus: {
                'enum': [
                    'in-play',
                    'concluded'
                ]
            },
            throwFirst: {
                'enum': [
                    'player1',
                    'player2'
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
                    }
                }
            },
            result: {
                properties: {
                    winner: {
                        'enum': [
                            'player1',
                            'player2'
                        ]
                    },
                    player1: {
                        bsonType: 'number'
                    },
                    player2: {
                        bsonType: 'number'
                    }
                }
            },
            statistics: {
                bsonType: 'object',
                properties: {
                    player1: {
                        properties: {
                            average: {
                                bsonType: 'number'
                            },
                            tonPlus: {
                                bsonType: 'number'
                            },
                            tonFourty: {
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
                    },
                    player2: {
                        properties: {
                            average: {
                                bsonType: 'number'
                            },
                            tonPlus: {
                                bsonType: 'number'
                            },
                            tonFourty: {
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
                    }
                }
            },
            timeline: {
                bsonType: 'array',
                properties: {
                    set: {
                        bsonType: 'number'
                    },
                    leg: {
                        bsonType: 'number'
                    },
                    winner: {
                        'enum': [
                            'player1',
                            'player2'
                        ]
                    },
                    doublesMissed: {
                        bsonType: 'object',
                        properties: {
                            player1: {
                                bsonType: 'number'
                            },
                            player2: {
                                bsonType: 'number'
                            }
                        }
                    },
                    matchDart: {
                        bsonType: 'number'
                    },
                    scoring: {
                        bsonType: 'array',
                        items: {
                            properties: {
                                player1: {
                                    bsonType: 'number'
                                },
                                player2: {
                                    bsonType: 'number'
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
db.createCollection("matches", {
    validator: matchesSchemaValidator
})