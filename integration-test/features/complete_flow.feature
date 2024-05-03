Feature: Complete happy path for reporting flow operations

  Background:
    Given systems up

  @runnable
  Scenario Outline: Standard scenario, single FlussoRiversamento single datiSingoliPagamenti in report
    # Sending the nodoInviaFlussoRendicontazione
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
    And <tag> with <tag_value> in FlussoRiversamento
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
    And EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
    And check esito is OK of nodoInviaFlussoRendicontazione response
    # Requiring the list of reports with nodoChiediElencoFlussiRendicontazione
    And an XML for nodoChiediElencoFlussiRendicontazione
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
    And EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    And check if totRestituiti is not 0 in nodoChiediElencoFlussiRendicontazione response
    # Retrieve the generated report with nodoChiediFlussoRendicontazione
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
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check xmlRendicontazione field exists in nodoChiediFlussoRendicontazione response
    Examples:
      | tag                                | tag_value                                                                                                                                    | soapUI test |
      | pay_i:denominazioneMittente        | None                                                                                                                                         | SIN_NIFR_60 |
      | pay_i:denominazioneRicevente       | None                                                                                                                                         | ?           |
      | pay_i:denominazioneRicevente       | a                                                                                                                                            | ?           |
      | pay_i:denominazioneRicevente       | aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa | ?           |
      | pay_i:codiceBicBancaDiRiversamento | None                                                                                                                                         | SIN_NIFR_63 |
      | pay_i:codiceBicBancaDiRiversamento | None                                                                                                                                         | SIN_NIFR_76 |
      | pay_i:importoTotalePagamenti       | 0.00                                                                                                                                         | SIN_NIFR_85 |
      | pay_i:codiceBicBancaDiRiversamento | None                                                                                                                                         | SIN_NIFR_97 |


  @runnable
  Scenario: Standard scenario, single FlussoRiversamento multiple datiSingoliPagamenti in report
    # Sending the nodoInviaFlussoRendicontazione
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
      <pay_i:importoTotalePagamenti>30.00</pay_i:importoTotalePagamenti>
      <pay_i:datiSingoliPagamenti>
          <pay_i:identificativoUnivocoVersamento>#random_iuv#</pay_i:identificativoUnivocoVersamento>
          <pay_i:identificativoUnivocoRiscossione>#random_iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
          <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
          <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
      </pay_i:datiSingoliPagamenti>
      <pay_i:datiSingoliPagamenti>
          <pay_i:identificativoUnivocoVersamento>#random_iuv#</pay_i:identificativoUnivocoVersamento>
          <pay_i:identificativoUnivocoRiscossione>#random_iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>8.00</pay_i:singoloImportoPagato>
          <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
          <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
      </pay_i:datiSingoliPagamenti>
      <pay_i:datiSingoliPagamenti>
          <pay_i:identificativoUnivocoVersamento>#random_iuv#</pay_i:identificativoUnivocoVersamento>
          <pay_i:identificativoUnivocoRiscossione>#random_iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>2.00</pay_i:singoloImportoPagato>
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
    And EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
    And check esito is OK of nodoInviaFlussoRendicontazione response
    # Requiring the list of reports with nodoChiediElencoFlussiRendicontazione
    And an XML for nodoChiediElencoFlussiRendicontazione
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
    And EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    And check if totRestituiti is not 0 in nodoChiediElencoFlussiRendicontazione response
    # Retrieve the generated report with nodoChiediFlussoRendicontazione
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
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check xmlRendicontazione field exists in nodoChiediFlussoRendicontazione response


  @runnable
  Scenario: Generate first flow, then send another similar it and correctly read updated flow
    # first send of the report flow
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
    And EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    And check xmlRendicontazione field exists in nodoChiediFlussoRendicontazione response
    And check if pay_i:importoTotalePagamenti field is 10.00 in base64 xmlRendicontazione field of nodoChiediFlussoRendicontazione response
    # second send of the report flow, with a new transfer and an updated total amount
    And an XML for FlussoRiversamento
    """
    <pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
      <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
      <pay_i:identificativoFlusso>$identificativoFlusso</pay_i:identificativoFlusso>
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
      <pay_i:importoTotalePagamenti>60.00</pay_i:importoTotalePagamenti>
      <pay_i:datiSingoliPagamenti>
          <pay_i:identificativoUnivocoVersamento>#iuv#</pay_i:identificativoUnivocoVersamento>
          <pay_i:identificativoUnivocoRiscossione>#iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>10.00</pay_i:singoloImportoPagato>
          <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
          <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
      </pay_i:datiSingoliPagamenti>
      <pay_i:datiSingoliPagamenti>
          <pay_i:identificativoUnivocoVersamento>#random_iuv#</pay_i:identificativoUnivocoVersamento>
          <pay_i:identificativoUnivocoRiscossione>#random_iur#</pay_i:identificativoUnivocoRiscossione>
          <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
          <pay_i:singoloImportoPagato>50.00</pay_i:singoloImportoPagato>
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
    # now check if nodoChiediElencoFlussiRendicontazione has at least one element
    And an XML for nodoChiediElencoFlussiRendicontazione
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
    And EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    And check if totRestituiti is not 0 in nodoChiediElencoFlussiRendicontazione response
    # now check if nodoChiediFlussoRendicontazione can find the required report and the total amount value was correctly updated
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
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check xmlRendicontazione field exists in nodoChiediFlussoRendicontazione response
    And check if pay_i:importoTotalePagamenti field is 60.00 in base64 xmlRendicontazione field of nodoChiediFlussoRendicontazione response


  @bigfdr
  Scenario: Standard scenario, big report
    # Sending the nodoInviaFlussoRendicontazione
    Given an XML for FlussoRiversamento with lots of transfers
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
      [TRANSFERS]
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
    And EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
    And check esito is OK of nodoInviaFlussoRendicontazione response
    # Requiring the list of reports with nodoChiediElencoFlussiRendicontazione
    And an XML for nodoChiediElencoFlussiRendicontazione
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
    And EC sends SOAP nodoChiediElencoFlussiRendicontazione to nodo-dei-pagamenti
    And check if totRestituiti is not 0 in nodoChiediElencoFlussiRendicontazione response
    # Retrieve the generated report with nodoChiediFlussoRendicontazione
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
    When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
    Then check xmlRendicontazione field exists in nodoChiediFlussoRendicontazione response

