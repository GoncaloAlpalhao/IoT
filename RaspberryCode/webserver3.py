import wifimgr
from time import sleep
import machine
import gc
import time
from machine import Pin
try:
    import usocket as socket
except:
    import socket

led = Pin("LED", Pin.OUT)
led_state = 'off'
wlan = wifimgr.get_connection()
if wlan is None:
    print("Could not initialize the network connection.")
    while True:
        pass  # you shall not pass :D
led.off()
led_gif = '''<h1>LED OFF</h1><img src="https://media.tenor.com/7gUwUBvlgqAAAAAM/red-button-spam.gif"><br>'''
def web_page():
    # replace the "html" variable with the following to create a more user-friendly control panel
    html = """<!DOCTYPE html><html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="data:,">
    <style>
        html {
            font-family: Helvetica;
            display: inline-block;
            margin: 0px auto;
            text-align: center;
        }
        .buttonGreen {
            background-color: #4CAF50;
            border: 2px solid #000000;;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }
        .buttonRed {
            background-color: #D11D53;
            border: 2px solid #000000;;
            color: white;
            padding: 15px 32px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 16px;
            margin: 4px 2px;
            cursor: pointer;
        }
        body {
      background-color: #f2f2f2;
      margin: 0;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      font-family: 'Arial', sans-serif;
    }
    
    .container {
      background-color: #fff;
      padding: 30px;
      border-radius: 10px;
      box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
      text-align: center;
      animation: pulse 1s infinite;
    }
    
    @keyframes pulse {
      0% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.1);
      }
      100% {
        transform: scale(1);
      }
    }
    
    h1 {
      font-size: 36px;
      color: #ff5500;
      margin: 0 0 20px;
      text-transform: uppercase;
      letter-spacing: 2px;
      animation: glow 2s infinite;
    }
    
    @keyframes glow {
      0% {
        text-shadow: none;
      }
      50% {
        text-shadow: 0 0 10px #ff5500;
      }
      100% {
        text-shadow: none;
      }
    }
    
    h2 {
      font-size: 24px;
      color: #333333;
      margin: 0;
      animation: slideIn 1s;
    }
    
    @keyframes slideIn {
      0% {
        transform: translateX(-100%);
        opacity: 0;
      }
      100% {
        transform: translateX(0);
        opacity: 1;
      }
    }
    
    #temperature {
      font-size: 48px;
      color: #ff9900;
      font-weight: bold;
      margin-top: 20px;
      animation: rotate 4s linear infinite;
    }
    
    @keyframes rotate {
      0% {
        transform: rotate(0);
      }
      100% {
        transform: rotate(360deg);
      }
    }
    
    .footer {
      margin-top: 30px;
      font-size: 14px;
      color: #999999;
      animation: blink 1s infinite;
    }
    
    @keyframes blink {
      0% {
        opacity: 1;
      }
      50% {
        opacity: 0;
      }
      100% {
        opacity: 1;
      }
    }
        text-decoration: none;
        font-size: 30px;
        margin: 2px;
        cursor: pointer;
    </style>
    <script>
        function updateTemperature() {
            var xhttp = new XMLHttpRequest();
            xhttp.onreadystatechange = function() {
                if (this.readyState === 4 && this.status === 200) {
                    document.getElementById("temperature").innerHTML = this.responseText;
                }
            };
            xhttp.open("GET", "/temperature", true);
            xhttp.send();
        }
        
        setInterval(updateTemperature, 1000);
    </script>
</head>
<body>
    <center><h1>Control Panel</h1></center><br><br>
    <form>
        <center>
            <button class="buttonGreen" name="led" value="on" type="submit">Turn Me On</button>
            <br><br>
            <button class="buttonRed" name="led" value="off" type="submit">Turn Me Off</button>
        </center>
    </form>
    <br><br>
    <br><br>

<div id="temperature222">
  <div class="container">
    <h1>Informações sobre a temperatura</h1>
    <h2>Temperatura:</h2>
    <span id="temperature">%s</span>
    <div class="footer">
      <p>© 2023 Todos os direitos reservados</p>
    </div>
  </div></div>
</body>
</html>
"""
    return html

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind(('', 80))
s.listen(5)

def temp():
    #Criar uma instância para poder ler a temperatura
    temp = machine.ADC(machine.ADC.CORE_TEMP)
    #Formula para converter o valor lido para Celsius
    tempCelsius = 27 - (((temp.read_u16()*(3.3/65535)) - 0.706) / 0.001721)                                                                                       
    return str(tempCelsius)
     
# Listen for connections, serve client
while True:
    try:
        if gc.mem_free() < 102000:
            gc.collect()
        cl, addr = s.accept()
        print('client connected from', addr)
        request = cl.recv(1024)
        print("request:")
        print(request)
        request = str(request)
        led_on = request.find('led=on')
        led_off = request.find('led=off')
        time.sleep(0.5)
        print( 'led on = ' + str(led_on))
        print( 'led off = ' + str(led_off))
        
        if led_on == 8:
            print("led on")
            led.on()
            led.value(1)
        if led_off == 8:
            print("led off")
            led.off()
            led.value(0)
        
        ledState = '''<h1>LED OFF</h1><img src="https://i.redd.it/5er6y5o4t0x61.gif"><br>''' if led.value() == 0 else '''<h1>LED ON</h1><img src="https://picture.allocacoc.com.cn/200724/1595569071649435.gif"><br>''' # a compact if-else statement
        temperatura = " <br><h3>Temperatura: </h3><span id='temperature'>" + temp() + "</span>"

        # Handle temperature request separately
        if '/temperature' in request:
            temperatura = temp() + " C"
            cl.send('HTTP/1.0 200 OK\r\nContent-type: text/plain\r\n\r\n')
            cl.send(temperatura)
            cl.close()
            continue

        # Create and send response
        stateis = ledState
        response = web_page() % stateis
        cl.send('HTTP/1.0 200 OK\r\nContent-type: text/html\r\n\r\n')
        cl.send(response)
        cl.close()
        
    except OSError as e:
        cl.close()
        s.close()
        print('connection closed')