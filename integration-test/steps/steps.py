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


@given('the generated report')
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
            "global_configuration").get("psp") + "-" + str(random.randint(100000000, 999999999))
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


@step('an XML for FlussoRiversamento with lots of transfers')
def step_impl(context):
    payload = context.text or ""
    payload = utils.replace_local_variables(payload, context)
    payload = utils.replace_context_variables(payload, context)
    payload = utils.replace_global_variables(payload, context)

    if "[TRANSFERS]" in payload:
        number_of_transfers = 500000
        transfers = ("<pay_i:numeroTotalePagamenti>" + str(number_of_transfers) + "</pay_i:numeroTotalePagamenti>\n" +
                     "<pay_i:importoTotalePagamenti>" + str(
                    number_of_transfers * 5) + ".00</pay_i:importoTotalePagamenti>\n")
        for i in range(number_of_transfers + 1):
            transfers += """
                <pay_i:datiSingoliPagamenti>
                    <pay_i:identificativoUnivocoVersamento>1100000{iuv_suffix}</pay_i:identificativoUnivocoVersamento>
                    <pay_i:identificativoUnivocoRiscossione>8104910{iur_suffix}</pay_i:identificativoUnivocoRiscossione>
                    <pay_i:indiceDatiSingoloPagamento>1</pay_i:indiceDatiSingoloPagamento>
                    <pay_i:singoloImportoPagato>5.00</pay_i:singoloImportoPagato>
                    <pay_i:codiceEsitoSingoloPagamento>0</pay_i:codiceEsitoSingoloPagamento>
                    <pay_i:dataEsitoSingoloPagamento>#date#</pay_i:dataEsitoSingoloPagamento>
                </pay_i:datiSingoliPagamenti>
            """.format(iuv_suffix=str(i).zfill(6), iur_suffix=str(i).zfill(6))
        payload = payload.replace('[TRANSFERS]', transfers)

    if "#timedate#" in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3]
        payload = payload.replace('#timedate#', timedate)
        setattr(context, 'timedate', timedate)

    if '#date#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        payload = payload.replace('#date#', date)
        setattr(context, 'date', date)

    if '#identificativoFlusso#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        identificativoFlusso = date + context.config.userdata.get("global_configuration").get("psp") + "-" + str(
            random.randint(100000000, 999999999))
        payload = payload.replace('#identificativoFlusso#', identificativoFlusso)
        setattr(context, 'identificativoFlusso', identificativoFlusso)

    if "#iuv#" in payload:
        iuv = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iuv#', iuv)
        setattr(context, "iuv", iuv)

    if "#iur#" in payload:
        iur = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iur#', iur)
        setattr(context, "iur", iur)

    if "#random_iuv#" in payload:
        while "#random_iuv#" in payload:
            iuv = '11' + str(random.randint(1000000000000, 9999999999999))
            payload = payload.replace('#random_iuv#', iuv, 1)

    if "#random_iur#" in payload:
        while "#random_iur#" in payload:
            iur = str(random.randint(100000000000000, 999999999999999))
            payload = payload.replace('#random_iur#', iur, 1)

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

    if '$rendAttachment' in payload:
        rendAttachment = getattr(context, 'rendAttachment')
        rendAttachment_b = bytes(rendAttachment, 'UTF-8')
        rendAttachment_uni = b64.b64encode(rendAttachment_b)
        rendAttachment_uni = f"{rendAttachment_uni}".split("'")[1]
        payload = payload.replace('$rendAttachment', rendAttachment_uni)

    setattr(context, "FlussoRiversamento", payload)


@step('an XML for {primitive}')
def step_impl(context, primitive):
    payload = context.text or ""
    payload = utils.replace_local_variables(payload, context)
    payload = utils.replace_context_variables(payload, context)
    payload = utils.replace_global_variables(payload, context)

    if "#timedate#" in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        timedate = date + datetime.datetime.now().strftime("T%H:%M:%S.%f")[:-3]
        payload = payload.replace('#timedate#', timedate)
        setattr(context, 'timedate', timedate)

    if '#date#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        payload = payload.replace('#date#', date)
        setattr(context, 'date', date)

    if '#identificativoFlusso#' in payload:
        date = datetime.date.today().strftime("%Y-%m-%d")
        identificativoFlusso = date + context.config.userdata.get("global_configuration").get("psp") + "-" + str(
            random.randint(100000000, 999999999))
        payload = payload.replace('#identificativoFlusso#', identificativoFlusso)
        setattr(context, 'identificativoFlusso', identificativoFlusso)

    if "#iuv#" in payload:
        iuv = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iuv#', iuv)
        setattr(context, "iuv", iuv)

    if "#iur#" in payload:
        iur = '11' + str(random.randint(1000000000000, 9999999999999))
        payload = payload.replace('#iur#', iur)
        setattr(context, "iur", iur)

    if "#random_iuv#" in payload:
        while "#random_iuv#" in payload:
            iuv = '11' + str(random.randint(1000000000000, 9999999999999))
            payload = payload.replace('#random_iuv#', iuv, 1)

    if "#random_iur#" in payload:
        while "#random_iur#" in payload:
            iur = str(random.randint(100000000000000, 999999999999999))
            payload = payload.replace('#random_iur#', iur, 1)

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

    if '$rendAttachment' in payload:
        rendAttachment = getattr(context, 'rendAttachment')
        rendAttachment_b = bytes(rendAttachment, 'UTF-8')
        rendAttachment_uni = b64.b64encode(rendAttachment_b)
        rendAttachment_uni = f"{rendAttachment_uni}".split("'")[1]
        payload = payload.replace('$rendAttachment', rendAttachment_uni)

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
    headers = {'Content-Type': 'application/xml', 'SOAPAction': soap_primitive,
               'Ocp-Apim-Subscription-Key': utils.get_ndp_subscription_key(context)}
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
    assert len(my_document.getElementsByTagName(tag)) > 0, f"Cannot found tag [{tag}] from response: [{soap_response.content}]"
    data = my_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value != data, f"the passed value [{data}] is not different to required: [{value}]"


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
    assert len(my_internal_document.getElementsByTagName(tag)) > 0, f"Cannot found tag [{tag}] from response: [{report}]"
    data = my_internal_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value == data, f"the passed value [{data}] is not equals to required: [{value}]"


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
    assert len(my_document.getElementsByTagName(tag)) > 0, f"Cannot found tag [{tag}] from response: [{soap_response.content}]"
    data = my_document.getElementsByTagName(tag)[0].firstChild.data
    print(f'check tag "{tag}" - expected: {value}, obtained: {data}')
    assert value == data, f"the passed value [{data}] is not equals to required: [{value}]"


@step('check {tag} field exists in {primitive} response')
def step_impl(context, tag, primitive):
    soap_response = getattr(context, primitive + RESPONSE)
    my_document = parseString(soap_response.content)
    assert len(my_document.getElementsByTagName(tag)) > 0
