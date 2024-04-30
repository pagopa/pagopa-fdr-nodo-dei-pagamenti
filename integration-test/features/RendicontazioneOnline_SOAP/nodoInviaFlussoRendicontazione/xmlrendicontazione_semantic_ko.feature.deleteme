Feature: Syntax checks for nodoInviaFlussoRendicontazione - KO

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

    @runnable
    Scenario Outline: Check error for nodoInviaFlussoRendicontazione primitive
        Given the Create REND scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
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
            <xmlRendicontazione>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxwYXlfaTpGbHVzc29SaXZlcnNhbWVudG8geG1sbnM6cGF5X2k9Imh0dHA6Ly93d3cuZGlnaXRwYS5nb3YuaXQvc2NoZW1hcy8yMDExL1BhZ2FtZW50aS8iIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTpzY2hlbWFMb2NhdGlvbj0iaHR0cDovL3d3dy5kaWdpdHBhLmdvdi5pdC9zY2hlbWFzLzIwMTEvUGFnYW1lbnRpLyBGbHVzc29SZW5kaWNvbnRhemlvbmVfdl8xXzBfMS54c2QgIj4NCiAgPHBheV9pOnZlcnNpb25lT2dnZXR0bz4xLjA8L3BheV9pOnZlcnNpb25lT2dnZXR0bz4NCiAgPHBheV9pOmlkZW50aWZpY2F0aXZvRmx1c3NvPjIwMTctMDktMTFJRFBTUEZOWnJlbmQwMTwvcGF5X2k6aWRlbnRpZmljYXRpdm9GbHVzc28+DQogIDxwYXlfaTpkYXRhT3JhRmx1c3NvPjIwMTctMDktMTJUMTI6MDA6MDA8L3BheV9pOmRhdGFPcmFGbHVzc28+DQogIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SZWdvbGFtZW50bz5SRU5EU0VNMDE8L3BheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb1JlZ29sYW1lbnRvPg0KICA8cGF5X2k6ZGF0YVJlZ29sYW1lbnRvPjIwMTctMDktMTE8L3BheV9pOmRhdGFSZWdvbGFtZW50bz4NCiAgPHBheV9pOmlzdGl0dXRvTWl0dGVudGU+DQogICAgPHBheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb01pdHRlbnRlPg0KICAgICAgPHBheV9pOnRpcG9JZGVudGlmaWNhdGl2b1VuaXZvY28+RzwvcGF5X2k6dGlwb0lkZW50aWZpY2F0aXZvVW5pdm9jbz4NCiAgICAgIDxwYXlfaTpjb2RpY2VJZGVudGlmaWNhdGl2b1VuaXZvY28+SURQU1BGTlo8L3BheV9pOmNvZGljZUlkZW50aWZpY2F0aXZvVW5pdm9jbz4NCiAgICA8L3BheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb01pdHRlbnRlPg0KICAgIDxwYXlfaTpkZW5vbWluYXppb25lTWl0dGVudGU+U2ltdWxhdG9yZSBQU1AgbnVvdm8gbm9kbzwvcGF5X2k6ZGVub21pbmF6aW9uZU1pdHRlbnRlPg0KICA8L3BheV9pOmlzdGl0dXRvTWl0dGVudGU+DQogIDxwYXlfaTpjb2RpY2VCaWNCYW5jYURpUml2ZXJzYW1lbnRvPkJJQ0lEUFNQPC9wYXlfaTpjb2RpY2VCaWNCYW5jYURpUml2ZXJzYW1lbnRvPg0KICA8cGF5X2k6aXN0aXR1dG9SaWNldmVudGU+DQogICAgPHBheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb1JpY2V2ZW50ZT4NCiAgICAgIDxwYXlfaTp0aXBvSWRlbnRpZmljYXRpdm9Vbml2b2NvPkc8L3BheV9pOnRpcG9JZGVudGlmaWNhdGl2b1VuaXZvY28+DQogICAgICA8cGF5X2k6Y29kaWNlSWRlbnRpZmljYXRpdm9Vbml2b2NvPjkwMDAwMDAwMDAxPC9wYXlfaTpjb2RpY2VJZGVudGlmaWNhdGl2b1VuaXZvY28+DQogICAgPC9wYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SaWNldmVudGU+DQogICAgPHBheV9pOmRlbm9taW5hemlvbmVSaWNldmVudGU+U2ltdWxhdG9yZSB4IG51b3ZvIG5vZG88L3BheV9pOmRlbm9taW5hemlvbmVSaWNldmVudGU+DQogIDwvcGF5X2k6aXN0aXR1dG9SaWNldmVudGU+DQogIDxwYXlfaTpudW1lcm9Ub3RhbGVQYWdhbWVudGk+MTwvcGF5X2k6bnVtZXJvVG90YWxlUGFnYW1lbnRpPg0KICA8cGF5X2k6aW1wb3J0b1RvdGFsZVBhZ2FtZW50aT4xMC4wMDwvcGF5X2k6aW1wb3J0b1RvdGFsZVBhZ2FtZW50aT4NCiAgPHBheV9pOmRhdGlTaW5nb2xpUGFnYW1lbnRpPg0KICAgIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29WZXJzYW1lbnRvPlJSSVVWMDExPC9wYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29WZXJzYW1lbnRvPg0KICAgIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SaXNjb3NzaW9uZT5DQ0QwMTwvcGF5X2k6aWRlbnRpZmljYXRpdm9Vbml2b2NvUmlzY29zc2lvbmU+DQoJPHBheV9pOmluZGljZURhdGlTaW5nb2xvUGFnYW1lbnRvPjE8L3BheV9pOmluZGljZURhdGlTaW5nb2xvUGFnYW1lbnRvPg0KICAgIDxwYXlfaTpzaW5nb2xvSW1wb3J0b1BhZ2F0bz4xMC4wMDwvcGF5X2k6c2luZ29sb0ltcG9ydG9QYWdhdG8+DQogICAgPHBheV9pOmNvZGljZUVzaXRvU2luZ29sb1BhZ2FtZW50bz4wPC9wYXlfaTpjb2RpY2VFc2l0b1NpbmdvbG9QYWdhbWVudG8+DQogICAgPHBheV9pOmRhdGFFc2l0b1NpbmdvbG9QYWdhbWVudG8+MjAxNy0wOS0wMTwvcGF5X2k6ZGF0YUVzaXRvU2luZ29sb1BhZ2FtZW50bz4NCiAgPC9wYXlfaTpkYXRpU2luZ29saVBhZ2FtZW50aT4NCjwvcGF5X2k6Rmx1c3NvUml2ZXJzYW1lbnRvPg0K</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        And <tag> with <tag_value> in nodoInviaFlussoRendicontazione
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check faultCode is PPT_SEMANTICA of nodoInviaFlussoRendicontazione response
        Examples:
            | tag                  | tag_value                 | soapUI test |
            | identificativoFlusso | 201-09-12IDPSPFNZ-ciao123 | SEM_NIFR_27 |
            | identificativoFlusso | 2017-09-12IDPSPFN-ciao125 | SEM_NIFR_29 |


    # [SEM_NIFR_26]
    Scenario: Create REND2
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
    Scenario: Send nodoInviaFlussoRendicontazione primitive
        Given the Create REND2 scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
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
            <dataOraFlusso>2021-10-24T12:14:29.176</dataOraFlusso>
            <xmlRendicontazione>PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxwYXlfaTpGbHVzc29SaXZlcnNhbWVudG8geG1sbnM6cGF5X2k9Imh0dHA6Ly93d3cuZGlnaXRwYS5nb3YuaXQvc2NoZW1hcy8yMDExL1BhZ2FtZW50aS8iIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTpzY2hlbWFMb2NhdGlvbj0iaHR0cDovL3d3dy5kaWdpdHBhLmdvdi5pdC9zY2hlbWFzLzIwMTEvUGFnYW1lbnRpLyBGbHVzc29SZW5kaWNvbnRhemlvbmVfdl8xXzBfMS54c2QgIj4NCiAgPHBheV9pOnZlcnNpb25lT2dnZXR0bz4xLjA8L3BheV9pOnZlcnNpb25lT2dnZXR0bz4NCiAgPHBheV9pOmlkZW50aWZpY2F0aXZvRmx1c3NvPjIwMTctMDktMTFJRFBTUEZOWnJlbmQwMTwvcGF5X2k6aWRlbnRpZmljYXRpdm9GbHVzc28+DQogIDxwYXlfaTpkYXRhT3JhRmx1c3NvPjIwMTctMDktMTJUMTI6MDA6MDA8L3BheV9pOmRhdGFPcmFGbHVzc28+DQogIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SZWdvbGFtZW50bz5SRU5EU0VNMDE8L3BheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb1JlZ29sYW1lbnRvPg0KICA8cGF5X2k6ZGF0YVJlZ29sYW1lbnRvPjIwMTctMDktMTE8L3BheV9pOmRhdGFSZWdvbGFtZW50bz4NCiAgPHBheV9pOmlzdGl0dXRvTWl0dGVudGU+DQogICAgPHBheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb01pdHRlbnRlPg0KICAgICAgPHBheV9pOnRpcG9JZGVudGlmaWNhdGl2b1VuaXZvY28+RzwvcGF5X2k6dGlwb0lkZW50aWZpY2F0aXZvVW5pdm9jbz4NCiAgICAgIDxwYXlfaTpjb2RpY2VJZGVudGlmaWNhdGl2b1VuaXZvY28+SURQU1BGTlo8L3BheV9pOmNvZGljZUlkZW50aWZpY2F0aXZvVW5pdm9jbz4NCiAgICA8L3BheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb01pdHRlbnRlPg0KICAgIDxwYXlfaTpkZW5vbWluYXppb25lTWl0dGVudGU+U2ltdWxhdG9yZSBQU1AgbnVvdm8gbm9kbzwvcGF5X2k6ZGVub21pbmF6aW9uZU1pdHRlbnRlPg0KICA8L3BheV9pOmlzdGl0dXRvTWl0dGVudGU+DQogIDxwYXlfaTpjb2RpY2VCaWNCYW5jYURpUml2ZXJzYW1lbnRvPkJJQ0lEUFNQPC9wYXlfaTpjb2RpY2VCaWNCYW5jYURpUml2ZXJzYW1lbnRvPg0KICA8cGF5X2k6aXN0aXR1dG9SaWNldmVudGU+DQogICAgPHBheV9pOmlkZW50aWZpY2F0aXZvVW5pdm9jb1JpY2V2ZW50ZT4NCiAgICAgIDxwYXlfaTp0aXBvSWRlbnRpZmljYXRpdm9Vbml2b2NvPkc8L3BheV9pOnRpcG9JZGVudGlmaWNhdGl2b1VuaXZvY28+DQogICAgICA8cGF5X2k6Y29kaWNlSWRlbnRpZmljYXRpdm9Vbml2b2NvPjkwMDAwMDAwMDAxPC9wYXlfaTpjb2RpY2VJZGVudGlmaWNhdGl2b1VuaXZvY28+DQogICAgPC9wYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SaWNldmVudGU+DQogICAgPHBheV9pOmRlbm9taW5hemlvbmVSaWNldmVudGU+U2ltdWxhdG9yZSB4IG51b3ZvIG5vZG88L3BheV9pOmRlbm9taW5hemlvbmVSaWNldmVudGU+DQogIDwvcGF5X2k6aXN0aXR1dG9SaWNldmVudGU+DQogIDxwYXlfaTpudW1lcm9Ub3RhbGVQYWdhbWVudGk+MTwvcGF5X2k6bnVtZXJvVG90YWxlUGFnYW1lbnRpPg0KICA8cGF5X2k6aW1wb3J0b1RvdGFsZVBhZ2FtZW50aT4xMC4wMDwvcGF5X2k6aW1wb3J0b1RvdGFsZVBhZ2FtZW50aT4NCiAgPHBheV9pOmRhdGlTaW5nb2xpUGFnYW1lbnRpPg0KICAgIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29WZXJzYW1lbnRvPlJSSVVWMDExPC9wYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29WZXJzYW1lbnRvPg0KICAgIDxwYXlfaTppZGVudGlmaWNhdGl2b1VuaXZvY29SaXNjb3NzaW9uZT5DQ0QwMTwvcGF5X2k6aWRlbnRpZmljYXRpdm9Vbml2b2NvUmlzY29zc2lvbmU+DQoJPHBheV9pOmluZGljZURhdGlTaW5nb2xvUGFnYW1lbnRvPjE8L3BheV9pOmluZGljZURhdGlTaW5nb2xvUGFnYW1lbnRvPg0KICAgIDxwYXlfaTpzaW5nb2xvSW1wb3J0b1BhZ2F0bz4xMC4wMDwvcGF5X2k6c2luZ29sb0ltcG9ydG9QYWdhdG8+DQogICAgPHBheV9pOmNvZGljZUVzaXRvU2luZ29sb1BhZ2FtZW50bz4wPC9wYXlfaTpjb2RpY2VFc2l0b1NpbmdvbG9QYWdhbWVudG8+DQogICAgPHBheV9pOmRhdGFFc2l0b1NpbmdvbG9QYWdhbWVudG8+MjAxNy0wOS0wMTwvcGF5X2k6ZGF0YUVzaXRvU2luZ29sb1BhZ2FtZW50bz4NCiAgPC9wYXlfaTpkYXRpU2luZ29saVBhZ2FtZW50aT4NCjwvcGF5X2k6Rmx1c3NvUml2ZXJzYW1lbnRvPg0K</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is KO of nodoInviaFlussoRendicontazione response
        And check faultCode is PPT_SEMANTICA of nodoInviaFlussoRendicontazione response
