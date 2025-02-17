import psycopg2
import csv
import os

conn = psycopg2.connect(
    dbname="fdr",
    user="fdr",
    password=os.getenv("password"),
    host=os.getenv("host"),
    port=os.getenv("port")
)
cursor = conn.cursor()

query = ("INSERT INTO fdr.rendicontazione (optlock, psp, dominio, id_flusso, data_ora_flusso, stato, inserted_timestamp) "
         "VALUES(0, %s, %s, %s, %s, %s, %s)")

with open('rendicontazione.csv', mode='r', encoding='utf-8') as file:
    csv_reader = csv.reader(file)
    next(csv_reader) # skip header
    i = 0
    for row in csv_reader:
        cursor.execute(query, row)
        conn.commit()
        print(f"done item ${i}")
        i += 1


conn.close()

print("Data saved!")