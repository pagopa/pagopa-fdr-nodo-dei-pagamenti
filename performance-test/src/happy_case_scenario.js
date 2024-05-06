import http from 'k6/http';
import {check} from 'k6';
import {SharedArray} from 'k6/data';
import encoding from 'k6/encoding';
import { parseHTML } from 'k6/html';

export let options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
// note: SharedArray can currently only be constructed inside init code
// according to https://k6.io/docs/javascript-api/k6-data/sharedarray
const varsArray = new SharedArray('vars', function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const app_host = `${vars.app_host}`;
const subkey = `${__ENV.API_SUBSCRIPTION_KEY}`;

const pspId = `${vars.psp}`
const brokerPspId = `${vars.broker_psp}`;
const channelId = `${vars.channel}`;
const password = `${vars.password}`;
const creditorInstitutionId = `${vars.creditor_institution}`;
const brokerCiId = `${vars.broker_ci}`;
const stationId = `${vars.station}`;
const today = new Date().toISOString().slice(0, 10);
const url_nodo_psp = `${vars.app_host_nodo_psp}`;
const url_nodo_ci = `${vars.app_host_nodo_ci}`;


export function setup() {
  // Before All
  // setup code (once)
  // The setup code runs, setting up the test environment (optional) and generating data
  // used to reuse code for the same VU
}

function precondition() {
  // no pre conditions
}

function postcondition() {
  // no post conditions
}

export default function () {

  // Initialize response variable
  let response = '';
  var flow_id = `${today}${pspId}-${getRandom(1000000, 9999999) + __VU}`;

  // Initialize parameter constants
  const params = {
    headers: {
      'Content-Type': 'text/xml',
      'SOAPAction': 'nodoInviaFlussoRendicontazione',
      "Ocp-Apim-Subscription-Key": subkey,
    },
  };

  // starting the execution
  precondition();

  // Testing: nodoInviaFlussoRendicontazione
  response = http.post(url_nodo_psp, generateNodoInviaFlussoRendicontazione(flow_id), params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'esito field is OK': (r) => parseHTML(response.body).find('esito').text() === 'OK',
  });
  // console.log(`nodoInviaFlussoRendicontazione response: ${response.body} to [${url_nodo_psp}]\n`)

  // Testing: nodoChiediElencoFlussiRendicontazione
  params.headers['SOAPAction'] = 'nodoChiediElencoFlussiRendicontazione';
  response = http.post(url_nodo_ci, generateNodoChiediElencoFlussiRendicontazione(), params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'totRestituiti field is not zero': (r) => parseHTML(response.body).find('totRestituiti').text() !== '0',
  });
  // console.log(`nodoChiediElencoFlussiRendicontazione response: ${response.body} to [${url_nodo_ci}]\n`)

  // Testing: nodoChiediFlussoRendicontazione
  params.headers['SOAPAction'] = 'nodoChiediFlussoRendicontazione';
  var payload = generateNodoChiediFlussoRendicontazione(flow_id);
  response = http.post(url_nodo_ci, payload, params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'xmlRendicontazione field is defined': (r) => parseHTML(response.body).find('xmlRendicontazione').text() !== "",
  });
  // console.log(`nodoChiediFlussoRendicontazione response: ${response.body} to [${url_nodo_ci}]\n`);

  // ending the execution
  postcondition();

}

export function teardown(data) {
  // After All
  // teardown code
}


