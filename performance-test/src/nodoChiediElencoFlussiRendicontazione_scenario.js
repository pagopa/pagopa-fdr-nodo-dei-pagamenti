import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { parseHTML } from 'k6/html';
import http from 'k6/http';
import {
  generateNodoChiediElencoFlussiRendicontazione
} from './helpers/fdr_helpers.js';

export const options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
// note: SharedArray can currently only be constructed inside init code
// according to https://k6.io/docs/javascript-api/k6-data/sharedarray
const varsArray = new SharedArray('vars', function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
const vars = varsArray[0];
const subkey = `${__ENV.API_SUBSCRIPTION_KEY}`;

const parameters = {
  pspId: `${vars.psp}`,
  password: `${vars.password}`,
  creditorInstitutionId: `${vars.creditor_institution}`,
  brokerCiId: `${vars.broker_ci}`,
  stationId: `${vars.station}`,
  url_nodo_ci: `${vars.app_host_nodo_ci}`,
}

export function setup() {
  // Before All
  // setup code (once)
  // The setup code runs, setting up the test environment (optional) and generating data
  // used to reuse code for the same VU
}

export default function () {
  // Initialize parameter constants
  const params = {
    headers: {
      'Content-Type': 'text/xml',
      'SOAPAction': 'nodoChiediElencoFlussiRendicontazione',
      "Ocp-Apim-Subscription-Key": subkey,
    },
    tags: {
      primitiva: "nodoChiediElencoFlussiRendicontazione",
    }
  };

  // Testing: nodoChiediElencoFlussiRendicontazione
  const request_ncefr = generateNodoChiediElencoFlussiRendicontazione(parameters);
  const response = http.post(parameters.url_nodo_ci, request_ncefr, params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'totRestituiti field is not zero': (r) => parseHTML(r.body).find('totRestituiti').text() !== '0',
  });
  // console.log(`nodoChiediElencoFlussiRendicontazione request: ${request_ncefr} to [${parameters.url_nodo_ci}]\n`)
  // console.log(`nodoChiediElencoFlussiRendicontazione response: ${response} to [${parameters.url_nodo_ci}]\n`)
}

export function teardown(data) {
  // After All
  // teardown code
}