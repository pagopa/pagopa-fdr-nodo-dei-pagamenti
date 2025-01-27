# Performance Tests with k6

[k6](https://k6.io/) is a load testing tool. 👀
See [here](https://k6.io/docs/get-started/installation/) to install it.

## How to Run 🚀

To run k6 tests use the command:

``` shell
k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json --env API_SUBSCRIPTION_KEY=<your-secret> <script-name>.js
```

where

- _VARS_ is a environment file
- _TEST_TYPE_ is a file in `/test-types` folder
- _API_SUBSCRIPTION_KEY_ is your sub-key

`<script-name>.js` is the scenario to run with k6


ENV_JSON=./environments/local.environment.json
TEST_TYPE=./test-types/constant.json
SUB_KEY=8047d472dfc54afb9f3204423d850477
SCRIPT=only_nodoInviaFlussoRendicontazione_scenario
FLOW_SIZE=1
k6 run --env VARS=${ENV_JSON} --env TEST_TYPE=${TEST_TYPE} --env API_SUBSCRIPTION_KEY=${SUB_KEY} --env FLOW_SIZE=${FLOW_SIZE} ${SCRIPT}.js
k6 run --env VARS=./environments/local.environment.json --env TEST_TYPE=./test-types/constant.json --env API_SUBSCRIPTION_KEY=8047d472dfc54afb9f3204423d850477 --env FLOW_SIZE=1 only_nodoInviaFlussoRendicontazione_scenario.js