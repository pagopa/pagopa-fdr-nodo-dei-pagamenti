Feature: NIFRFTP

    Background:
        Given systems up

    Scenario: REND generation
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
                    <pay_i:tipoIdentificativoUnivoco>A</pay_i:tipoIdentificativoUnivoco>
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

    Scenario: Execute nodoInviaFlussoRendicontazione request
        Given the REND generation scenario executed successfully
        And initial XML nodoInviaFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
                <ws:nodoInviaFlussoRendicontazione>
                    <identificativoPSP>#psp#</identificativoPSP>
                    <identificativoIntermediarioPSP>#psp#</identificativoIntermediarioPSP>
                    <identificativoCanale>#canale#</identificativoCanale>
                    <password>pwdpwdpwd</password>
                    <identificativoDominio>44444444445</identificativoDominio>
                    <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
                    <dataOraFlusso>$timedate</dataOraFlusso>
                    <xmlRendicontazione>$rendAttachment</xmlRendicontazione>
                </ws:nodoInviaFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoInviaFlussoRendicontazione to nodo-dei-pagamenti
        Then check esito is OK of nodoInviaFlussoRendicontazione response

        And replace pa content with 44444444445 content

        # Rendicontazione
        And checks the value 0 of the record at column OPTLOCK of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value #psp# of the record at column PSP of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value #psp# of the record at column INTERMEDIARIO of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value #canale# of the record at column CANALE of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value None of the record at column PASSWORD of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value $pa of the record at column DOMINIO of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value NotNone of the record at column DATA_ORA_FLUSSO of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value None of the record at column FK_SFTP_FILE of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        
        And execution query rendicontazione to get value on the table RENDICONTAZIONE, with the columns FK_BINARY_FILE under macro RendicontazioneFTPeBollo with db name nodo_offline
        And through the query rendicontazione retrieve param fkBinaryFile at position 0 and save it under the key fkBinaryFile
        And checks the value $fkBinaryFile of the record at column ID of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo
        
        And checks the value VALID of the record at column STATO of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value NotNone of the record at column INSERTED_TIMESTAMP of the table RENDICONTAZIONE retrived by the query rendicontazione on db nodo_offline under macro RendicontazioneFTPeBollo

        # BINARY_FILE
        And checks the value NotNone of the record at column FILE_SIZE of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value NotNone of the record at column FILE_CONTENT of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value None of the record at column FILE_HASH of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value None of the record at column SIGNATURE_TYPE of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo
        And checks the value NotNone of the record at column XML_FILE_CONTENT of the table BINARY_FILE retrived by the query binary_file on db nodo_offline under macro RendicontazioneFTPeBollo


        # RENDICONTAZIONE_SFTP_SEND_QUEU
        And verify 0 record for the table RENDICONTAZIONE_SFTP_SEND_QUEUE retrived by the query send_queue on db nodo_offline under macro RendicontazioneFTPeBollo 


@runnable
    Scenario: Execute nodoChiediFlussoRendicontazione primitive
        Given the Execute nodoInviaFlussoRendicontazione request scenario executed successfully
        And initial XML nodoChiediFlussoRendicontazione
            """
            <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
            <soapenv:Header/>
            <soapenv:Body>
                <ws:nodoChiediFlussoRendicontazione>
                    <identificativoIntermediarioPA>44444444444</identificativoIntermediarioPA>
                    <identificativoStazioneIntermediarioPA>44444444444_01</identificativoStazioneIntermediarioPA>
                    <password>pwdpwdpwd</password>
                    <identificativoDominio>$pa</identificativoDominio>
                    <identificativoPSP>#psp#</identificativoPSP>
                    <identificativoFlusso>$identificativoFlusso</identificativoFlusso>
                </ws:nodoChiediFlussoRendicontazione>
            </soapenv:Body>
            </soapenv:Envelope>
            """
        When EC sends SOAP nodoChiediFlussoRendicontazione to nodo-dei-pagamenti
        Then check xmlRendicontazione contains $rendAttachment of nodoChiediFlussoRendicontazione response

