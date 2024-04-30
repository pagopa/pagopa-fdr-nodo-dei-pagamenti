Feature: Semantic checks for nodoChiediFlussoRendicontazione

    Background:
        Given systems up
        And REND generation
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
            <pay_i:codiceIdentificativoUnivoco>IDPSPFNZ</pay_i:codiceIdentificativoUnivoco>
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
            <pay_i:identificativoUnivocoVersamento>$iuv</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>$iuv</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
            </pay_i:datiSingoliPagamenti>
            </pay_i:FlussoRiversamento>
            """
    
    Scenario: Executed nodoInviaFlussoRendicontazione
        Given initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#channel#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When PSP sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response
    
    @runnable
    Scenario Outline: Check semantic errors for nodoChiediFlussoRendicontazione primitive - TEST1
        Given the Executed nodoInviaFlussoRendicontazione scenario executed successfully
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And <elem> with <value> in nodoChiediFlussoRendicontazione
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is <error> of nodoChiediFlussoRendicontazione response
        Examples:
            | elem                                  | value                     | error                             | soapUI test |
            | identificativoIntermediarioPA         | ciaoIntermediarioPA       | PPT_INTERMEDIARIO_PA_SCONOSCIUTO  | CFRSEM1     |
            | identificativoIntermediarioPA         | INT_NOT_ENABLED           | PPT_INTERMEDIARIO_PA_DISABILITATO | CFRSEM2     |
            | identificativoStazioneIntermediarioPA | ciaoStazionePA            | PPT_STAZIONE_INT_PA_SCONOSCIUTA   | CFRSEM3     |
            | identificativoStazioneIntermediarioPA | STAZIONE_NOT_ENABLED      | PPT_STAZIONE_INT_PA_DISABILITATA  | CFRSEM4     |
            | password                              | Password01                | PPT_AUTENTICAZIONE                | CFRSEM5     |
            | identificativoFlusso                  | 2017-09-11idPsp1-pluto123 | PPT_ID_FLUSSO_SCONOSCIUTO         | CFRSEM10    |

    @runnable @query
    # [CFRSEM6]
    Scenario: Aggiornamento DB_1
        Given the Executed nodoInviaFlussoRendicontazione scenario executed successfully
        And update through the query param_update of the table RENDICONTAZIONE the parameter DOMINIO with ciaoDominio, with where condition ID_FLUSSO and where value $identificativoFlusso under macro update_query on db fdr
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And identificativoDominio with ciaoDominio in nodoChiediFlussoRendicontazione
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_DOMINIO_SCONOSCIUTO of nodoChiediFlussoRendicontazione response

    @runnable @query
    # [CFRSEM7]
    Scenario: Aggiornamento DB_2
        Given the Executed nodoInviaFlussoRendicontazione scenario executed successfully
        And update through the query param_update of the table RENDICONTAZIONE the parameter DOMINIO with NOT_ENABLED, with where condition ID_FLUSSO and where value $identificativoFlusso under macro update_query on db fdr
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And identificativoDominio with NOT_ENABLED in nodoChiediFlussoRendicontazione
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_DOMINIO_DISABILITATO of nodoChiediFlussoRendicontazione response

    # [CFRSEM11]
    @runnable @query
    Scenario: Aggiornamento DB_3
        Given the Executed nodoInviaFlussoRendicontazione scenario executed successfully
        And update through the query param_update of the table RENDICONTAZIONE the parameter DOMINIO with 90000000001, with where condition ID_FLUSSO and where value $identificativoFlusso under macro update_query on db fdr
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>90000000001</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_AUTORIZZAZIONE of nodoChiediFlussoRendicontazione response
    
    # Send un nuovo flusso rendicontazione corretto
    Scenario: Executed nodoInviaFlussoRendicontazione_1
        Given initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoIntermediarioPSP>#broker_psp#</identificativoIntermediarioPSP>
            <identificativoCanale>#channel#</identificativoCanale>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            <dataOraFlusso>$timedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When PSP sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response


    @runnable @query
    # [CFRSEM8]
    Scenario: Check semantic errors for nodoChiediFlussoRendicontazione primitive - TEST3
        Given the Executed nodoInviaFlussoRendicontazione_1 scenario executed successfully
        And update through the query param_update of the table RENDICONTAZIONE the parameter PSP with ciaoPSP, with where condition ID_FLUSSO and where value $identificativoFlusso under macro update_query on db fdr
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And identificativoPSP with ciaoPSP in nodoChiediFlussoRendicontazione
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_PSP_SCONOSCIUTO of nodoChiediFlussoRendicontazione response


    @runnable @query
    # [CFRSEM9]
    Scenario: Check semantic errors for nodoChiediFlussoRendicontazione primitive
        Given the Executed nodoInviaFlussoRendicontazione_1 scenario executed successfully
        And update through the query param_update of the table RENDICONTAZIONE the parameter PSP with NOT_ENABLED, with where condition ID_FLUSSO and where value $identificativoFlusso under macro update_query on db fdr
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And identificativoPSP with NOT_ENABLED in nodoChiediFlussoRendicontazione
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_PSP_DISABILITATO of nodoChiediFlussoRendicontazione response
