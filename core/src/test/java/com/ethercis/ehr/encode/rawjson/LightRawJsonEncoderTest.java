/*
 * Copyright (c) 2015 Christian Chevalley
 * This file is part of Project Ethercis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ethercis.ehr.encode.rawjson;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by christian on 6/21/2017.
 */
public class LightRawJsonEncoderTest {

    @Test
    public void testLightEncoding() {
        String jsonbOrigin =
                "[\n" +
                        "  {\n" +
                        "    \"/items[at0002]\": [\n" +
                        "      {\n" +
                        "        \"/items[at0001]\": [\n" +
                        "          {\n" +
                        "            \"/name\": \"Urea\",\n" +
                        "            \"/value\": {\n" +
                        "              \"units\": \"mmol/l\",\n" +
                        "              \"accuracy\": 0.0,\n" +
                        "              \"magnitude\": 6.7,\n" +
                        "              \"precision\": 0,\n" +
                        "              \"normalRange\": {\n" +
                        "                \"interval\": {\n" +
                        "                  \"lower\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 2.5,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"upper\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 6.6,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"lowerIncluded\": true,\n" +
                        "                  \"upperIncluded\": true\n" +
                        "                }\n" +
                        "              },\n" +
                        "              \"accuracyPercent\": false\n" +
                        "            },\n" +
                        "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result']/items[at0001 and name/value='Urea']\",\n" +
                        "            \"/$CLASS$\": \"DvQuantity\"\n" +
                        "          }\n" +
                        "        ],\n" +
                        "        \"/items[at0003]\": [\n" +
                        "          {\n" +
                        "            \"/name\": \"Comment\",\n" +
                        "            \"/value\": {\n" +
                        "              \"value\": \"may be technical artefact\"\n" +
                        "            },\n" +
                        "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result']/items[at0003 and name/value='Comment']\",\n" +
                        "            \"/$CLASS$\": \"DvText\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"/items[at0001]\": [\n" +
                        "          {\n" +
                        "            \"/name\": \"365760004\",\n" +
                        "            \"/value\": {\n" +
                        "              \"units\": \"mmol/l\",\n" +
                        "              \"accuracy\": 0.0,\n" +
                        "              \"magnitude\": 5.8,\n" +
                        "              \"precision\": 0,\n" +
                        "              \"normalRange\": {\n" +
                        "                \"interval\": {\n" +
                        "                  \"lower\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 3.5,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"upper\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 5.3,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"lowerIncluded\": true,\n" +
                        "                  \"upperIncluded\": true\n" +
                        "                }\n" +
                        "              },\n" +
                        "              \"accuracyPercent\": false\n" +
                        "            },\n" +
                        "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #4']/items[at0001 and name/value='365760004']\",\n" +
                        "            \"/$CLASS$\": \"DvQuantity\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"/items[at0001]\": [\n" +
                        "          {\n" +
                        "            \"/name\": \"Sodium\",\n" +
                        "            \"/value\": {\n" +
                        "              \"units\": \"mmol/l\",\n" +
                        "              \"accuracy\": 0.0,\n" +
                        "              \"magnitude\": 177.0,\n" +
                        "              \"precision\": 0,\n" +
                        "              \"normalRange\": {\n" +
                        "                \"interval\": {\n" +
                        "                  \"lower\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 133.0,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"upper\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 146.0,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"lowerIncluded\": true,\n" +
                        "                  \"upperIncluded\": true\n" +
                        "                }\n" +
                        "              },\n" +
                        "              \"accuracyPercent\": false\n" +
                        "            },\n" +
                        "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #3']/items[at0001 and name/value='Sodium']\",\n" +
                        "            \"/$CLASS$\": \"DvQuantity\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"/items[at0001]\": [\n" +
                        "          {\n" +
                        "            \"/name\": \"Creatinine\",\n" +
                        "            \"/value\": {\n" +
                        "              \"units\": \"mmol/l\",\n" +
                        "              \"accuracy\": 0.0,\n" +
                        "              \"magnitude\": 115.0,\n" +
                        "              \"precision\": 0,\n" +
                        "              \"normalRange\": {\n" +
                        "                \"interval\": {\n" +
                        "                  \"lower\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 80.0,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"upper\": {\n" +
                        "                    \"units\": \"mmol/l\",\n" +
                        "                    \"accuracy\": 0.0,\n" +
                        "                    \"magnitude\": 110.0,\n" +
                        "                    \"precision\": 0,\n" +
                        "                    \"accuracyPercent\": false\n" +
                        "                  },\n" +
                        "                  \"lowerIncluded\": true,\n" +
                        "                  \"upperIncluded\": true\n" +
                        "                }\n" +
                        "              },\n" +
                        "              \"accuracyPercent\": false\n" +
                        "            },\n" +
                        "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #2']/items[at0001 and name/value='Creatinine']\",\n" +
                        "            \"/$CLASS$\": \"DvQuantity\"\n" +
                        "          }\n" +
                        "        ]\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }\n" +
                        "]";

        String translated = new LightRawJsonEncoder(jsonbOrigin).encodeContentAsMap("test");
        assertNotNull(translated);
        System.out.println(translated);
    }

