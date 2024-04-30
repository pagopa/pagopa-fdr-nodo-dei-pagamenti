import base64 as b64
import datetime
import os
import random
from xml.dom.minidom import parseString

import requests
from behave import *

import utils as utils

# Constants
RESPONSE = "Response"
REQUEST = "Request"


# Steps definitions
@given('systems up')
def step_impl(context):
    """
        health check for
            - nodo-dei-pagamenti ( application under test )
            - mock-ec ( used by nodo-dei-pagamenti to forwarding EC's requests )
            - pagopa-api-config ( used in tests to set DB's nodo-dei-pagamenti correctly according to input test ))
    """
    responses = True

    if "systems up" not in context.precondition_cache:

        for row in context.table:
            print(f"calling: {row.get('name')} -> {row.get('url')}")
            url = row.get("url") + row.get("healthcheck")
            print(f"calling -> {url}")
            headers = {'Host': 'api.dev.platform.pagopa.it:443'}
            resp = requests.get(url, headers=headers, verify=False)
            print(f"response: {resp.status_code}")
            responses &= (resp.status_code == 200)

        if responses:
            context.precondition_cache.add("systems up")

    assert responses


@given('report generation')
def step_impl(context):
    payload = context.text or ""
    payload = utils.replace_local_variables(payload, context)

    if '#date#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        payload = payload.replace('#date#', date)
        setattr(context, 'date', date)

    if '#timedate+1#' in payload:
        date = datetime.date.today() + datetime.timedelta(hours=1)
        date = date.strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3]
        payload = payload.replace('#timedate+1#', timedate)
        setattr(context, 'futureTimedate', timedate)

    if "#timedate#" in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3]
        payload = payload.replace('#timedate#', timedate)
        setattr(context, 'timedate', timedate)

    if '#identificativoFlusso#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        identificativoFlusso = date + context.config.userdata.get(
            "global_configuration").get("psp") + "-" + str(random.randint(0, 10000))
        payload = payload.replace(
            '#identificativoFlusso#', identificativoFlusso)
        setattr(context, 'identificativoFlusso', identificativoFlusso)

    if '#iuv#' in payload:
        iuv = "IUV" + str(random.randint(0, 10000)) + "-" + \
              datetime.datetime.now().strftime("%Y-%m-%d-%H:%M:%S.%f")[:-3]
        payload = payload.replace('#iuv#', iuv)
        setattr(context, 'iuv', iuv)

    if '#iur#' in payload:
        iur = "IUR" + str(random.randint(0, 10000)) + "-" + \
              datetime.datetime.now().strftime("%Y-%m-%d-%H:%M:%S.%f")[:-3]
        payload = payload.replace('#iur#', iur)
        setattr(context, 'iur', iur)

    payload = utils.replace_context_variables(payload, context)
    payload = utils.replace_global_variables(payload, context)

    payload_b = bytes(payload, 'UTF-8')
    payload_uni = b64.b64encode(payload_b)
    payload = f"{payload_uni}".split("'")[1]
    print(payload)

    print("Generated report: ", payload)
    setattr(context, 'rendAttachment', payload)


@given('{elem} with {value} in {action}')
def step_impl(context, elem, value, action):
    # use - to skip
    if elem != "-":
        value = utils.replace_local_variables(value, context)
        value = utils.replace_context_variables(value, context)
        value = utils.replace_global_variables(value, context)
        xml = utils.manipulate_soap_action(
            getattr(context, action), elem, value)
        setattr(context, action, xml)


