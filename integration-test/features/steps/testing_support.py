import traceback, requests

def executeQuery(context, query:str) -> list:
    print(f' Executing query [{query}] on OracleDB passing by apiconfig-testing-support service...')
    try:

        url = context.config.userdata.get("services").get("apiconfig-testing-support").get("url")
        return requests.post(url, json = query)

    except:
        print('Error executed query')
        traceback.print_exc()