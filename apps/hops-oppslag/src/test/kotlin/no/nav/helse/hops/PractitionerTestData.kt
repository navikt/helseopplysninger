package no.nav.helse.hops

object PractitionerTestData {
    val bundleWithSingleEntity = """
        {
          "resourceType": "Bundle",
          "id": "93aa1b90-007b-4cb7-8157-b9d63cf45a9f",
          "meta": {
            "lastUpdated": "2021-02-09T12:02:19.7147423+00:00"
          },
          "type": "searchset",
          "total": 1,
          "entry": [
            {
              "fullUrl": "https://hint-api-sit.utvikling.local/kontaktregister/ekstern/Practitioner/be54c000-e7e4-4f1b-9be8-2f5b13d91fb5",
              "resource": {
                "resourceType": "Practitioner",
                "id": "be54c000-e7e4-4f1b-9be8-2f5b13d91fb5",
                "meta": {
                  "extension": [
                    {
                      "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-CorrelationId",
                      "valueString": "8da4d301-7f8c-4e7d-b839-ef887d304d44"
                    }
                  ],
                  "versionId": "21",
                  "lastUpdated": "2021-01-27T11:03:46.24+00:00",
                  "profile": [
                    "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-Practitioner-v1"
                  ],
                  "security": [
                    {
                      "system": "http://terminology.hl7.org/CodeSystem/v3-Confidentiality",
                      "code": "N",
                      "display": "normal"
                    },
                    {
                      "system": "https://ehelse.no/fhir/CodeSystem/gd-address-confidentiality-v05",
                      "version": "deprecated",
                      "code": "Ugradert"
                    }
                  ]
                },
                "extension": [
                  {
                    "extension": [
                      {
                        "url": "fregPersonStatus",
                        "valueCode": "bosatt"
                      }
                    ],
                    "url": "http://ehelse.no/fhir/StructureDefinition/gd-person-status"
                  },
                  {
                    "extension": [
                      {
                        "url": "date",
                        "valueDateTime": "1975-11-02T23:00:00+00:00"
                      },
                      {
                        "url": "status",
                        "valueCodeableConcept": {
                          "coding": [
                            {
                              "system": "http://helsedirektoratet.no/fhir/CodeSystem/nhn-preg-person-status",
                              "code": "1",
                              "display": "Bosatt"
                            }
                          ],
                          "text": "Bosatt"
                        }
                      }
                    ],
                    "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-PersonStatus"
                  },
                  {
                    "extension": [
                      {
                        "url": "code",
                        "valueCodeableConcept": {
                          "coding": [
                            {
                              "system": "urn:iso:std:iso:3166",
                              "code": "NO",
                              "display": "Norge"
                            }
                          ],
                          "text": "Norge"
                        }
                      }
                    ],
                    "url": "http://hl7.no/fhir/StructureDefinition/no-basis-person-citizenship"
                  }
                ],
                "identifier": [
                  {
                    "extension": [
                      {
                        "url": "http://ehelse.no/fhir/StructureDefinition/gd-person-identifier-status",
                        "valueCode": "iBruk"
                      }
                    ],
                    "use": "official",
                    "system": "urn:oid:2.16.578.1.12.4.1.4.1",
                    "value": "09047608543"
                  },
                  {
                    "use": "official",
                    "system": "urn:oid:2.16.578.1.12.4.1.4.4",
                    "value": "9111492"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:helseaktorportalen"
                      }
                    ],
                    "system": "portalpilot",
                    "value": "MINE-OPPGJOR_MANUELL-REGISTRERING,PRAKSIS_VELGER"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "hdir:sar:id",
                    "value": "1001754195"
                  }
                ],
                "active": true,
                "name": [
                  {
                    "use": "official",
                    "text": "Mats Johannes Våge",
                    "family": "Våge",
                    "given": [
                      "Mats Johannes"
                    ]
                  },
                  {
                    "use": "usual",
                    "given": [
                      "Mats",
                      "Johannes"
                    ]
                  }
                ],
                "telecom": [
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:helseaktorportal/mine-opplysninger"
                      }
                    ],
                    "system": "email",
                    "value": "foo@bar.com",
                    "use": "work",
                    "rank": 1
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:helseaktorportal/mine-opplysninger"
                      }
                    ],
                    "system": "phone",
                    "value": "+47 12345678",
                    "use": "work",
                    "rank": 1
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:kontaktvedlikehold"
                      }
                    ],
                    "system": "email",
                    "value": "someone1@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:kontaktvedlikehold"
                      }
                    ],
                    "system": "email",
                    "value": "someone2@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:kontaktvedlikehold"
                      }
                    ],
                    "system": "email",
                    "value": "someone3@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:kontaktvedlikehold"
                      }
                    ],
                    "system": "phone",
                    "value": "+4798765432",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "other",
                    "value": "4712345678;",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "other",
                    "value": "4798765432;",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "foo@bar.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "someone1@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "someone2@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "someone3@somewhere.com",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "ellen.tester.hastighet@online.no",
                    "use": "work"
                  },
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-OwnerName",
                        "valueString": "hdir:sar-import"
                      }
                    ],
                    "system": "email",
                    "value": "Våge.Mats.Johannes@online.no",
                    "use": "work"
                  }
                ],
                "gender": "male",
                "birthDate": "1976-04-09",
                "qualification": [
                  {
                    "extension": [
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-approvalType",
                        "valueCodeableConcept": {
                          "coding": [
                            {
                              "system": "urn:oid:2.16.578.1.12.4.1.1.7704",
                              "code": "1",
                              "display": "Autorisasjon"
                            },
                            {
                              "system": "7704",
                              "version": "deprecated",
                              "code": "1",
                              "display": "Autorisasjon"
                            }
                          ],
                          "text": "Autorisasjon"
                        }
                      },
                      {
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-godkjentTurnus",
                        "valueDate": "2007-09-21"
                      },
                      {
                        "extension": [
                          {
                            "url": "code",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "urn:oid:2.16.578.1.12.4.1.1.7701",
                                  "code": "1",
                                  "display": "Full rekvisisjonsrett"
                                },
                                {
                                  "system": "7701",
                                  "version": "deprecated",
                                  "code": "1",
                                  "display": "Full rekvisisjonsrett"
                                }
                              ],
                              "text": "Full rekvisisjonsrett"
                            }
                          },
                          {
                            "url": "period",
                            "valuePeriod": {
                              "start": "2007-09-20T22:00:00+00:00",
                              "end": "2056-04-08T22:00:00+00:00"
                            }
                          }
                        ],
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-prescriptionCapability"
                      },
                      {
                        "extension": [
                          {
                            "url": "code",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "urn:oid:2.16.578.1.12.4.1.1.7702",
                                  "code": "3",
                                  "display": "Allmenlege med trygderefusjon"
                                },
                                {
                                  "system": "7702",
                                  "version": "deprecated",
                                  "code": "3",
                                  "display": "Allmenlege med trygderefusjon"
                                }
                              ],
                              "text": "Allmenlege med trygderefusjon"
                            }
                          },
                          {
                            "url": "period",
                            "valuePeriod": {
                              "start": "2009-12-13T23:00:00+00:00",
                              "end": "2046-05-08T22:00:00+00:00"
                            }
                          }
                        ],
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-additionalRegistration"
                      },
                      {
                        "extension": [
                          {
                            "url": "code",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "urn:oid:2.16.578.1.12.4.1.1.7702",
                                  "code": "6",
                                  "display": "Egenerklært kurs i trygdemedisin (del av grunnkurs B)"
                                },
                                {
                                  "system": "7702",
                                  "version": "deprecated",
                                  "code": "6",
                                  "display": "Egenerklært kurs i trygdemedisin (del av grunnkurs B)"
                                }
                              ],
                              "text": "Egenerklært kurs i trygdemedisin (del av grunnkurs B)"
                            }
                          },
                          {
                            "url": "period",
                            "valuePeriod": {
                              "start": "2013-04-27T22:00:00+00:00",
                              "end": "2056-04-08T22:00:00+00:00"
                            }
                          }
                        ],
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-additionalRegistration"
                      },
                      {
                        "extension": [
                          {
                            "url": "type",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "urn:oid:2.16.578.1.12.4.1.1.7703",
                                  "code": "3",
                                  "display": "Universitet"
                                },
                                {
                                  "system": "7703",
                                  "version": "deprecated",
                                  "code": "3",
                                  "display": "Universitet"
                                }
                              ],
                              "text": "Universitet"
                            }
                          },
                          {
                            "url": "date",
                            "valueDate": "2005-06-17"
                          },
                          {
                            "url": "institution",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "hdir:nhn:hpr:utdanningsinstitusjon",
                                  "code": "974757486",
                                  "display": "Universitetet i Tromsø"
                                }
                              ],
                              "text": "Universitetet i Tromsø"
                            }
                          },
                          {
                            "url": "country",
                            "valueCodeableConcept": {
                              "coding": [
                                {
                                  "system": "urn:iso:std:iso:3166",
                                  "code": "NO",
                                  "display": "Norge"
                                }
                              ],
                              "text": "Norge"
                            }
                          }
                        ],
                        "url": "http://helsedirektoratet.no/fhir/StructureDefinition/hdir-Diploma"
                      }
                    ],
                    "code": {
                      "coding": [
                        {
                          "system": "urn:oid:2.16.578.1.12.4.1.1.9060",
                          "code": "LE",
                          "display": "Lege"
                        },
                        {
                          "system": "9060",
                          "version": "deprecated",
                          "code": "LE",
                          "display": "Lege"
                        }
                      ],
                      "text": "Lege"
                    },
                    "period": {
                      "start": "2007-09-20T22:00:00+00:00",
                      "end": "2056-04-08T22:00:00+00:00"
                    },
                    "issuer": {
                      "reference": "Organization/e62f7f42-a3d3-4e7f-9e0e-ec1df11d7528",
                      "type": "Organization",
                      "identifier": {
                        "system": "urn:oid:2.16.578.1.12.4.1.4.101",
                        "value": "983544622"
                      },
                      "display": "Helsedirektoratet"
                    }
                  }
                ]
              },
              "search": {
                "mode": "match"
              }
            }
          ]
        }
    """.trimIndent()
}
