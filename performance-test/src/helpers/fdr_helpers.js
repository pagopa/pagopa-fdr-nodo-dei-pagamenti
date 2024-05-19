import encoding from 'k6/encoding';

export function generateNodoInviaFlussoRendicontazione(parameters, flow_id, flow_size) {
    var now = new Date().toISOString().slice(0, 23);
    var total = 0;

    var payments = ``;
    for (let i = 1; i <= flow_size; i ++) {
        var amount = getRandom(10, 1000);
        total += amount;
        var suffix = ('0000000000' + i).slice(-10);
        payments = payments.concat(`<pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV${suffix}</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR${suffix}</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${parameters.today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>\n`)
    }

    var report = `<pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
        <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
        <pay_i:identificativoFlusso>${flow_id}</pay_i:identificativoFlusso>
        <pay_i:dataOraFlusso>${now}</pay_i:dataOraFlusso>
        <pay_i:identificativoUnivocoRegolamento>IUREG</pay_i:identificativoUnivocoRegolamento>
        <pay_i:dataRegolamento>${parameters.today}</pay_i:dataRegolamento>
        <pay_i:istitutoMittente>
            <pay_i:identificativoUnivocoMittente>
                <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
                <pay_i:codiceIdentificativoUnivoco>${parameters.pspId}</pay_i:codiceIdentificativoUnivoco>
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
        <pay_i:numeroTotalePagamenti>${flow_size}</pay_i:numeroTotalePagamenti>
        <pay_i:importoTotalePagamenti>${total}.00</pay_i:importoTotalePagamenti>
        ${payments}
    </pay_i:FlussoRiversamento>`;

    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
                <identificativoPSP>${parameters.pspId}</identificativoPSP>
                <identificativoIntermediarioPSP>${parameters.brokerPspId}</identificativoIntermediarioPSP>
                <identificativoCanale>${parameters.channelId}</identificativoCanale>
                <password>${parameters.password}</password>
                <identificativoDominio>${parameters.creditorInstitutionId}</identificativoDominio>
                <identificativoFlusso>${flow_id}</identificativoFlusso>
                <dataOraFlusso>${now}</dataOraFlusso>
                <xmlRendicontazione>${encoding.b64encode(report)}</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

export function generateNodoChiediElencoFlussiRendicontazione(parameters) {
    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
                <identificativoIntermediarioPA>${parameters.brokerCiId}</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>${parameters.stationId}</identificativoStazioneIntermediarioPA>
                <password>${parameters.password}</password>
                <identificativoDominio>${parameters.creditorInstitutionId}</identificativoDominio>
                <identificativoPSP>${parameters.pspId}</identificativoPSP>
            </ws:nodoChiediElencoFlussiRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

export function generateNodoChiediFlussoRendicontazione(parameters, flow_id) {
    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
                <identificativoIntermediarioPA>${parameters.brokerCiId}</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>${parameters.stationId}</identificativoStazioneIntermediarioPA>
                <password>${parameters.password}</password>
                <identificativoDominio>${parameters.creditorInstitutionId}</identificativoDominio>
                <identificativoPSP>${parameters.pspId}</identificativoPSP>
                <identificativoFlusso>${flow_id}</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

export function getRandom(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}