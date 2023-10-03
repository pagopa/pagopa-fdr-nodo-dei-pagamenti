import cx_Oracle, traceback, os, time


def getConnection(host:str, database:str, user:str, password:str, port:str):
    print(f'Connecting to the OracleDB... {user}')

    try:
        
        dsn = cx_Oracle.makedsn(host, port, service_name= database)
        conn = cx_Oracle.connect(user=user, password=password, dsn=dsn)

        print('Successfully connected to OracleDB')
        return conn
    except :
        print('Error connection to db')
        traceback.print_exc() 


def closeConnection(conn) -> None:
    try:
        if conn is not None:
            print('Closing connection to the OracleDb...')
            conn.close()
            print('Connection to OracleDB closed successfully')
    except:
        print('Error close connection to db')
        traceback.print_exc() 


def executeQuery(conn, query:str) -> list:
    print(f' Executing query [{query}] on OracleDB instance...')
    try:
        
        cur = conn.cursor()
        cur.execute(query)

        if 'SELECT' in query:    
            rows = cur.fetchall()
            print(f' Query executed successfully - [{len(rows)}] row/s found')
            return rows
        elif 'UPDATE' in query:
            conn.commit()
            print("Update executed successfully")

    except:
        print('Error executed query')
        traceback.print_exc() 



