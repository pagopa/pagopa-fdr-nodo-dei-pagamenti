from tornado.ioloop import IOLoop
from tornado.web import Application, RequestHandler
import json

class MockHandler(RequestHandler):
    def post(self):
        # Recupera il JSON dal corpo della richiesta
        try:
            print(f"Dati ricevuti BODY: {self.request.body}")
            data = json.loads(self.request.body)
            print(f"Dati ricevuti: {data}")

            # Restituisce una risposta di successo con stato 200
            self.set_status(200)
            self.write(json.dumps({"message": "Successo"}))
        except json.JSONDecodeError:
            self.set_status(400)
            self.write(json.dumps({"message": "Errore nel parsing del JSON"}))

if __name__ == "__main__":
    app = Application([
        (r"/mock-endpoint", MockHandler),
    ])
    app.listen(8888)
    print("Server Tornado in esecuzione sulla porta 8888")
    IOLoop.current().start()
