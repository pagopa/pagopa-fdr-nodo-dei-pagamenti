ENV="$(echo "$1" | awk '{print toupper($1)}')" CI=$2 FLOW_ID=$3 PSP=$4 SUBKEY=$5

FD3_GET_URL=$(if [ $ENV == "DEV" ];
                then echo 'https://api.dev.platform.pagopa.it/fdr-org/service/v1';
                else echo 'https://api.uat.platform.pagopa.it/fdr-org/service/v1';
              fi)

curl --location --request GET ${FD3_GET_URL}/organizations/${CI}/fdrs/${FLOW_ID}/revisions/1/psps/${PSP} \
--header "Accept: application/json" \
--header "Ocp-Apim-Subscription-Key: ${SUBKEY}"
