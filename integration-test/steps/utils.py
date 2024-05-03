import datetime
import os
import re
import time
from threading import Thread
from xml.dom.minidom import parseString

import requests
from behave.__main__ import main as behave_main
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry
from urllib3 import disable_warnings
from urllib3.exceptions import InsecureRequestWarning

disable_warnings(InsecureRequestWarning)

import xml.etree.ElementTree as ET


def random_s():
    import random
    cont = 5
    strNumRand = ''
    while cont != 0:
        strNumRand += str(random.randint(0, 9))
        cont -= 1
    return strNumRand


def compare_lists(lista_api, lista_query):
    set_api = set(lista_api)
    print(set_api)
    set_query = set(lista_query)
    print(set_query)

    return set_api == set_query


def current_milli_time():
    return round(time.time() * 1000)


def requests_retry_session(
        retries=3,
        backoff_factor=0.3,
        status_forcelist=(500, 502, 504),
        session=None,
):
    session = session or requests.Session()
    retry = Retry(
        total=retries,
        read=retries,
        connect=retries,
        backoff_factor=backoff_factor,
        status_forcelist=status_forcelist,
    )
    adapter = HTTPAdapter(max_retries=retry)
    session.mount('http://', adapter)
    session.mount('https://', adapter)
    return session


def get_soap_url_nodo(context, primitive=-1):
    primitive_mapping = {
        "nodoInviaFlussoRendicontazione": "/nodo-per-psp/v1",
        "nodoChiediElencoFlussiRendicontazione": "/nodo-per-pa/v1",
        "nodoChiediFlussoRendicontazione": "/nodo-per-pa/v1"
    }

    return context.config.userdata.get("services").get("nodo-dei-pagamenti").get("url") + primitive_mapping.get(primitive)


def save_soap_action(mock, primitive, soap_action, override=False):
    # set what your server accepts
    headers = {'Content-Type': 'application/xml'}
    print(f'{mock}/response/{primitive}?override={override}')
    response = requests.post(
        f"{mock}/response/{primitive}?override={override}", soap_action, headers=headers, verify=False)
    print(response.content, response.status_code)
    return response.status_code


def manipulate_soap_action(soap_action, elem, value):
    TYPE_ELEMENT = 1  # dom element
    # TYPE_VALUE = 3 # dom value
    my_document = parseString(soap_action)
    if value == "None":
        element = my_document.getElementsByTagName(elem)[0]
        element.parentNode.removeChild(element)
    elif value == "Empty":
        element = my_document.getElementsByTagName(elem)[0].childNodes[0]
        element.nodeValue = ''
        childs = my_document.getElementsByTagName(elem)[0].childNodes
        for child in childs:
            if child.nodeType == TYPE_ELEMENT:
                child.parentNode.removeChild(child)
    elif value == 'RemoveParent':
        element = my_document.getElementsByTagName(elem)[0]
        parent = element.parentNode
        children = element.childNodes
        parent.removeChild(element)
        for child in list(children):
            if child.nodeType == TYPE_ELEMENT:
                parent.appendChild(child)
    elif str(value).startswith("Occurrences"):
        occurrences = int(value.split(",")[1])
        original_node = my_document.getElementsByTagName(elem)[0]
        cloned_node = original_node.cloneNode(2)
        for i in range(0, occurrences - 1):
            original_node.parentNode.insertBefore(cloned_node, original_node)
            original_node = cloned_node
            cloned_node = original_node.cloneNode(2)
    else:
        node = my_document.getElementsByTagName(
            elem)[0] if my_document.getElementsByTagName(elem) else None

        if node is None:
            # create
            element = my_document.createTextNode(value)
            my_document.getElementsByTagName(elem)[0].appendChild(element)
        elif len(node.childNodes) > 1:
            # replace object
            object = parseString(value)
            node.parentNode.replaceChild(object.childNodes[0], node)
        else:
            # leaf -> single value
            while node.hasChildNodes():
                node.removeChild(node.firstChild)
            element = my_document.createTextNode(value)
            node.appendChild(element)

    return my_document.toxml()


def replace_context_variables(body, context):
    pattern = re.compile('\\$\\w+')
    match = pattern.findall(body)
    for field in match:
        saved_elem = getattr(context, field.replace('$', ''))
        value = str(saved_elem)
        body = body.replace(field, value)
    return body


