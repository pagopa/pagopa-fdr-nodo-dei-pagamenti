Feature: Syntax checks for nodoInviaFlussoRendicontazione - KO

    Background:
        Given systems up

    Scenario: Generazione rendicontazione
        Given REND generation
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>#timedate#</pay_i:dataOraFlusso>
            <pay_i:identificativoUnivocoRegolamento>#iuv#</pay_i:identificativoUnivocoRegolamento>
            <pay_i:dataRegolamento>#date#</pay_i:dataRegolamento>
            <pay_i:istitutoMittente>
            <pay_i:identificativoUnivocoMittente>
            <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
            <pay_i:codiceIdentificativoUnivoco>#psp#</pay_i:codiceIdentificativoUnivoco>
            </pay_i:identificativoUnivocoMittente>
            <pay_i:denominazioneMittente>denMitt_1</pay_i:denominazioneMittente>
            </pay_i:istitutoMittente>
            <pay_i:codiceBicBancaDiRiversamento>BICIDPSP</pay_i:codiceBicBancaDiRiversamento>
            <pay_i:istitutoRicevente>
            <pay_i:identificativoUnivocoRicevente>
            <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
            <pay_i:codiceIdentificativoUnivoco>codIdUniv_2</pay_i:codiceIdentificativoUnivoco>
            </pay_i:identificativoUnivocoRicevente>
            <pay_i:denominazioneRicevente>denRic_2</pay_i:denominazioneRicevente>
            </pay_i:istitutoRicevente>
            <pay_i:numeroTotalePagamenti>1</pay_i:numeroTotalePagamenti>
            <pay_i:importoTotalePagamenti>10.00</pay_i:importoTotalePagamenti>
            <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>#iuv#</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>#iuv#</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
            </pay_i:datiSingoliPagamenti>
            </pay_i:FlussoRiversamento>
            """

    @runnable
    # [SIN_NIFR_01]
    Scenario: Check PPT_SINTASSI_EXTRAXSD error for nodoInviaFlussoRendicontazione primitive - TEST1
        Given the Generazione rendicontazione scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/ciao/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#canale#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoInviaFlussoRendicontazione response

    @runnable
    # [SIN_NIFR_04]
    Scenario: Check PPT_SINTASSI_EXTRAXSD error for nodoInviaFlussoRendicontazione primitive - TEST2
        Given the Generazione rendicontazione scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#canale#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazio>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoInviaFlussoRendicontazione response

    @runnable
    Scenario Outline: Check PPT_SINTASSI_EXTRAXSD error for nodoInviaFlussoRendicontazione primitive - TEST3
        Given the Generazione rendicontazione scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#canale#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And <tag> with <tag_value> in nodoInviaFlussoRendicontazione
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoInviaFlussoRendicontazione response
        Examples:
            | tag                               | tag_value                            | soapUI test |
            | soapenv:Body                      | None                                 | SIN_NIFR_08 |
            | soapenv:Body                      | Empty                                | SIN_NIFR_09 |
            | ws:nodoInviaFlussoRendicontazione | None                                 | SIN_NIFR_10 |
            | ws:nodoInviaFlussoRendicontazione | Empty                                | SIN_NIFR_11 |
            | identificativoPSP                 | None                                 | SIN_NIFR_12 |
            | identificativoPSP                 | Empty                                | SIN_NIFR_13 |
            | identificativoPSP                 | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | SIN_NIFR_14 |
            | identificativoIntermediarioPSP    | None                                 | SIN_NIFR_15 |
            | identificativoIntermediarioPSP    | Empty                                | SIN_NIFR_16 |
            | identificativoIntermediarioPSP    | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | SIN_NIFR_17 |
            | identificativoCanale              | None                                 | SIN_NIFR_18 |
            | identificativoCanale              | Empty                                | SIN_NIFR_19 |
            | identificativoCanale              | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | SIN_NIFR_20 |
            | password                          | None                                 | SIN_NIFR_21 |
            | password                          | Empty                                | SIN_NIFR_22 |
            | password                          | aaaaaaa                              | SIN_NIFR_23 |
            | password                          | aaaaaaaaaaaaaaaa                     | SIN_NIFR_24 |
            | identificativoDominio             | None                                 | SIN_NIFR_25 |
            | identificativoDominio             | Empty                                | SIN_NIFR_25 |
            | identificativoDominio             | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | SIN_NIFR_27 |
            | identificativoFlusso              | None                                 | SIN_NIFR_28 |
            | dataOraFlusso                     | Empty                                | SIN_NIFR_29 |
            | dataOraFlusso                     | 2022-24-01T11:02:53.692              | SIN_NIFR_30 |
            | dataOraFlusso                     | 22-01-24T11:02:53.692                | SIN_NIFR_30 |
            | dataOraFlusso                     | 2022-01-24T11:02                     | SIN_NIFR_30 |
            | xmlRendicontazione                | None                                 | SIN_NIFR_31 |
#

    @runnable
    Scenario Outline: Check PPT_SINTASSI_EXTRAXSD error for nodoInviaFlussoRendicontazione primitive - TEST4
        Given the Generazione rendicontazione scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#canale#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And <tag> with <tag_value> in nodoInviaFlussoRendicontazione
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoInviaFlussoRendicontazione response
        Examples:
            | tag                | tag_value                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | soapUI test |
            | xmlRendicontazione | 123456789                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | SIN_NIFR_33 |
            | xmlRendicontazione | 3C7@61795F693A4%$?66C7573736F526976657273616D656E746F20786D6C6E733A7061795F693D22687474703A2F2F7777772E646967697470612E676F762E69742F736368656D61732F323031312F506167616D656E74692F2220786D6C6E733A7873693D22687474703A2F2F7777772E77332E6F72672F323030312F584D4C536368656D612D696E7374616E636522207873693A736368656D614C6F636174696F6E3D22687474703A2F2F7777772E646967697470612E676F762E69742F736368656D61732F323031312F506167616D656E74692F20466C7573736F52656E6469636F6E74617A696F6E655F765F315F305F312E78736420223E0D0A2020202020202020202020203C7061795F693A76657273696F6E654F67676574746F3E312E303C2F7061795F693A76657273696F6E654F67676574746F3E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F466C7573736F3E236964656E7469666963617469766F466C7573736F233C2F7061795F693A6964656E7469666963617469766F466C7573736F3E0D0A2020202020202020202020203C7061795F693A646174614F7261466C7573736F3E2374696D6564617465233C2F7061795F693A646174614F7261466C7573736F3E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F556E69766F636F5265676F6C616D656E746F3E23697576233C2F7061795F693A6964656E7469666963617469766F556E69766F636F5265676F6C616D656E746F3E0D0A2020202020202020202020203C7061795F693A646174615265676F6C616D656E746F3E2364617465233C2F7061795F693A646174615265676F6C616D656E746F3E0D0A2020202020202020202020203C7061795F693A697374697475746F4D697474656E74653E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F556E69766F636F4D697474656E74653E0D0A2020202020202020202020203C7061795F693A7469706F4964656E7469666963617469766F556E69766F636F3E473C2F7061795F693A7469706F4964656E7469666963617469766F556E69766F636F3E0D0A2020202020202020202020203C7061795F693A636F646963654964656E7469666963617469766F556E69766F636F3E23707370233C2F7061795F693A636F646963654964656E7469666963617469766F556E69766F636F3E0D0A2020202020202020202020203C2F7061795F693A6964656E7469666963617469766F556E69766F636F4D697474656E74653E0D0A2020202020202020202020203C7061795F693A64656E6F6D696E617A696F6E654D697474656E74653E64656E4D6974745F313C2F7061795F693A64656E6F6D696E617A696F6E654D697474656E74653E0D0A2020202020202020202020203C2F7061795F693A697374697475746F4D697474656E74653E0D0A2020202020202020202020203C7061795F693A636F6469636542696342616E63614469526976657273616D656E746F3E42494349445053503C2F7061795F693A636F6469636542696342616E63614469526976657273616D656E746F3E0D0A2020202020202020202020203C7061795F693A697374697475746F5269636576656E74653E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F556E69766F636F5269636576656E74653E0D0A2020202020202020202020203C7061795F693A7469706F4964656E7469666963617469766F556E69766F636F3E473C2F7061795F693A7469706F4964656E7469666963617469766F556E69766F636F3E0D0A2020202020202020202020203C7061795F693A636F646963654964656E7469666963617469766F556E69766F636F3E636F644964556E69765F323C2F7061795F693A636F646963654964656E7469666963617469766F556E69766F636F3E0D0A2020202020202020202020203C2F7061795F693A6964656E7469666963617469766F556E69766F636F5269636576656E74653E0D0A2020202020202020202020203C7061795F693A64656E6F6D696E617A696F6E655269636576656E74653E64656E5269635F323C2F7061795F693A64656E6F6D696E617A696F6E655269636576656E74653E0D0A2020202020202020202020203C2F7061795F693A697374697475746F5269636576656E74653E0D0A2020202020202020202020203C7061795F693A6E756D65726F546F74616C65506167616D656E74693E313C2F7061795F693A6E756D65726F546F74616C65506167616D656E74693E0D0A2020202020202020202020203C7061795F693A696D706F72746F546F74616C65506167616D656E74693E31302E30303C2F7061795F693A696D706F72746F546F74616C65506167616D656E74693E0D0A2020202020202020202020203C7061795F693A6461746953696E676F6C69506167616D656E74693E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F556E69766F636F56657273616D656E746F3E23697576233C2F7061795F693A6964656E7469666963617469766F556E69766F636F56657273616D656E746F3E0D0A2020202020202020202020203C7061795F693A6964656E7469666963617469766F556E69766F636F526973636F7373696F6E653E23697576233C2F7061795F693A6964656E7469666963617469766F556E69766F636F526973636F7373696F6E653E0D0A2020202020202020202020203C7061795F693A696E646963654461746953696E676F6C6F506167616D656E746F3E313C2F7061795F693A696E646963654461746953696E676F6C6F506167616D656E746F3E0D0A2020202020202020202020203C7061795F693A73696E676F6C6F496D706F72746F50616761746F3E31302E30303C2F7061795F693A73696E676F6C6F496D706F72746F50616761746F3E0D0A2020202020202020202020203C7061795F693A636F64696365457369746F53696E676F6C6F506167616D656E746F3E303C2F7061795F693A636F64696365457369746F53696E676F6C6F506167616D656E746F3E0D0A2020202020202020202020203C7061795F693A64617461457369746F53696E676F6C6F506167616D656E746F3E2364617465233C2F7061795F693A64617461457369746F53696E676F6C6F506167616D656E746F3E0D0A2020202020202020202020203C2F7061795F693A6461746953696E676F6C69506167616D656E74693E0D0A2020202020202020202020203C2F7061795F693A466C7573736F526976657616D656E746F3E | SIN_NIFR_34 |


    @runnable
    Scenario Outline: Check PPT_SINTASSI_XSD error for nodoInviaFlussoRendicontazione primitive - TEST5
        Given initial xml REND
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>#timedate#</pay_i:dataOraFlusso>
            <pay_i:identificativoUnivocoRegolamento>#iuv#</pay_i:identificativoUnivocoRegolamento>
            <pay_i:dataRegolamento>#date#</pay_i:dataRegolamento>
            <pay_i:istitutoMittente>
            <pay_i:identificativoUnivocoMittente>
            <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
            <pay_i:codiceIdentificativoUnivoco>#psp#</pay_i:codiceIdentificativoUnivoco>
            </pay_i:identificativoUnivocoMittente>
            <pay_i:denominazioneMittente>denMitt_1</pay_i:denominazioneMittente>
            </pay_i:istitutoMittente>
            <pay_i:codiceBicBancaDiRiversamento>BICIDPSP</pay_i:codiceBicBancaDiRiversamento>
            <pay_i:istitutoRicevente>
            <pay_i:identificativoUnivocoRicevente>
            <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
            <pay_i:codiceIdentificativoUnivoco>codIdUniv_2</pay_i:codiceIdentificativoUnivoco>
            </pay_i:identificativoUnivocoRicevente>
            <pay_i:denominazioneRicevente>denRic_2</pay_i:denominazioneRicevente>
            </pay_i:istitutoRicevente>
            <pay_i:numeroTotalePagamenti>1</pay_i:numeroTotalePagamenti>
            <pay_i:importoTotalePagamenti>10.00</pay_i:importoTotalePagamenti>
            <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>#iuv#</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>#iuv#</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
            </pay_i:datiSingoliPagamenti>
            </pay_i:FlussoRiversamento>
            """
        And <tag> with <tag_value> in REND
        And REND generation
        """
        $REND
        """
        And  initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#canale#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SINTASSI_XSD of nodoInviaFlussoRendicontazione response
        Examples:
            | tag                                    | tag_value                                                               | soapUI test  |
            | pay_i:versioneOggetto                  | 2                                                                       | SIN_NIFR_35  |
            | pay_i:versioneOggetto                  | 1.00                                                                    | SIN_NIFR_35  |
            | pay_i:versioneOggetto                  | None                                                                    | SIN_NIFR_36  |
            | pay_i:versioneOggetto                  | Empty                                                                   | SIN_NIFR_37  |
            | pay_i:identificativoFlusso             | None                                                                    | SIN_NIFR_38  |
            | pay_i:identificativoFlusso             | Empty                                                                   | SIN_NIFR_39  |
            | pay_i:dataOraFlusso                    | None                                                                    | SIN_NIFR_40  |
            | pay_i:dataOraFlusso                    | Empty                                                                   | SIN_NIFR_41  |
            | pay_i:dataOraFlusso                    | $date                                                                   | SIN_NIFR_42  |
            | pay_i:identificativoUnivocoRegolamento | None                                                                    | SIN_NIFR_43  |
            | pay_i:identificativoUnivocoRegolamento | Empty                                                                   | SIN_NIFR_44  |
            | pay_i:identificativoUnivocoRegolamento | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_45  |
            | pay_i:dataRegolamento                  | None                                                                    | SIN_NIFR_46  |
            | pay_i:dataRegolamento                  | Empty                                                                   | SIN_NIFR_47  |
            | pay_i:dataRegolamento                  | $timedate                                                               | SIN_NIFR_48  |
            | pay_i:istitutoMittente                 | None                                                                    | SIN_NIFR_49  |
            | pay_i:istitutoMittente                 | Empty                                                                   | SIN_NIFR_50  |
            | pay_i:istitutoMittente                 | RemoveParent                                                            | SIN_NIFR_51  |
            | pay_i:identificativoUnivocoMittente    | RemoveParent                                                            | SIN_NIFR_52  |
            | pay_i:identificativoUnivocoMittente    | Empty                                                                   | SIN_NIFR_53  |
            | pay_i:tipoIdentificativoUnivoco        | None                                                                    | SIN_NIFR_54  |
            | pay_i:tipoIdentificativoUnivoco        | Empty                                                                   | SIN_NIFR_55  |
            | pay_i:tipoIdentificativoUnivoco        | C                                                                       | SIN_NIFR_56  |
            | pay_i:codiceIdentificativoUnivoco      | None                                                                    | SIN_NIFR_57  |
            | pay_i:codiceIdentificativoUnivoco      | Empty                                                                   | SIN_NIFR_58  |
            | pay_i:codiceIdentificativoUnivoco      | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_59  |
            | pay_i:denominazioneMittente            | Empty                                                                   | SIN_NIFR_61  |
            | pay_i:denominazioneMittente            | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | SIN_NIFR_62  |
            | pay_i:codiceBicBancaDiRiversamento     | Empty                                                                   | SIN_NIFR_64  |
            | pay_i:codiceBicBancaDiRiversamento     | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_65  |
            | pay_i:istitutoRicevente                | Empty                                                                   | SIN_NIFR_66  |
            | pay_i:istitutoRicevente                | RemoveParent                                                            | SIN_NIFR_67  |
            | pay_i:identificativoUnivocoRicevente   | RemoveParent                                                            | SIN_NIFR_68  |
            | pay_i:identificativoUnivocoRicevente   | Empty                                                                   | SIN_NIFR_69  |
            | pay_i:tipoIdentificativoUnivoco        | None                                                                    | SIN_NIFR_70  |
            | pay_i:tipoIdentificativoUnivoco        | Empty                                                                   | SIN_NIFR_71  |
            | pay_i:tipoIdentificativoUnivoco        | D                                                                       | SIN_NIFR_72  |
            | pay_i:codiceIdentificativoUnivoco      | None                                                                    | SIN_NIFR_73  |
            | pay_i:codiceIdentificativoUnivoco      | Empty                                                                   | SIN_NIFR_74  |
            | pay_i:codiceIdentificativoUnivoco      | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_75  |
            | pay_i:denominazioneRicevente           | Empty                                                                   | SIN_NIFR_77  |
            | pay_i:numeroTotalePagamenti            | None                                                                    | SIN_NIFR_79  |
            | pay_i:numeroTotalePagamenti            | Empty                                                                   | SIN_NIFR_80  |
            | pay_i:numeroTotalePagamenti            | aaaaaaaaaaaaaaaa                                                        | SIN_NIFR_81  |
            | pay_i:importoTotalePagamenti           | None                                                                    | SIN_NIFR_82  |
            | pay_i:importoTotalePagamenti           | Empty                                                                   | SIN_NIFR_83  |
            | pay_i:importoTotalePagamenti           | 2000987689.00                                                           | SIN_NIFR_84  |
            | pay_i:importoTotalePagamenti           | 20.0                                                                    | SIN_NIFR_86  |
            | pay_i:importoTotalePagamenti           | 20.000                                                                  | SIN_NIFR_86  |
            | pay_i:importoTotalePagamenti           | a.00                                                                    | SIN_NIFR_87  |
            | pay_i:importoTotalePagamenti           | 1,00                                                                    | SIN_NIFR_88  |
            | pay_i:datiSingoliPagamenti             | None                                                                    | SIN_NIFR_89  |
            | pay_i:datiSingoliPagamenti             | Empty                                                                   | SIN_NIFR_90  |
            | pay_i:identificativoUnivocoVersamento  | None                                                                    | SIN_NIFR_91  |
            | pay_i:identificativoUnivocoVersamento  | Empty                                                                   | SIN_NIFR_92  |
            | pay_i:identificativoUnivocoVersamento  | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_93  |
            | pay_i:identificativoUnivocoRiscossione | None                                                                    | SIN_NIFR_94  |
            | pay_i:identificativoUnivocoRiscossione | Empty                                                                   | SIN_NIFR_95  |
            | pay_i:identificativoUnivocoRiscossione | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa                                    | SIN_NIFR_96  |
            | pay_i:indiceDatiSingoloPagamento       | Empty                                                                   | SIN_NIFR_98  |
            | pay_i:indiceDatiSingoloPagamento       | 10                                                                      | SIN_NIFR_99  |
            | pay_i:indiceDatiSingoloPagamento       | a                                                                       | SIN_NIFR_100 |
            | pay_i:singoloImportoPagato             | None                                                                    | SIN_NIFR_101 |
            | pay_i:singoloImportoPagato             | Empty                                                                   | SIN_NIFR_102 |
            | pay_i:singoloImportoPagato             | 1099999999.00                                                           | SIN_NIFR_103 |
            | pay_i:singoloImportoPagato             | 0.00                                                                    | SIN_NIFR_104 |
            | pay_i:singoloImportoPagato             | 1.0                                                                     | SIN_NIFR_105 |
            | pay_i:singoloImportoPagato             | 1.000                                                                   | SIN_NIFR_105 |
            | pay_i:singoloImportoPagato             | a.00                                                                    | SIN_NIFR_106 |
            | pay_i:singoloImportoPagato             | 1,00                                                                    | SIN_NIFR_107 |
            | pay_i:codiceEsitoSingoloPagamento      | None                                                                    | SIN_NIFR_108 |
            | pay_i:codiceEsitoSingoloPagamento      | Empty                                                                   | SIN_NIFR_109 |
            | pay_i:codiceEsitoSingoloPagamento      | 10                                                                      | SIN_NIFR_110 |
            | pay_i:codiceEsitoSingoloPagamento      | 1                                                                       | SIN_NIFR_111 |
            | pay_i:dataEsitoSingoloPagamento        | None                                                                    | SIN_NIFR_112 |
            | pay_i:dataEsitoSingoloPagamento        | Empty                                                                   | SIN_NIFR_113 |
            | pay_i:dataEsitoSingoloPagamento        | $timedate                                                               | SIN_NIFR_114 |
