Feature: Semantic checks for nodoChiediElencoFlussiRendicontazione - KO

    Background:
        Given systems up
    
    @runnable
    Scenario Outline: Check semantic errors for nodoChiediElencoFlussiRendicontazione primitive
        Given initial XML nodoChiediElencoFlussiRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header />
            <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
            <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
            <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
            <password>#password#</password>
            <identificativoDominio>#creditor_institution_code#</identificativoDominio>
            <identificativoPSP>#psp#</identificativoPSP>
            </ws:nodoChiediElencoFlussiRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And <elem> with <value> in nodoChiediElencoFlussiRendicontazione
        When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
        Then check faultCode is <error> of nodoChiediElencoFlussiRendicontazione response
        Examples:
            | elem                                  | value                | error                             | soapUI test |
            | identificativoIntermediarioPA         | ciaoIntermediarioPA  | PPT_INTERMEDIARIO_PA_SCONOSCIUTO  | CEFRSEM1    |
            | identificativoIntermediarioPA         | INT_NOT_ENABLED      | PPT_INTERMEDIARIO_PA_DISABILITATO | CEFRSEM2    |
            | identificativoStazioneIntermediarioPA | ciaoStazionePA       | PPT_STAZIONE_INT_PA_SCONOSCIUTA   | CEFRSEM3    |
            | identificativoStazioneIntermediarioPA | STAZIONE_NOT_ENABLED | PPT_STAZIONE_INT_PA_DISABILITATA  | CEFRSEM4    |
            | identificativoDominio                 | ciaoDominio          | PPT_DOMINIO_SCONOSCIUTO           | CEFRSEM6    |
            | identificativoDominio                 | NOT_ENABLED          | PPT_DOMINIO_DISABILITATO          | CEFRSEM7    |
            | identificativoPSP                     | ciaoPSP              | PPT_PSP_SCONOSCIUTO               | CEFRSEM8    |
            | identificativoPSP                     | NOT_ENABLED          | PPT_PSP_DISABILITATO              | CEFRSEM9    |
            | identificativoIntermediarioPA         | 90000000001          | PPT_AUTORIZZAZIONE                | CEFRSEM10   |
            | password                              | password01           | PPT_AUTENTICAZIONE                | CEFRSEM5    |
