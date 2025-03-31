# https://everything.curl.dev/http/post/chunked

FILENAME=$1 POST_URL=$2 SUBKEY=$3 CI=$4 FLOW_ID=$5 PSP=$6 FD3_GET_URL=$7
#curl  -H "Transfer-Encoding: chunked" --location --request POST -d @${FILENAME} 'https://api.uat.platform.pagopa.it/nodo-ndp/nodo-per-psp/v1' \
#curl  -H "Transfer-Encoding: chunked" --location --request POST -d @${FILENAME} 'https://api.dev.platform.pagopa.it/nodo/nodo-per-psp/v1' \
#curl  -H "Transfer-Encoding: chunked" --location --request POST -d @${FILENAME} 'localhost:8088/webservices/input' \
curl  -H "Transfer-Encoding: chunked" --location --request POST -d @"${FILENAME}" "${POST_URL}" \
--header 'SOAPAction: nodoInviaFlussoRendicontazione' \
--header 'Content-Type: application/xml' \
--header `Ocp-Apim-Subscription-Key: ${SUBKEY}`

echo "Flow correctly sent by PSP ${PSP} with ID ${FLOW_ID} to ${CI} via ${POST_URL}";
echo "After a while you can use the below curl to verify the flow status";
echo "curl --location --request GET ${FD3_GET_URL}/organizations/{CI}/fdrs/{FLOW_ID}/revisions/1/psps/{PSP} --header 'Accept: application/json' --header 'Ocp-Apim-Subscription-Key: <Insert Correct SUBKEY>' ";

#rm ${FILENAME}