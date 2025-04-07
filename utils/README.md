This directory contains utility scripts for generating and uploading FdRs on PSP side.
Additionally, it contains a distinct script to check the uploaded FdR on Phase 3 CI side.

The nodoInviaFlussoRendicontazione script call the base64.js script to generate the FdR and then uploads it to the FdR Phase 1 service using the chunked.sh script.

The parameters required for the nodoInviaFlussoRendicontazione.sh script are:
- NUM_PAYMENTS: The number of payments to be included in the FdR.
- ENV: The environment to be used (DEV or UAT).
- APIM_SUBKEY: The API Management subscription key for authentication.

The fdr3-verify-flow.sh script is used to check the uploaded FdR on Phase 3 CI side.
It is supposed to be run after a while the nodoInviaFlussoRendicontazione.sh script has been executed successfully.

The scripts are designed to be run in a Linux environment and require Node.js to be installed.


Example script to generate and upload FdR:
```
./nodoInviaFlussoRendicontazione.sh 50 <DEV|UAT> <Valid APIM SUBKEY>
```

Example script to generate the FdR but not upload it:
The base64.js script generates a FdR file with a specified number of payments and a given environment (DEV or UAT).
In this case since the resulting flow is not uploaded to the FdR Phase 1 service, the script will not require a valid APIM SUBKEY.
```
NUM_PAYMENTS=111 node DEV base64.js
```

Example script to check the uploaded FdR on Phase 3 CI side:
The parameters required for the fdr3-verify-flow.sh script are:
- ENV: The environment to be used (DEV or UAT).
- CI: The CI identifier (e.g., "15376371009").
- FLOW_ID: The ID of the flow to be checked.
- PSP: The ID of the PSP.
- SUBKEY: The API Management subscription key for authentication.
```
./fdr3-verify-flow.sh DEV 15376371009 2025-04-0160000000001-S241698127 60000000001 <Valid APIM SUBKEY>
```
This script and the required parameters are mentioned on a successful run of the chunked.sh script run by nodoInviaFlussoRendicontazione.sh