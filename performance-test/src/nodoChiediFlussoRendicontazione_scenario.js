import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { parseHTML } from 'k6/html';
import http from 'k6/http';
import {
  generateNodoChiediFlussoRendicontazione
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
    flow_id: `${vars.flow_id}`,
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
      'SOAPAction': 'nodoChiediFlussoRendicontazione',
      "Ocp-Apim-Subscription-Key": subkey,
    },
  };

  // Testing: nodoChiediFlussoRendicontazione
  const request_ncfr = generateNodoChiediFlussoRendicontazione(parameters, parameters.flow_id);
  const response = http.post(parameters.url_nodo_ci, request_ncfr, params)
  check(response, {
    'check status is 200': (resp) => resp.status === 200,
    'xmlRendicontazione field is defined': (r) => parseHTML(r.body).find('xmlRendicontazione').text() !== "",
  });
  // console.log(`nodoChiediFlussoRendicontazione request: ${request_ncfr} to [${parameters.url_nodo_ci}]\n`);
  // console.log(`nodoChiediFlussoRendicontazione response: ${response.body} to [${parameters.url_nodo_ci}]\n`);
}

export function teardown(data) {
  // After All
  // teardown code
}