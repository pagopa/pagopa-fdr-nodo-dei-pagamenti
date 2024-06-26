# sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <k6-db-name> <subkey> <flow_size>

ENVIRONMENT=$1
TYPE=$2
SCRIPT=$3
DB_NAME=$4
API_SUBSCRIPTION_KEY=$5
FLOW_SIZE=$6

if [ -z "$ENVIRONMENT" ]
then
  echo "No env specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <flow_size>"
  exit 1
fi

if [ -z "$TYPE" ]
then
  echo "No test type specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <flow_size>"
  exit 1
fi
if [ -z "$SCRIPT" ]
then
  echo "No script name specified: sh run_performance_test.sh <local|dev|uat> <load|stress|spike|soak|...> <script-name> <db-name> <subkey> <flow_size>"
  exit 1
fi

if [ -z "$DB_NAME" ]
then
  DB_NAME="k6"
  echo "No DB name specified: 'k6' is used."
fi

export env=${ENVIRONMENT}
export type=${TYPE}
export script=${SCRIPT}
export db_name=${DB_NAME}
export sub_key=${API_SUBSCRIPTION_KEY}
export flow_size=${FLOW_SIZE}

stack_name=$(cd .. && basename "$PWD")
docker compose -p "${stack_name}-k6" up -d --remove-orphans --force-recreate --build
docker logs -f k6
docker stop nginx
