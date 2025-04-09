
NUM_PAYMENTS=$1 ENVIRONMENT=$(echo "$2" | awk '{print toupper($0)}') SUBKEY=$3 node base64.js | xargs bash chunked.sh