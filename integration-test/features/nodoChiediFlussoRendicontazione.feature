Feature: Syntax and semantic checks for nodoChiediFlussoRendicontazione

  Background:
    Given systems up

  Scenario: Reporting flow generation
    Given the generated report
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


  # [CFRSIN0]
  @runnable
  Scenario: nodoChiediFlussoRendicontazione - Syntax error: wrong WSDL namespace
    Given an XML for nodoChiediFlussoRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/WRONG.ENVELOPE/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header>
            <ws:nodoChiediFlussoRendicontazione>ciao</ws:nodoChiediFlussoRendicontazione>
        </soapenv:Header>
        <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
                <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
                <password>#password#</password>
                <identificativoDominio>#creditor_institution_code#</identificativoDominio>
                <identificativoPSP>#psp#</identificativoPSP>
                <identificativoFlusso>#identificativoFlusso#</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediFlussoRendicontazione response


  #[CFRSIN1]
  @runnable
  Scenario: nodoChiediFlussoRendicontazione - Syntax error: wrong header content
    Given an XML for nodoChiediFlussoRendicontazione
    """
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/" xmlns:ppt="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header>
            <ppt:nodoChiediFlussoRendicontazione>FAKE_REPORT_001</ppt:nodoChiediFlussoRendicontazione>
        </soapenv:Header>
        <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
                <identificativoIntermediarioPA>#broker_ci#</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>#id_station#</identificativoStazioneIntermediarioPA>
                <password>#password#</password>
                <identificativoDominio>#creditor_institution_code#</identificativoDominio>
                <identificativoPSP>#psp#</identificativoPSP>
                <identificativoFlusso>#identificativoFlusso#</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediFlussoRendicontazione response


  @runnable
  Scenario Outline: nodoChiediFlussoRendicontazione - Syntax error: wrong content on tag
    Given an XML for nodoChiediFlussoRendicontazione
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
                <identificativoFlusso>#identificativoFlusso#</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    And <elem> with <value> in nodoChiediFlussoRendicontazione
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediFlussoRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediFlussoRendicontazione response
    Examples:
      | elem                                  | value                                | soapUI test |
      | soapenv:Body                          | Empty                                | CFRSIN2     |
      | ws:nodoChiediFlussoRendicontazione    | Empty                                | CFRSIN4     |
      | soapenv:Body                          | None                                 | CFRSIN3     |
      | identificativoIntermediarioPA         | None                                 | CFRSIN6     |
      | identificativoIntermediarioPA         | Empty                                | CFRSIN7     |
      | identificativoIntermediarioPA         | dbcFkSRY15k6mEaIVGoki0OcZKyVboNtjndJ | CFRSIN8     |
      | identificativoStazioneIntermediarioPA | None                                 | CFRSIN9     |
      | identificativoStazioneIntermediarioPA | Empty                                | CFRSIN10    |
      | identificativoStazioneIntermediarioPA | k91JETYVnE7grIIKbzWE6Di7XKM3ymJeawhf | CFRSIN11    |
      | password                              | None                                 | CFRSIN12    |
      | password                              | Empty                                | CFRSIN13    |
      | password                              | Xlve3Jc                              | CFRSIN14    |
      | password                              | xxkV8x4phzRKyiuE                     | CFRSIN15    |
      | identificativoPSP                     | Empty                                | CFRSIN20    |
      | identificativoDominio                 | Empty                                | CFRSIN17    |
      | identificativoDominio                 | k91JETYVnE7grIIKbzWE6Di7XKM3ymJeawhf | CFRSIN18    |
      | identificativoPSP                     | Empty                                | CFRSIN20    |
      | identificativoPSP                     | k91JETYVnE7grIIKbzWE6Di7XKM3ymJeawhf | CFRSIN21    |
      | identificativoFlusso                  | None                                 | CFRSIN22    |


  @runnable
  Scenario Outline: nodoChiediFlussoRendicontazione - Syntax error: missing domain and PSP
    Given an XML for nodoChiediFlussoRendicontazione
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
                <identificativoFlusso>#identificativoFlusso#</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check ppt:nodoChiediFlussoRendicontazioneRisposta field exists in nodoChiediFlussoRendicontazione response
    Examples:
      | elem                  | value | soapUI test |
      | identificativoDominio | None  | CFRSIN16    |
      | identificativoPSP     | None  | CFRSIN19    |


  # [CFRSIN23]
  @runnable
  Scenario: nodoChiediFlussoRendicontazione - Syntax error: missing identificativoFlusso
    Given an XML for nodoChiediFlussoRendicontazione
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
                <identificativoFlusso></identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check faultCode is PPT_ID_FLUSSO_SCONOSCIUTO of nodoChiediFlussoRendicontazione response


  # [CFRSIN24]
  @runnable
  Scenario: nodoChiediFlussoRendicontazione - Syntax error: invalid field
    Given an XML for nodoChiediFlussoRendicontazione
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
                <identificativoFlusso>#identificativoFlusso#</identificativoFlusso>
                <rpt>content</rpt>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>
    """
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediFlussoRendicontazione response
    And check faultString is Errore di sintassi extra XSD. of nodoChiediFlussoRendicontazione response


  @runnable
  Scenario Outline: nodoChiediFlussoRendicontazione - Semantic error: invalid entities for reporting flow request
    Given the Reporting flow generation scenario executed successfully
    And an XML for nodoInviaFlussoRendicontazione
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
    And PSP sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
    And check esito is OK of nodoInviaFlussoRendicontazione response
    And an XML for nodoChiediFlussoRendicontazione
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


  @runnable
  Scenario Outline: nodoChiediFlussoRendicontazione - Sintax/semantic error: wrong content on password
    Given an XML for FlussoRiversamento
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
          <pay_i:identificativoUnivocoRiscossione>#iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
          <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
          <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
      </pay_i:datiSingoliPagamenti>
    </pay_i:FlussoRiversamento>
    """
    And the generated report
    """
    $FlussoRiversamento
    """
    And an XML for nodoInviaFlussoRendicontazione
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
    And PSP sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
    And check esito is OK of nodoInviaFlussoRendicontazione response
    And an XML for nodoChiediFlussoRendicontazione
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
    Then check faultString is Errore di sintassi extra XSD. of nodoChiediFlussoRendicontazione response
    And check faultCode is PPT_SINTASSI_EXTRAXSD of nodoChiediFlussoRendicontazione response
    Examples:
      | elem                                  | value                                | soapUI test |
      | password                              | None                                 | CFRSIN12    |
      | password                              | Empty                                | CFRSIN13    |
      | password                              | Xlve3Jc                              | CFRSIN14    |
      | password                              | xxkV8x4phzRKyiuE                     | CFRSIN15    |