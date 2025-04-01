This directory contains utility scripts for generating and uploading FdRs on PSP side.
Additionally, it contains a distinct script to check the uploaded FdR on Phase 3 CI side.

Example script to generate and upload FdR:
```
./nodoInviaFlussoRendicontazione.sh 50 DEV <Valid APIM SUBKEY>
```

Example script to generate the FdR but not upload it:

```
NUM_PAYMENTS=111 node DEV base64.js
```

Example script to check the uploaded FdR on Phase 3 CI side:
```
./fdr3-verify-flow.sh https://api.dev.platform.pagopa.it/fdr-org/service/v1 15376371009 2025-04-0160000000001-S241698127 60000000001 <Valid APIM SUBKEY>
```
