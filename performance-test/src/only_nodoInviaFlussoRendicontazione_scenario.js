import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { parseHTML } from 'k6/html';
import { generateNodoInviaFlussoRendicontazione, getRandom } from './helpers/fdr_helpers.js';

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
const flow_size = Number(`${__ENV.FLOW_SIZE}`);


const parameters = {
    pspId: `${vars.psp}`,
    brokerPspId:  `${vars.broker_psp}`,
    channelId: `${vars.channel}`,
    password: `${vars.password}`,
    creditorInstitutionId: `${vars.creditor_institution}`,
    brokerCiId: `${vars.broker_ci}`,
    stationId: `${vars.station}`,
    today: new Date().toISOString().slice(0, 10),
    url_nodo_psp: `${vars.app_host_nodo_psp}`,
    url_nodo_ci: `${vars.app_host_nodo_ci}`,
}


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
  var flow_id = `${parameters.today}${parameters.pspId}-${getRandom(10000000000, 99999999999) + __VU}`;

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
  var request_nifr = generateNodoInviaFlussoRendicontazione(parameters, flow_id, flow_size);
  response = http.post(parameters.url_nodo_psp, request_nifr, params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'esito field is OK': (r) => parseHTML(response.body).find('esito').text() === 'OK',
  });
  //console.log(`nodoInviaFlussoRendicontazione request: ${request_nifr} to [${parameters.url_nodo_psp}]\n`)
  //console.log(`nodoInviaFlussoRendicontazione response: ${response.body} to [${parameters.url_nodo_psp}]\n`)

  // ending the execution
  postcondition();

}

export function teardown(data) {
  // After All
  // teardown code
}


