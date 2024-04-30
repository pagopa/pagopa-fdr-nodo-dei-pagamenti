Feature: Syntax checks OK for nodoChiediElencoFlussiRendicontazione

    Background:
        Given systems up

    @runnable
    Scenario Outline: Syntax checks OK for nodoChiediElencoFlussiRendicontazione - TEST1
        Given initial XML nodoChiediElencoFlussiRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
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
        And  <elem> with <value> in nodoChiediElencoFlussiRendicontazione
        When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
        Then check totRestituiti field exists in nodoChiediElencoFlussiRendicontazione response
        Examples:
            | elem                  | value | soapUI test |
            | identificativoDominio | None  | CEFRSIN16.1 |
            | identificativoPSP     | None  | CEFRSIN18   |


    @runnable
    Scenario: Syntax checks OK for nodoChiediElencoFlussiRendicontazione - TEST2
        Given initial XML nodoChiediElencoFlussiRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
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
        When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
        Then check totRestituiti field exists in nodoChiediElencoFlussiRendicontazione response