@step('initial XML {primitive}')
def step_impl(context, primitive):
    payload = context.text or ""
    payload = utils.replace_local_variables(payload, context)
    payload = utils.replace_context_variables(payload, context)
    payload = utils.replace_global_variables(payload, context)

    idPA = context.config.userdata.get("global_configuration").get("creditor_institution_code")

    if len(payload) > 0:
        my_document = parseString(payload)
        idBrokerPSP = context.config.userdata.get("global_configuration").get("broker_psp")
        if len(my_document.getElementsByTagName('idBrokerPSP')) > 0:
            idBrokerPSP = my_document.getElementsByTagName('idBrokerPSP')[0].firstChild.data

        payload = payload.replace('#idempotency_key#', f"{idBrokerPSP}_{str(random.randint(1000000000, 9999999999))}")

        payload = payload.replace('#idempotency_key_IOname#',
                                  "IOname" + "_" + str(random.randint(1000000000, 9999999999)))

    if "#timedate#" in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3]
        payload = payload.replace('#timedate#', timedate)
        setattr(context, 'timedate', timedate)

    if '#date#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        payload = payload.replace('#date#', date)
        setattr(context, 'date', date)

    if '#yesterday_date#' in payload:
        yesterday_date = (datetime.datetime.now() - datetime.timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]
        payload = payload.replace('#yesterday_date#', yesterday_date)
        setattr(context, 'yesterday_date', yesterday_date)

    if '#tomorrow_date#' in payload:
        tomorrow_date = (datetime.datetime.now() + datetime.timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]
        payload = payload.replace('#tomorrow_date#', tomorrow_date)
        setattr(context, 'tomorrow_date', tomorrow_date)

    if '#identificativoFlusso#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        identificativoFlusso = date + context.config.userdata.get("global_configuration").get("psp") + "-" + str(
            random.randint(0, 10000))
        payload = payload.replace('#identificativoFlusso#', identificativoFlusso)
        setattr(context, 'identificativoFlusso', identificativoFlusso)

    if '#iubd#' in payload:
        iubd = '' + str(random.randint(10000000, 20000000)) + str(random.randint(10000000, 20000000))
        payload = payload.replace('#iubd#', iubd)
        setattr(context, 'iubd', iubd)

    if "#ccp#" in payload:
        ccp = str(random.randint(100000000000000, 999999999999999))
        payload = payload.replace('#ccp#', ccp)
        setattr(context, "ccp", ccp)

    if "#ccpms#" in payload:
        ccpms = str(utils.current_milli_time())
        payload = payload.replace('#ccpms#', ccpms)
        setattr(context, "ccpms", ccpms)

    if "#ccpms2#" in payload:
        ccpms2 = str(utils.current_milli_time()) + '1'
        payload = payload.replace('#ccpms2#', ccpms2)
        setattr(context, "ccpms2", ccpms2)

    if "#iuv#" in payload:
        iuv = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iuv#', iuv)
        setattr(context, "iuv", iuv)

    if "#iuv1#" in payload:
        iuv1 = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iuv1#', iuv1)
        setattr(context, "iuv1", iuv1)

    if "#iur#" in payload:
        iur = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iur#', iur)
        setattr(context, "iur", iur)

    if "#iur1#" in payload:
        iur1 = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iur1#', iur1)
        setattr(context, "iur1", iur1)

    if "#random_iuv#" in payload:
        while "#random_iuv#" in payload:
            iuv = '11' + str(random.randint(1000000000000, 9999999999999))
            payload = payload.replace('#random_iuv#', iuv, 1)

    if "#random_iur#" in payload:
        while "#random_iur#" in payload:
            iur = str(random.randint(100000000000000, 999999999999999))
            payload = payload.replace('#random_iur#', iur, 1)

    if '#IUV#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        IUV = 'IUV' + str(random.randint(0, 10000)) + '-' + date + \
              datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]
        payload = payload.replace('#IUV#', IUV)
        setattr(context, 'IUV', IUV)

    if '#IUV2#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        IUV2 = str(date + datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3] + '-' + str(random.randint(0, 100000)))
        payload = payload.replace('#IUV2#', IUV2)
        setattr(context, 'IUV2', IUV2)

    if '#notice_number#' in payload:
        notice_number = f"30211{str(random.randint(1000000000000, 9999999999999))}"
        payload = payload.replace('#notice_number#', notice_number)
        setattr(context, "iuv", notice_number[1:])

    if '#notice_number_old#' in payload:
        notice_number = f"31211{str(random.randint(1000000000000, 9999999999999))}"
        payload = payload.replace('#notice_number_old#', notice_number)
        setattr(context, "iuv", notice_number[1:])

    if '#carrello#' in payload:
        carrello = idPA + "302" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + "-" + utils.random_s()
        payload = payload.replace('#carrello#', carrello)
        setattr(context, 'carrello', carrello)

    if '#carrello1#' in payload:
        carrello1 = idPA + "302" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + utils.random_s()
        payload = payload.replace('#carrello1#', carrello1)
        setattr(context, 'carrello1', carrello1)

    if '#secCarrello#' in payload:
        secCarrello = idPA + "301" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + "-" + utils.random_s()
        payload = payload.replace('#secCarrello#', secCarrello)
        setattr(context, 'secCarrello', secCarrello)

    if '#carrNOTENABLED#' in payload:
        carrNOTENABLED = "11111122223" + "311" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + "-" + utils.random_s()
        payload = payload.replace('#carrNOTENABLED#', carrNOTENABLED)
        setattr(context, 'carrNOTENABLED', carrNOTENABLED)

    if '#thrCarrello#' in payload:
        thrCarrello = idPA + "088" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + "-" + utils.random_s()
        payload = payload.replace('#thrCarrello#', thrCarrello)
        setattr(context, 'thrCarrello', thrCarrello)

    if '#CARRELLO#' in payload:
        CARRELLO = "CARRELLO" + "-" + \
                   str(getattr(context, 'date') +
                       datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3])
        payload = payload.replace('#CARRELLO#', CARRELLO)
        setattr(context, 'CARRELLO', CARRELLO)

    if '#CARRELLO1#' in payload:
        CARRELLO1 = "CARRELLO" + str(random.randint(0, 100000))
        payload = payload.replace('#CARRELLO1#', CARRELLO1)
        setattr(context, 'CARRELLO1', CARRELLO1)

    if '#CARRELLO2#' in payload:
        CARRELLO2 = "CARRELLO" + str(random.randint(0, 10000))
        payload = payload.replace('#CARRELLO2#', CARRELLO2)
        setattr(context, 'CARRELLO2', CARRELLO2)

    if '#carrelloMills#' in payload:
        carrello = str(utils.current_milli_time())
        payload = payload.replace('#carrelloMills#', carrello)
        setattr(context, 'carrelloMills', carrello)

    if '#ccp3#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("%H:%M:%S.%f")[:-3]
        ccp3 = str(random.randint(0, 10000)) + timedate
        payload = payload.replace('#ccp3#', ccp3)
        setattr(context, 'ccp3', ccp3)
    if '$iuv' in payload:
        payload = payload.replace('$iuv', getattr(context, 'iuv'))

    if '$iur' in payload:
        payload = payload.replace('$iur', getattr(context, 'iur'))

    if '$intermediarioPA' in payload:
        payload = payload.replace(
            '$intermediarioPA', getattr(context, 'intermediarioPA'))

    if '$identificativoFlusso' in payload:
        payload = payload.replace('$identificativoFlusso', getattr(
            context, 'identificativoFlusso'))

    if '$1ccp' in payload:
        payload = payload.replace('$1ccp', getattr(context, 'ccp1'))

    if '$2ccp' in payload:
        payload = payload.replace('$2ccp', getattr(context, 'ccp2'))

    if '$rendAttachment' in payload:
        rendAttachment = getattr(context, 'rendAttachment')
        rendAttachment_b = bytes(rendAttachment, 'UTF-8')
        rendAttachment_uni = b64.b64encode(rendAttachment_b)
        rendAttachment_uni = f"{rendAttachment_uni}".split("'")[1]
        payload = payload.replace('$rendAttachment', rendAttachment_uni)

    if '#carrello#' in payload:
        carrello = idPA + "311" + "0" + str(random.randint(1000, 2000)) + str(
            random.randint(1000, 2000)) + str(random.randint(1000, 2000)) + "00" + "-" + utils.random_s()
        payload = payload.replace('#carrello#', carrello)
        setattr(context, 'carrello', carrello)

    setattr(context, primitive, payload)


