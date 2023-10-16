import json
from behave.model import Table
import os
import steps.utils as utils

import steps.testing_support as testing_support
if 'NODOPGDB' in os.environ:
    import steps.db_operation_pg as db
    import psycopg2
    from psycopg2 import OperationalError
else:
    import steps.db_operation as db
    import os, cx_Oracle, requests


def before_all(context):
    # initialize precondition cache to avoid check systems up for each scenario
    context.precondition_cache = set()

    print('Global settings...')

    if 'NODOPGDB' not in os.environ:
        lib_dir = os.path.abspath(
            os.path.join(__file__, os.pardir, os.pardir, os.pardir, os.pardir, os.pardir, 'oracle',
                         'instantclient_21_6'))
        cx_Oracle.init_oracle_client(lib_dir=lib_dir)

    more_userdata = json.load(open(os.path.join(context.config.base_dir + "/../resources/config.json")))
    context.config.update_userdata(more_userdata)
    # services = context.config.userdata.get("services")
    # db_config = context.config.userdata.get("db_configuration")
    
    selected_query = utils.query_json(context, 'select_config', 'configurations')    
    exec_query = testing_support.executeQuery(context, selected_query)

    # convert list of tuple in a dict
    config_dict = {key: value for key, value in exec_query}

    setattr(context, 'configurations', config_dict)


def before_feature(context, feature):
    services = context.config.userdata.get("services")
    # add heading
    feature.background.steps[0].table = Table(headings=("name", "url", "healthcheck", "soap_service", "rest_service"))
    # add data in the tables
    for system_name in services.keys():
        row = (system_name,
               services.get(system_name).get("url", ""),
               services.get(system_name).get("healthcheck", ""),
               services.get(system_name).get("soap_service", ""),
               services.get(system_name).get("rest_service", ""))
        feature.background.steps[0].table.add_row(row)


def after_feature(context, feature):
    global_configuration = context.config.userdata.get("global_configuration")


def after_all(context):
    print("After all disabled")
    # config_dict = getattr(context, 'configurations')
    # for key, value in config_dict.items():
    #     #print(key, value)
    #     selected_query = utils.query_json(context, 'update_config', 'configurations').replace('value', value).replace('key', key)
    #     testing_support.executeQuery(context, selected_query)
    #
    # db.closeConnection(conn)
    # headers = {'Host': 'api.dev.platform.pagopa.it:443'}
    # requests.get(utils.get_refresh_config_url(context), verify=False, headers=headers)
