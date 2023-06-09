import wifimgr
import machine
import gc
import time
try:
    import usocket as socket
except:
    import socket

wlan = wifimgr.get_connection()
if wlan is None:
    print("Could not initialize the network connection.")
    while True:
        pass  # No trespassing :D
# Print the IP address
print(wlan.ifconfig()[0])
def web_page():
    # Information page for the user
    html = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>Conexão à Internet</title>
      <style>
        body {
          background-color: #f2f2f2;
          font-family: Arial, sans-serif;
          text-align: center;
          margin-top: 20%;
        }

        h1 {
          font-size: 24px;
          margin-bottom: 10px;
        }

        p {
          font-size: 18px;
          color: #333;
        }

        img {
          max-width: 300px;
          margin-top: 20px;
        }
      </style>
    </head>
    <body>
      <h1>Conexão à Internet</h1>
      <p>O dispositivo está conectado à internet.</p>
      <img src="https://i.redd.it/7w1v3nu7wufa1.gif" alt="gif" />
    </body>
    </html>
    """
    return html

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('', 80))
s.listen(5)

while True:
    try:
        if gc.mem_free() < 102000:
            gc.collect()
        
        # Verificar se a página ainda não foi enviada
        cl, addr = s.accept()
        # Criar e enviar a resposta
        response = web_page()
        cl.send('HTTP/1.0 200 OK\r\nContent-type: text/html\r\n\r\n')
        cl.send(response)
        cl.close()
        
    except OSError as e:
        cl.close()
        s.close()
        print('connection closed')