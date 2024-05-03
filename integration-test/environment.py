import json
import os
import logging

import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

from behave.model import Table


def before_all(context):
    # initialize precondition cache to avoid check systems up for each scenario
    context.precondition_cache = set()

    context.config.setup_logging()

    logging.debug('Global settings: loading configuration')

    config_file = "/config/dev.json"
    if 'CONFIG_FILE' in os.environ:
        config_file = os.getenv('CONFIG_FILE')

    more_userdata = json.load(open(os.path.join(context.config.base_dir + config_file)))
    for key, cfg in more_userdata.get("services").items():
        if cfg.get("subscription_key") is not None:
            cfg["subscription_key"] = os.getenv(cfg["subscription_key"])
    context.config.update_userdata(more_userdata)


def before_feature(context, feature):
    services = context.config.userdata.get("services")
    # add heading
    feature.background.steps[0].table = Table(headings=("name", "url", "healthcheck"))
    # add data in the tables
    for system_name in services.keys():
        row = (system_name,
               services.get(system_name).get("url", ""),
               services.get(system_name).get("healthcheck", ""))
        feature.background.steps[0].table.add_row(row)
