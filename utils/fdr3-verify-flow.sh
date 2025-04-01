FD3_GET_URL=$1 CI=$2 FLOW_ID=$3 PSP=$4 SUBKEY=$5

curl --location --request GET ${FD3_GET_URL}/organizations/${CI}/fdrs/${FLOW_ID}/revisions/1/psps/${PSP} \
--header "Accept: application/json" \
--header "Ocp-Apim-Subscription-Key: ${SUBKEY}"