@step('the {name} scenario executed successfully')
def step_impl(context, name):
    phase = (
            [phase for phase in context.feature.scenarios if name in phase.name] or [None])[0]
    text_step = ''.join(
        [step.keyword + " " + step.name + "\n\"\"\"\n" + (step.text or '') + "\n\"\"\"\n" for step in phase.steps])
    context.execute_steps(text_step)


@step('{sender} sends soap {soap_primitive} to {receiver}')
def step_impl(context, sender, soap_primitive, receiver):
    headers = {'Content-Type': 'application/xml', 'SOAPAction': soap_primitive}
    if 'SUBSCRIPTION_KEY' in os.environ:
        headers['Ocp-Apim-Subscription-Key'] = os.getenv('SUBSCRIPTION_KEY')
    url_nodo = utils.get_soap_url_nodo(context, soap_primitive)
    print("url_nodo: ", url_nodo)
    print("nodo soap_request sent >>>", getattr(context, soap_primitive))
    print("headers: ", headers)
    soap_response = requests.post(url_nodo, getattr(context, soap_primitive), headers=headers, verify=False)
    print(soap_response.content.decode('utf-8'))
    print(soap_response.status_code)
    setattr(context, soap_primitive + RESPONSE, soap_response)

    assert (soap_response.status_code == 200), f"status_code {soap_response.status_code}"