function generateNodoInviaFlussoRendicontazione(flow_id) {
    var now = new Date().toISOString().slice(0, 23);
    var amount1 = getRandom(10, 1000);
    var amount2 = getRandom(10, 1000);
    var amount3 = getRandom(10, 1000);
    var amount4 = getRandom(10, 1000);
    var amount5 = getRandom(10, 1000);
    var total = amount1 + amount2 + amount3 + amount4 + amount5;

    var report = `<pay_i:FlussoRiversamento xmlns:pay_i="http://www.digitpa.gov.it/schemas/2011/Pagamenti/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.digitpa.gov.it/schemas/2011/Pagamenti/ FlussoRendicontazione_v_1_0_1.xsd ">
        <pay_i:versioneOggetto>1.0</pay_i:versioneOggetto>
        <pay_i:identificativoFlusso>${flow_id}</pay_i:identificativoFlusso>
        <pay_i:dataOraFlusso>${now}</pay_i:dataOraFlusso>
        <pay_i:identificativoUnivocoRegolamento>IUREG</pay_i:identificativoUnivocoRegolamento>
        <pay_i:dataRegolamento>${today}</pay_i:dataRegolamento>
        <pay_i:istitutoMittente>
            <pay_i:identificativoUnivocoMittente>
                <pay_i:tipoIdentificativoUnivoco>G</pay_i:tipoIdentificativoUnivoco>
                <pay_i:codiceIdentificativoUnivoco>${pspId}</pay_i:codiceIdentificativoUnivoco>
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
        <pay_i:numeroTotalePagamenti>5</pay_i:numeroTotalePagamenti>
        <pay_i:importoTotalePagamenti>${total}.00</pay_i:importoTotalePagamenti>
        <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV0000000001</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR0000000001</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount1}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>
        <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV0000000002</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR0000000002</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount2}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>
        <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV0000000003</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR0000000003</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount3}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>
        <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV0000000004</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR0000000004</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount4}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>
        <pay_i:datiSingoliPagamenti>
            <pay_i:identificativoUnivocoVersamento>IUV0000000005</pay_i:identificativoUnivocoVersamento>
            <pay_i:identificativoUnivocoRiscossione>IUR0000000005</pay_i:identificativoUnivocoRiscossione>
            <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
            <pay_i:singoloImportoPagato>${amount5}.00</pay_i:singoloImportoPagato>
            <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
            <pay_i:dataEsitoSingoloPagamento>${today}</pay_i:dataEsitoSingoloPagamento>
        </pay_i:datiSingoliPagamenti>
    </pay_i:FlussoRiversamento>`;

    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoInviaFlussoRendicontazione>
                <identificativoPSP>${pspId}</identificativoPSP>
                <identificativoIntermediarioPSP>${brokerPspId}</identificativoIntermediarioPSP>
                <identificativoCanale>${channelId}</identificativoCanale>
                <password>${password}</password>
                <identificativoDominio>${creditorInstitutionId}</identificativoDominio>
                <identificativoFlusso>${flow_id}</identificativoFlusso>
                <dataOraFlusso>${now}</dataOraFlusso>
                <xmlRendicontazione>${encoding.b64encode(report)}</xmlRendicontazione>
            </ws:nodoInviaFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

function generateNodoChiediElencoFlussiRendicontazione() {
    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoChiediElencoFlussiRendicontazione>
                <identificativoIntermediarioPA>${brokerCiId}</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>${stationId}</identificativoStazioneIntermediarioPA>
                <password>${password}</password>
                <identificativoDominio>${creditorInstitutionId}</identificativoDominio>
                <identificativoPSP>${pspId}</identificativoPSP>
            </ws:nodoChiediElencoFlussiRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

function generateNodoChiediFlussoRendicontazione(flow_id) {
    return `<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ws="http://ws.pagamenti.telematici.gov/">
        <soapenv:Header/>
        <soapenv:Body>
            <ws:nodoChiediFlussoRendicontazione>
                <identificativoIntermediarioPA>${brokerCiId}</identificativoIntermediarioPA>
                <identificativoStazioneIntermediarioPA>${stationId}</identificativoStazioneIntermediarioPA>
                <password>${password}</password>
                <identificativoDominio>${creditorInstitutionId}</identificativoDominio>
                <identificativoPSP>${pspId}</identificativoPSP>
                <identificativoFlusso>${flow_id}</identificativoFlusso>
            </ws:nodoChiediFlussoRendicontazione>
        </soapenv:Body>
    </soapenv:Envelope>`;
}

function getRandom(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}