        @Test
        public void testLightEncoding2() {
                String jsonbOrigin =
                                       "  {\n" +
                                       "    \"/name\": [\n" +
                                       "      {\n" +
                                       "        \"value\": \"Laboratory test panel\"\n" +
                                       "      }\n" +
                                       "    ],\n" +
                                       "    \"/items[at0002]\": [\n" +
                                       "      {\n" +
                                       "        \"/name\": [\n" +
                                       "          {\n" +
                                       "            \"value\": \"Laboratory result\"\n" +
                                       "          }\n" +
                                       "        ],\n" +
                                       "        \"/items[at0001]\": [\n" +
                                       "          {\n" +
                                       "            \"/name\": [\n" +
                                       "              {\n" +
                                       "                \"value\": \"white blood cell count\"\n" +
                                       "              }\n" +
                                       "            ],\n" +
                                       "            \"/value\": {\n" +
                                       "              \"units\": \"10*9/l\",\n" +
                                       "              \"accuracy\": 0.0,\n" +
                                       "              \"magnitude\": 4.7,\n" +
                                       "              \"precision\": 0,\n" +
                                       "              \"normalRange\": {\n" +
                                       "                \"interval\": {\n" +
                                       "                  \"lower\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 3.6,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"upper\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 11.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"lowerIncluded\": true,\n" +
                                       "                  \"upperIncluded\": true\n" +
                                       "                }\n" +
                                       "              },\n" +
                                       "              \"accuracyPercent\": false\n" +
                                       "            },\n" +
                                       "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result']/items[at0001 and name/value='white blood cell count']\",\n" +
                                       "            \"/$CLASS$\": \"DvQuantity\"\n" +
                                       "          }\n" +
                                       "        ]\n" +
                                       "      },\n" +
                                       "      {\n" +
                                       "        \"/name\": [\n" +
                                       "          {\n" +
                                       "            \"value\": \"Laboratory result #2\"\n" +
                                       "          }\n" +
                                       "        ],\n" +
                                       "        \"/items[at0001]\": [\n" +
                                       "          {\n" +
                                       "            \"/name\": [\n" +
                                       "              {\n" +
                                       "                \"value\": \"neutrophil count\"\n" +
                                       "              }\n" +
                                       "            ],\n" +
                                       "            \"/value\": {\n" +
                                       "              \"units\": \"10*9/l\",\n" +
                                       "              \"accuracy\": 0.0,\n" +
                                       "              \"magnitude\": 4.9,\n" +
                                       "              \"precision\": 0,\n" +
                                       "              \"normalRange\": {\n" +
                                       "                \"interval\": {\n" +
                                       "                  \"lower\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 1.8,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"upper\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 7.5,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"lowerIncluded\": true,\n" +
                                       "                  \"upperIncluded\": true\n" +
                                       "                }\n" +
                                       "              },\n" +
                                       "              \"accuracyPercent\": false\n" +
                                       "            },\n" +
                                       "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #2']/items[at0001 and name/value='neutrophil count']\",\n" +
                                       "            \"/$CLASS$\": \"DvQuantity\"\n" +
                                       "          }\n" +
                                       "        ]\n" +
                                       "      },\n" +
                                       "      {\n" +
                                       "        \"/name\": [\n" +
                                       "          {\n" +
                                       "            \"value\": \"Laboratory result #3\"\n" +
                                       "          }\n" +
                                       "        ],\n" +
                                       "        \"/items[at0001]\": [\n" +
                                       "          {\n" +
                                       "            \"/name\": [\n" +
                                       "              {\n" +
                                       "                \"value\": \"lymphocyte count\"\n" +
                                       "              }\n" +
                                       "            ],\n" +
                                       "            \"/value\": {\n" +
                                       "              \"units\": \"10*9/l\",\n" +
                                       "              \"accuracy\": 0.0,\n" +
                                       "              \"magnitude\": 3.2,\n" +
                                       "              \"precision\": 0,\n" +
                                       "              \"normalRange\": {\n" +
                                       "                \"interval\": {\n" +
                                       "                  \"lower\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 1.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"upper\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 4.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"lowerIncluded\": true,\n" +
                                       "                  \"upperIncluded\": true\n" +
                                       "                }\n" +
                                       "              },\n" +
                                       "              \"accuracyPercent\": false\n" +
                                       "            },\n" +
                                       "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #3']/items[at0001 and name/value='lymphocyte count']\",\n" +
                                       "            \"/$CLASS$\": \"DvQuantity\"\n" +
                                       "          }\n" +
                                       "        ]\n" +
                                       "      },\n" +
                                       "      {\n" +
                                       "        \"/name\": [\n" +
                                       "          {\n" +
                                       "            \"value\": \"Laboratory result #4\"\n" +
                                       "          }\n" +
                                       "        ],\n" +
                                       "        \"/items[at0001]\": [\n" +
                                       "          {\n" +
                                       "            \"/name\": [\n" +
                                       "              {\n" +
                                       "                \"value\": \"61928009\"\n" +
                                       "              }\n" +
                                       "            ],\n" +
                                       "            \"/value\": {\n" +
                                       "              \"units\": \"10*9/l\",\n" +
                                       "              \"accuracy\": 0.0,\n" +
                                       "              \"magnitude\": 198.0,\n" +
                                       "              \"precision\": 0,\n" +
                                       "              \"normalRange\": {\n" +
                                       "                \"interval\": {\n" +
                                       "                  \"lower\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 140.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"upper\": {\n" +
                                       "                    \"units\": \"10*9/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 400.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"lowerIncluded\": true,\n" +
                                       "                  \"upperIncluded\": true\n" +
                                       "                }\n" +
                                       "              },\n" +
                                       "              \"accuracyPercent\": false\n" +
                                       "            },\n" +
                                       "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #4']/items[at0001 and name/value='61928009']\",\n" +
                                       "            \"/$CLASS$\": \"DvQuantity\"\n" +
                                       "          }\n" +
                                       "        ]\n" +
                                       "      },\n" +
                                       "      {\n" +
                                       "        \"/name\": [\n" +
                                       "          {\n" +
                                       "            \"value\": \"Laboratory result #5\"\n" +
                                       "          }\n" +
                                       "        ],\n" +
                                       "        \"/items[at0001]\": [\n" +
                                       "          {\n" +
                                       "            \"/name\": [\n" +
                                       "              {\n" +
                                       "                \"value\": \"271026005\"\n" +
                                       "              }\n" +
                                       "            ],\n" +
                                       "            \"/value\": {\n" +
                                       "              \"units\": \"g/l\",\n" +
                                       "              \"accuracy\": 0.0,\n" +
                                       "              \"magnitude\": 153.0,\n" +
                                       "              \"precision\": 0,\n" +
                                       "              \"normalRange\": {\n" +
                                       "                \"interval\": {\n" +
                                       "                  \"lower\": {\n" +
                                       "                    \"units\": \"g/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 130.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"upper\": {\n" +
                                       "                    \"units\": \"g/l\",\n" +
                                       "                    \"accuracy\": 0.0,\n" +
                                       "                    \"magnitude\": 180.0,\n" +
                                       "                    \"precision\": 0,\n" +
                                       "                    \"accuracyPercent\": false\n" +
                                       "                  },\n" +
                                       "                  \"lowerIncluded\": true,\n" +
                                       "                  \"upperIncluded\": true\n" +
                                       "                }\n" +
                                       "              },\n" +
                                       "              \"accuracyPercent\": false\n" +
                                       "            },\n" +
                                       "            \"/$PATH$\": \"/content[openEHR-EHR-OBSERVATION.laboratory_test.v0 and name/value='Laboratory test']/data[at0001]/events[at0002 and name/value='Any event']/data[at0003]/items[openEHR-EHR-CLUSTER.laboratory_test_panel.v0 and name/value='Laboratory test panel']/items[at0002 and name/value='Laboratory result #5']/items[at0001 and name/value='271026005']\",\n" +
                                       "            \"/$CLASS$\": \"DvQuantity\"\n" +
                                       "          }\n" +
                                       "        ]\n" +
                                       "      }\n" +
                                       "    ]\n" +
                                       "  }\n";
                String translated = new LightRawJsonEncoder(jsonbOrigin).encodeContentAsMap("test");
                assertNotNull(translated);
                System.out.println(translated);
        }

}