
NUM_PAYMENTS=$1 ENVIRONMENT=$2 SUBKEY=$3 node base64.js | xargs bash chunked.sh
#NUM_PAYMENTS=$1 ENVIRONMENT=$2 SUBKEY=$3 node base64.js