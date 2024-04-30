Feature: Syntax and semantic checks for nodoChiediElencoFlussiRendicontazione

  Background:
    Given systems up


  #[CEFRSIN0]
  @runnable
  Scenario: nodoChiediElencoFlussiRendicontazione - Syntax error: wrong WSDL namespace
    Given initial XML nodoChiediElencoFlussiRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/ciao/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header>
            <ws:nodoChiediElencoFlussiRendicontazione />
        </soapenv:Header>
        <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
                <identificativoIntermediarioPA>#creditor_institution_code#</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
                <password>#password#</password>
            </ws:nodoChiediElencoFlussiRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediElencoFlussiRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediElencoFlussiRendicontazione response
    And check description is Errore validazione XML [Envelope] - cvc-elt.1.a: impossibile trovare la dichiarazione dell'elemento "soapenv:Envelope". of nodoChiediElencoFlussiRendicontazione response


  #[CEFRSIN1]
  @runnable
  Scenario: nodoChiediElencoFlussiRendicontazione - Syntax error: Missing fields
    Given initial XML nodoChiediElencoFlussiRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ppt="http://ws.pagamenti.telematici.gov/ppthead" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header>
            <ppt:nodoChiediElencoFlussiRendicontazione />
        </soapenv:Header>
        <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
                <identificativoIntermediarioPA>#creditor_institution_code#</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
                <password>#password#</password>
            </ws:nodoChiediElencoFlussiRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediElencoFlussiRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediElencoFlussiRendicontazione response


  #[CEFRSIN3]
  @runnable
  Scenario: nodoChiediElencoFlussiRendicontazione - Syntax error: Empty body
    Given initial XML nodoChiediElencoFlussiRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
        <soapenv:Header />
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediElencoFlussiRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediElencoFlussiRendicontazione response


  @runnable
  Scenario Outline: nodoChiediElencoFlussiRendicontazione - Syntax error: partial body and wrong content on tag
    Given initial XML nodoChiediElencoFlussiRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
                <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
                <password>#password#</password>
            </ws:nodoChiediElencoFlussiRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    And <elem> with <value> in nodoChiediElencoFlussiRendicontazione
    When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediElencoFlussiRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediElencoFlussiRendicontazione response
    Examples:
      | elem                                     | value                                | soapUI test |
      | soapenv:Body                             | Empty                                | CEFRSIN2    |
      | ws:nodoChiediElencoFlussiRendicontazione | Empty                                | CEFRSIN4    |
      | identificativoIntermediarioPA            | None                                 | CEFRSIN6    |
      | identificativoIntermediarioPA            | Empty                                | CEFRSIN7    |
      | identificativoIntermediarioPA            | dbcFkSRY15k6mEaIVGoki0OcZKyVboNtjndJ | CEFRSIN8    |
      | identificativoStazioneIntermediarioPA    | None                                 | CEFRSIN9    |
      | identificativoStazioneIntermediarioPA    | Empty                                | CEFRSIN10   |
      | identificativoStazioneIntermediarioPA    | k91JETYVnE7grIIKbzWE6Di7XKM3ymJeawhf | CEFRSIN11   |
      | password                                 | None                                 | CEFRSIN12   |
      | password                                 | Empty                                | CEFRSIN13   |
      | password                                 | Xlve3Jc                              | CEFRSIN14   |
      | password                                 | xxkV8x4phzRKyiuE                     | CEFRSIN15   |


  @runnable
  Scenario Outline: nodoChiediElencoFlussiRendicontazione - Syntax error: wrong content on tag
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
    And <elem> with <value> in nodoChiediElencoFlussiRendicontazione
    When EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediElencoFlussiRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediElencoFlussiRendicontazione response
    Examples:
      | elem                  | value                                | soapUI test |
      | identificativoDominio | Empty                                | CEFRSIN16   |
      | identificativoDominio | pRJRvRYYpkxm6thxNaE8hxKtry5wULdLAq8X | CEFRSIN17   |
      | identificativoPSP     | Empty                                | CEFRSIN19   |
      | identificativoPSP     | qHtFhwjP3lTEs5SmYehnnK5aEZaAD1vqQukR | CEFRSIN20   |


  @runnable
  Scenario Outline: nodoChiediElencoFlussiRendicontazione - Semantic error
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
      | identificativoIntermediarioPA         | fakeIntermediarioPA  | PPT_INTERMEDIARIO_PA_SCONOSCIUTO  | CEFRSEM1    |
      | identificativoIntermediarioPA         | INT_NOT_ENABLED      | PPT_INTERMEDIARIO_PA_DISABILITATO | CEFRSEM2    |
      | identificativoStazioneIntermediarioPA | fakeStazionePA       | PPT_STAZIONE_INT_PA_SCONOSCIUTA   | CEFRSEM3    |
      | identificativoStazioneIntermediarioPA | STAZIONE_NOT_ENABLED | PPT_STAZIONE_INT_PA_DISABILITATA  | CEFRSEM4    |
      | identificativoDominio                 | fakeDominio          | PPT_DOMINIO_SCONOSCIUTO           | CEFRSEM6    |
      | identificativoDominio                 | NOT_ENABLED          | PPT_DOMINIO_DISABILITATO          | CEFRSEM7    |
      | identificativoPSP                     | fakePSP              | PPT_PSP_SCONOSCIUTO               | CEFRSEM8    |
      | identificativoPSP                     | NOT_ENABLED          | PPT_PSP_DISABILITATO              | CEFRSEM9    |
      | identificativoIntermediarioPA         | 90000000001          | PPT_AUTORIZZAZIONE                | CEFRSEM10   |
      | password                              | password01           | PPT_AUTENTICAZIONE                | CEFRSEM5    |