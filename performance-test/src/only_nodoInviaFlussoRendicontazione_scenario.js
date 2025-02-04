import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { parseHTML } from 'k6/html';
import exec from 'k6/execution';
import { generateNodoInviaFlussoRendicontazione, generateNodoInviaFlussoRendicontazioneFixedPayments, generateMultiplePaymentsObject, getRandom } from './helpers/fdr_helpers.js';

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
  // chunks must be: 4500 (6000) is the most common,
  var paymentsObject_3000 = generateMultiplePaymentsObject(parameters, 3000);
  var paymentsObject_4500 = generateMultiplePaymentsObject(parameters, 4500);
  var paymentsObject_6000 = generateMultiplePaymentsObject(parameters, 6000);
  var paymentsObject_7500 = generateMultiplePaymentsObject(parameters, 7500);
  var paymentsObject_15000 = generateMultiplePaymentsObject(parameters, 15000);
  var paymentsObject_30000 = generateMultiplePaymentsObject(parameters, 30000);
  var paymentsObject_37500 = generateMultiplePaymentsObject(parameters, 37500);
  var paymentsObject_45000 = generateMultiplePaymentsObject(parameters, 45000);
  var paymentsObject_60000 = generateMultiplePaymentsObject(parameters, 60000);
  var paymentsObject_120000 = generateMultiplePaymentsObject(parameters, 120000);
  var paymentsObject_150000 = generateMultiplePaymentsObject(parameters, 150000);
  const multiplePaymentsObject = [paymentsObject_3000, paymentsObject_4500, paymentsObject_6000, paymentsObject_7500, paymentsObject_15000, paymentsObject_30000, paymentsObject_37500, paymentsObject_45000, paymentsObject_60000, paymentsObject_120000, paymentsObject_150000];
  return multiplePaymentsObject;
}

function precondition() {
  // no pre conditions
}

function postcondition() {
  // no post conditions
}

export default function (multiplePaymentsObject) {

  var multiplePaymentsObjectToUse = ``;
  var flow_size = 0;
  if (exec.scenario.name === 'step_0' || exec.scenario.name === 'step_7' || exec.scenario.name === 'step_9') {
    // use 3000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[0];
    flow_size = 3000;
  } else if (exec.scenario.name === 'step_1' || exec.scenario.name === 'step_11') {
    // use 6000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[2];
    flow_size = 6000;
  } else if (exec.scenario.name === 'step_2') {
    // use 7500 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[3];
    flow_size = 7500;
  } else if (exec.scenario.name === 'step_3' || exec.scenario.name === 'step_13') {
    // use 4500 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[1];
    flow_size = 4500;
  } else if (exec.scenario.name === 'step_4') {
    // use 30000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[5];
    flow_size = 30000;
  } else if (exec.scenario.name === 'step_5') {
    // use 37500 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[6];
    flow_size = 37500;
  } else if (exec.scenario.name === 'step_6') {
    // use 45000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[7];
    flow_size = 45000;
  } else if (exec.scenario.name === 'step_8') {
    // use 15000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[4];
    flow_size = 15000;
  } else if (exec.scenario.name === 'step_10') {
    // use 120000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[9];
    flow_size = 120000;
  } else if (exec.scenario.name === 'step_12') {
    // use 60000 payments
    multiplePaymentsObjectToUse = multiplePaymentsObject[8];
    flow_size = 60000;
  }
  // Initialize response variable
  let response = '';
  var flow_id = `${parameters.today}${parameters.pspId}-${getRandom(1000000, 9999999) + __VU}`;

  // Initialize parameter constants
  const params = {
    headers: {
      'Content-Type': 'text/xml',
      'SOAPAction': 'nodoInviaFlussoRendicontazione',
      "Ocp-Apim-Subscription-Key": subkey,
    },
    tags: {
    primitiva: "nodoInviaFlussoRendicontazione",
    pagamenti: flow_size.toString()
    }
  };

  // starting the execution
  precondition();

  // Testing: nodoInviaFlussoRendicontazione
  var request_nifr = generateNodoInviaFlussoRendicontazioneFixedPayments(parameters, flow_id, flow_size, multiplePaymentsObjectToUse);
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


