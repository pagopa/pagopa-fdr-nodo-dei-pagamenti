import json
import os

from behave.model import Table


def before_all(context):
    # initialize precondition cache to avoid check systems up for each scenario
    context.precondition_cache = set()

    print('Global settings...')

    more_userdata = json.load(open(os.path.join(context.config.base_dir + "/config/config.json")))
    context.config.update_userdata(more_userdata)


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
    print("After feature disabled")
    global_configuration = context.config.userdata.get("global_configuration")


def after_all(context):
    print("After all disabled")