@step('check if {tag} is not {value} in {primitive} response')
def step_impl(context, tag, value, primitive):
    soap_response = getattr(context, primitive + RESPONSE)
    value = utils.replace_local_variables(value, context)
    value = utils.replace_context_variables(value, context)
    value = utils.replace_global_variables(value, context)
    print('soap_response: ', soap_response.headers)
    my_document = parseString(soap_response.content)
    if len(my_document.getElementsByTagName('faultCode')) > 0:
        print("fault code: ", my_document.getElementsByTagName(
            'faultCode')[0].firstChild.data)
        print("fault string: ", my_document.getElementsByTagName(
            'faultString')[0].firstChild.data)
        print("fault description: ", my_document.getElementsByTagName(
            'description')[0].firstChild.data)
    data = my_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value != data


@step('check if {tag} field is {value} in base64 {base64_field} field of {primitive} response')
def step_impl(context, tag, value, base64_field, primitive):
    soap_response = getattr(context, primitive + RESPONSE)
    value = utils.replace_local_variables(value, context)
    value = utils.replace_context_variables(value, context)
    value = utils.replace_global_variables(value, context)
    print('soap_response: ', soap_response.headers)
    my_document = parseString(soap_response.content)
    if len(my_document.getElementsByTagName('faultCode')) > 0:
        print("fault code: ", my_document.getElementsByTagName(
            'faultCode')[0].firstChild.data)
        print("fault string: ", my_document.getElementsByTagName(
            'faultString')[0].firstChild.data)
        print("fault description: ", my_document.getElementsByTagName(
            'description')[0].firstChild.data)
    payload_with_base64 = my_document.getElementsByTagName(base64_field)[0].firstChild.data
    report = b64.b64decode(payload_with_base64)
    my_internal_document = parseString(report)
    data = my_internal_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value == data


@step('check {tag} is {value} of {primitive} response')
def step_impl(context, tag, value, primitive):
    soap_response = getattr(context, primitive + RESPONSE)
    value = utils.replace_local_variables(value, context)
    value = utils.replace_context_variables(value, context)
    value = utils.replace_global_variables(value, context)
    print('soap_response: ', soap_response.headers)
    my_document = parseString(soap_response.content)
    if len(my_document.getElementsByTagName('faultCode')) > 0:
        print("fault code: ", my_document.getElementsByTagName(
            'faultCode')[0].firstChild.data)
        print("fault string: ", my_document.getElementsByTagName(
            'faultString')[0].firstChild.data)
        print("fault description: ", my_document.getElementsByTagName(
            'description')[0].firstChild.data)
    data = my_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value == data


@step('check {tag} field exists in {primitive} response')
def step_impl(context, tag, primitive):
    soap_response = getattr(context, primitive + RESPONSE)
    my_document = parseString(soap_response.content)
    assert len(my_document.getElementsByTagName(tag)) > 0
