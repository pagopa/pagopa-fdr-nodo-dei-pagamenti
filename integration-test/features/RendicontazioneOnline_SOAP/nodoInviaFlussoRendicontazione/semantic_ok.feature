Feature: Syntax checks for nodoInviaFlussoRendicontazione - OK

    Background:
        Given systems up

    Scenario: Create REND
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

#     [SEM_NIFR_16]
    Scenario: Send nodoInviaFlussoRendicontazione primitive - TEST1
        Given the Create REND scenario executed successfully
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
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response


    Scenario: Create REND2
        Given the Send nodoInviaFlussoRendicontazione primitive scenario executed successfully
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
    Scenario: Send nodoInviaFlussoRendicontazione_dataOraMaggiore primitive - TEST2
        Given the Create REND2 scenario executed successfully
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
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    # [SEM_NIFR_18]
    Scenario: Create REND3
        Given REND generation
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>2021-10-24T12:14:29.176</pay_i:dataOraFlusso>
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
    Scenario: Send nodoInviaFlussoRendicontazione3 primitive - TEST3
        Given the Create REND3 scenario executed successfully
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
            <dataOraFlusso>2021-10-24T12:14:29.176</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    Scenario: Create REND4
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
    Scenario: Send nodoInviaFlussoRendicontazione4 primitive - TEST4
        Given the Create REND4 scenario executed successfully
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
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    # [SEM_NIFR_23]
    Scenario: Create REND5
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
            <pay_i:codiceIdentificativoUnivoco>80000000001</pay_i:codiceIdentificativoUnivoco>
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
    Scenario: Send nodoInviaFlussoRendicontazione5 primitive - TEST5
        Given the Create REND5 scenario executed successfully
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
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    # [SEM_NIFR_25]
    Scenario: Create REND6
        Given REND generation
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>#timedate+1#</pay_i:dataOraFlusso>
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
    Scenario: Send nodoInviaFlussoRendicontazione6 primitive - TEST6
        Given the Create REND6 scenario executed successfully
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
            <dataOraFlusso>$futureTimedate</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    @runnable
    # [SEM_NIFR_24]
    Scenario: Send nodoInviaFlussoRendicontazione7 primitive - TEST7
        Given the Create REND scenario executed successfully
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
            <dataOraFlusso>$timedate+01:00</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    #[SEM_NIFR_12]
    Scenario: Create REND7
        Given REND generation
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>2022-01-13T12:52:06.936</pay_i:dataOraFlusso>
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
    Scenario: Send nodoInviaFlussoRendicontazione8 primitive - TEST8
        Given the Create REND7 scenario executed successfully
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
            <dataOraFlusso>2022-01-13T12:52:06.936</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    #[SEM_NIFR_13]
    Scenario: Create REND8
        Given REND generation
            """
            <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
            <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
            <pay_i:identificativoFlusso>#identificativoFlusso#</pay_i:identificativoFlusso>
            <pay_i:dataOraFlusso>2923-01-13T12:52:06.936</pay_i:dataOraFlusso>
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
    Scenario: Send nodoInviaFlussoRendicontazione9 primitive - TEST9
        Given the Create REND8 scenario executed successfully
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
            <dataOraFlusso>2923-01-13T12:52:06.936</dataOraFlusso>
            <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

    @runnable
    #[SEM_NIFR_22]
    Scenario: Send nodoInviaFlussoRendicontazione10 primitive - TEST10
        Given the Create REND scenario executed successfully
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
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response
