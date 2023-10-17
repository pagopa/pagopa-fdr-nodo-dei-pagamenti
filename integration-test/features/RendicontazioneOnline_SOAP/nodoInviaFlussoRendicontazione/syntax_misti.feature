Feature: PAG-1879

    Background:
        Given systems up
        And initial xml REND
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
            <pay_i:denominazioneRicevente>ciao</pay_i:denominazioneRicevente>
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

    @test <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
    Scenario Outline: Test syntax error
        Given pay_i:denominazioneRicevente with <value> in REND
        And REND generation
            """
            $REND
            """
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
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
        Then check esito is KO of nodoInviaFlussoRendicontazione response
        And check faultCode is PPT_SINTASSI_XSD of nodoInviaFlussoRendicontazione response
        Examples:
            | value                                                                                                                                         |
            | Empty                                                                                                                                         |
            | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa |

    @test <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
    Scenario Outline: Test OK
        Given REND generation
            """
            $REND
            """
        Given pay_i:denominazioneRicevente with <value> in REND
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
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
        Then check esito is OK of nodoInviaFlussoRendicontazione response
        Examples:
            | value                                                                                                                                        |
            | None                                                                                                                                         |
            | a                                                                                                                                            |
            | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa |
