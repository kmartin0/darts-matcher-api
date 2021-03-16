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
                bsonType: 'object',
                registered: {
                    bsonType: 'array',
                    items: {
                        bsonType: 'objectId'
                    }
                },
                anonymous: {
                    bsonType: 'array',
                    items: {
                        bsonType: 'string'
                    }
                },
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
                bsonType: 'objectId|string'
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
            result: {
                bsonType: 'array',
                items: {
                    properties: {
                        player: {
                            bsonType: 'objectId|string'
                        },
                        score: {
                            bsonType: 'number'
                        },
                        result: {
                            'enum': [
                                'WIN',
                                'LOSE',
                                'DRAW'
                            ]
                        }
                    }
                }
            },
            statistics: {
                bsonType: 'array',
                items: {
                    properties: {
                        player: {
                            bsonType: 'objectId|string'
                        },
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
                }
            },
            timeline: {
                bsonType: 'array',
                items: {
                    properties: {
                        set: {
                            bsonType: 'number'
                        },
                        winner: {
                            bsonType: 'objectId|string'
                        },
                        legs: {
                            bsonType: 'array',
                            items: {
                                properties: {
                                    leg: {
                                        bsonType: 'number'
                                    },
                                    winner: {
                                        bsonType: 'objectId|string'
                                    },
                                    dartsUsedFinalThrow: {
                                        bsonType: 'number'
                                    },
                                    players: {
                                        bsonType: 'array',
                                        items: {
                                            properties: {
                                                player: {
                                                    bsonType: 'objectId|string'
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
db.createCollection("matches", {
    validator: matchesSchemaValidator
})