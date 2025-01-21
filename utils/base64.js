function makeid(length) {
    var result           = '';
    var characters       = '0123456789';
    var charactersLength = characters.length;
    for ( var i = 0; i < length; i++ ) {
      result += characters.charAt(Math.floor(Math.random() * 
 charactersLength));
   }
   return result;
}

function howManyDatiSingoliPagamenti(n, data) {
    payments = "";
    for (let i = 0; i < n; i++) {
        singlePayment=`<datiSingoliPagamenti>
            <identificativoUnivocoVersamento>${makeid(17)}</identificativoUnivocoVersamento>
            <identificativoUnivocoRiscossione>IUR${makeid(17)}</identificativoUnivocoRiscossione>
            <indiceDatiSingoloPagamento>${1}</indiceDatiSingoloPagamento>
            <singoloImportoPagato>100.00</singoloImportoPagato>
            <codiceEsitoSingoloPagamento>0</codiceEsitoSingoloPagamento>
            <dataEsitoSingoloPagamento>${data}</dataEsitoSingoloPagamento>
        </datiSingoliPagamenti>`;
        payments+=singlePayment+"\n"
    }
    return payments;
}

let yourDate = new Date();
dataRegolamento = yourDate.toISOString().split('T')[0]


Date.prototype.addDays = function(days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() + days);
    return date;
}
var dataOraFlusso = new Date().addDays(0);
//console.log(dataOraFlusso);

istitutoMittente="AGID_01"

identificativoFlusso = `${dataRegolamento}${istitutoMittente}-S${makeid(9)}`;

const NUM_PAYMENTS = process.env.NUM_PAYMENTS || 100; // default 100 payments

xmlFlusso = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <FlussoRiversamento xmlns="http://www.digitpa.gov.it/schemas/2011/Pagamenti/">
                    <versioneOggetto>1.0</versioneOggetto>
                    <identificativoFlusso>${identificativoFlusso}</identificativoFlusso>
                    <dataOraFlusso>${dataOraFlusso.toISOString().split('.')[0]}</dataOraFlusso>
                    <identificativoUnivocoRegolamento>Bonifico SEPA-${makeid(5)}-77777777777</identificativoUnivocoRegolamento>
                    <dataRegolamento>${dataRegolamento}</dataRegolamento>
                    <istitutoMittente>
                        <identificativoUnivocoMittente>
                            <tipoIdentificativoUnivoco>B</tipoIdentificativoUnivoco>
                            <codiceIdentificativoUnivoco>${istitutoMittente}</codiceIdentificativoUnivoco>
                        </identificativoUnivocoMittente>
                        <denominazioneMittente>AGID</denominazioneMittente>
                    </istitutoMittente>
                    <istitutoRicevente>
                        <identificativoUnivocoRicevente>
                            <tipoIdentificativoUnivoco>G</tipoIdentificativoUnivoco>
                            <codiceIdentificativoUnivoco>77777777777</codiceIdentificativoUnivoco>
                        </identificativoUnivocoRicevente>
                        <denominazioneRicevente>AGSM ENERGIA S.R.L. SOCIETA' UNIPERSONAL E</denominazioneRicevente>
                    </istitutoRicevente>
                    <numeroTotalePagamenti>${NUM_PAYMENTS}</numeroTotalePagamenti>
                    <importoTotalePagamenti>${NUM_PAYMENTS*100}.00</importoTotalePagamenti>
                    ${howManyDatiSingoliPagamenti(NUM_PAYMENTS,dataRegolamento)}
                </FlussoRiversamento>
        `

//console.log(xmlFlusso);
xmlFlusso=Buffer.from(xmlFlusso).toString('base64');

nodoInviaFlussoRendicontazione=`
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body>
<ns5:nodoInviaFlussoRendicontazione xmlns:ns2="http://www.digitpa.gov.it/schemas/2011/Pagamenti/"
    xmlns:ns3="http://PuntoAccessoPSP.spcoop.gov.it/BarCode_GS1_128_Modified"
    xmlns:ns4="http://PuntoAccessoPSP.spcoop.gov.it/QrCode"
    xmlns:ns5="http://ws.pagamenti.telematici.gov/">
    <identificativoPSP>AGID_01</identificativoPSP>
    <identificativoIntermediarioPSP>97735020584</identificativoIntermediarioPSP>
    <identificativoCanale>97735020584_03</identificativoCanale>
    <password>pwd_AgID</password>
    <identificativoDominio>77777777777</identificativoDominio>
    <identificativoFlusso>${identificativoFlusso}</identificativoFlusso>
    <dataOraFlusso>${dataOraFlusso.toISOString().split('.')[0]}</dataOraFlusso>
    <xmlRendicontazione>${xmlFlusso}</xmlRendicontazione>
</ns5:nodoInviaFlussoRendicontazione>
</soap:Body>
</soap:Envelope>
`
//console.log(nodoInviaFlussoRendicontazione);
let nomeFile=`${NUM_PAYMENTS}-${identificativoFlusso}.xml`
require("fs").writeFileSync(nomeFile, nodoInviaFlussoRendicontazione);
console.log(nomeFile)
// only for local debug use
//let nomeFileOnlyFlow=`${NUM_PAYMENTS}identificativoPSP--identificativoIntermediarioPSP--identificativoCanale--identificativoDominio--${identificativoFlusso}--dataOraFlusso.xml`
//require("fs").writeFileSync(nomeFileOnlyFlow, xmlFlusso);

