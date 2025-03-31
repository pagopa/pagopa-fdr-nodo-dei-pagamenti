This directory contains utility scripts for generating and uploading FdRs on PSP side.
Additionally, it contains a functionally distinct script to check the uploaded FdR on CI side through ....

Example script to generate and upload FdR:
```
./nodoInviaFlussoRendicontazione.sh 50 DEV <Valid SUBKEY>
```

Example script to generate the FdR but not upload it:

```
NUM_PAYMENTS=111 node DEV base64.js
```
