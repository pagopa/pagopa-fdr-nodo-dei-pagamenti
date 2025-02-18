# FDR - Fase 1
Manage FdR Nodo Dei Pagamenti


## Api Documentation 📖
- REST APIs - See the [OpenApi 3 here](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-fdr-nodo-dei-pagamenti/refs/heads/main/openapi/openapi.json)

# 🧩 Integration Test
- Report DEV is available [here](https://pagopadweusharedtstdtsa.blob.core.windows.net/pagopa-fdr-nodo-dei-pagamenti/reports/index.html)
- Report UAT is available [here](https://pagopauweusharedtstdtsa.blob.core.windows.net/pagopa-fdr-nodo-dei-pagamenti/reports/index.html) 

## Run Features Locally with Docker 🐳

From `integration-test` folder:

```shell
docker build -t fdr1_test .

docker run \
-e TAGS="runnable" \
-e ENV="dev" \
-e CONFIG_FILE="/config/dev.json" \
-v  ./reports:/app/reports fdr1_test
```

You can choose the tags to run.
Available values are: 
- `runnable`
- `ftp`
- `bigfdr` 
- `legacy` 
- `""` empty for all

## Run Features Locally 🏡

From `integration-test` folder:


1. install `behave` and `allure`
2. install the `requirements.txt`
3. run `behave --format allure_behave.formatter:AllureFormatter -o results --summary --tags="@runnable" --show-timings -v`