def replace_local_variables(body, context):
    pattern = re.compile('\\$\\w+\\.\\w+')
    match = pattern.findall(body)
    for field in match:
        saved_elem = getattr(context, field.replace('$', '').split('.')[0])
        value = saved_elem
        if len(field.replace('$', '').split('.')) > 1:
            tag = field.replace('$', '').split('.')[1]
            if isinstance(saved_elem, str):
                document = parseString(saved_elem)
            else:
                document = parseString(saved_elem.content)
                print(tag)
            value = document.getElementsByTagNameNS(
                '*', tag)[0].firstChild.data
        body = body.replace(field, value)
    return body


def replace_global_variables(payload, context):
    pattern = re.compile('#\\w+#')
    match = pattern.findall(payload)
    for elem in match:
        replaced_sharp = elem.replace("#", "")
        if replaced_sharp in context.config.userdata.get("global_configuration"):
            payload = payload.replace(elem, context.config.userdata.get(
                "global_configuration").get(replaced_sharp))
    return payload


def get_ndp_subscription_key(context):
    return context.config.userdata.get("services").get("nodo-dei-pagamenti").get("subscription_key")


def json2xml(json_obj, line_padding=""):
    result_list = list()
    json_obj_type = type(json_obj)
    if json_obj_type is list:
        for sub_elem in json_obj:
            result_list.append(json2xml(sub_elem, line_padding))
        return "\n".join(result_list)
    if json_obj_type is dict:
        for tag_name in json_obj:
            sub_obj = json_obj[tag_name]
            if type(sub_obj) is dict:
                result_list.append("%s<%s>" % (line_padding, tag_name))
                for key in sub_obj:
                    sub_sub_obj = sub_obj[key]
                    result_list.append("%s<%s>" % (line_padding, key))
                    result_list.append(json2xml(sub_sub_obj, "\t" + line_padding))
                    result_list.append("%s</%s>" % (line_padding, key))
                result_list.append("%s</%s>" % (line_padding, tag_name))
            elif type(sub_obj) is list:
                result_list.append("%s<%s>" % (line_padding, tag_name))
                if tag_name == 'paymentTokens':
                    for sub_elem in sub_obj:
                        result_list.append("%s<%s>" % (line_padding, "paymentToken"))
                        result_list.append(json2xml(sub_elem, line_padding))
                        result_list.append("%s</%s>" % (line_padding, "paymentToken"))
                if tag_name == 'positionslist':
                    for sub_elem in sub_obj:
                        result_list.append("%s<%s>" % (line_padding, "position"))
                        result_list.append(json2xml(sub_elem, line_padding))
                        result_list.append("%s</%s>" % (line_padding, "position"))
                if tag_name == 'payments':
                    for sub_elem in sub_obj:
                        result_list.append("%s<%s>" % (line_padding, "payment"))
                        result_list.append(json2xml(sub_elem, line_padding))
                        result_list.append("%s</%s>" % (line_padding, "payment"))
                result_list.append("%s</%s>" % (line_padding, tag_name))
            else:
                result_list.append("%s<%s>" % (line_padding, tag_name))
                result_list.append(json2xml(sub_obj, "\t" + line_padding))
                result_list.append("%s</%s>" % (line_padding, tag_name))
        return "\n".join(result_list)
    return "%s%s" % (line_padding, json_obj)


def parallel_executor(context, feature_name, scenario):
    # os.chdir(testenv.PARALLEACTIONS_PATH)
    behave_main(
        '-i {} -n {} --tags=@test --no-skipped --no-capture'.format(feature_name, scenario))


def searchValueTag(xml_string, path_tag, flag_all_value_tag):
    list_tag = path_tag.split(".")
    size_list = len(list_tag)

    tag_padre = list_tag[0]
    tag = list_tag[size_list - 1]

    tree = ET.ElementTree(ET.fromstring(xml_string))
    root = tree.getroot()
    list_value_tag = []
    full_list_tag = []
    for single_tag in root.findall('.//' + tag_padre):
        list_value_tag = searchValueTagRecursive(tag_padre, tag, single_tag)
        full_list_tag.append(list_value_tag)
        if flag_all_value_tag == False:
            if list_value_tag: break
    return full_list_tag


def searchValueTagRecursive(tag_padre, tag, single_tag):
    list_tag = []

    if tag_padre == tag:
        list_tag = single_tag.text
    else:
        for next_tag in single_tag:
            list_tag = searchValueTagRecursive(next_tag.tag, tag, next_tag)
            if list_tag: break
    return list_tag
