import psycopg2
from psycopg2 import OperationalError


def create_connection(db_name, db_user, db_password, db_host, db_port):
    connection = None
    try:
        connection = psycopg2.connect(
            database=db_name,
            user=db_user,
            password=db_password,
            host=db_host,
            port=db_port,
            # options=f'-c search_path={db_user}',
        )
        print("Connection to PostgreSQL DB successful")
    except OperationalError as e:
        print(f"The error '{e}' occurred")
    return connection

# wrapper

def getConnection(host:str, database:str, user:str, password:str, port:str):
    return create_connection(database, user, password, host, port )



def execute_read_query(connection, query):
    print('execute_sql_query ...')
    print(query)
    cursor = connection.cursor()
    result = None
    try:
        cursor.execute(query)
        
        if 'SELECT' in query:
            result = cursor.fetchall()
            print(f' Query executed successfully - [{len(result)}] row/s found')
            return result
        elif 'UPDATE' in query:
            connection.commit()
            print("Update executed successfully")

        
    except OperationalError as e:
        print(f"The error '{e}' occurred")

# wrapper
def executeQuery(conn, query:str) -> list:
    return execute_read_query(conn, query)



def closeConnection(conn) -> None:
    try:
        if conn is not None:
            print('Closing connection to the PgDb...')
            conn.close()
            print('Connection to PgDb closed successfully')
    except:
        print('Error close connection to db')
        traceback.print_exc()
