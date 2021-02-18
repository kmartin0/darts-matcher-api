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
            players: {
                bsonType: 'array',
                items: {
                    bsonType: 'objectId'
                }
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
                bsonType: 'objectId'
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
                bsonType: 'object',
                properties: {
                    winner: {
                        bsonType: 'objectId'
                    },
                    players: {
                        bsonType: 'array',
                        items: {
                            properties: {
                                player: {
                                    bsonType: 'objectId'
                                },
                                score: {
                                    bsonType: 'number'
                                }
                            }
                        }
                    },
                }
            },
            statistics: {
                bsonType: 'array',
                items: {
                    properties: {
                        player: {
                            bsonType: 'objectId'
                        },
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
            },
            timeline: {
                bsonType: 'array',
                items: {
                    properties: {
                        set: {
                            bsonType: 'number'
                        },
                        leg: {
                            bsonType: 'number'
                        },
                        winner: {
                            bsonType: 'objectId'
                        },
                        dartsUsedFinalThrow: {
                            bsonType: 'number'
                        },
                        players: {
                            bsonType: 'array',
                            items: {
                                properties: {
                                    player: {
                                        bsonType: 'objectId'
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