# https://everything.curl.dev/http/post/chunked

FILENAME=$1
#curl  -H "Transfer-Encoding: chunked" --location --request POST -d @${FILENAME} 'https://api.uat.platform.pagopa.it/nodo/nodo-per-psp/v1' \
curl  -H "Transfer-Encoding: chunked" --location --request POST -d @${FILENAME} 'localhost:8088/webservices/input' \
--header 'SOAPAction: nodoInviaFlussoRendicontazione' \
--header 'Content-Type: application/xml'

rm ${FILENAME}