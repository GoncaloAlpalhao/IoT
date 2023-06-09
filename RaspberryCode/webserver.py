# Simple HTTP Server Example
# Control an LED and read a Button using a web browser

import time
import network
import socket
import machine
from machine import Pin, ADC

led = Pin(15, Pin.OUT)
ledState = 'LED State Unknown'
LED = Pin("LED", Pin.OUT)
button = Pin(16, Pin.IN, Pin.PULL_UP)

ssid = 'TPSI'
password = 'tpsi2022'

wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(ssid, password)

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
            <button class="buttonGreen" name="led" value="on" type="submit">LED ON</button>
            <br><br>
            <button class="buttonRed" name="led" value="off" type="submit">LED OFF</button>
        </center>
    </form>
    <br><br>
    <br><br>
    <p><span id="temperature">%s</span></p>
</body>
</html>
"""

# Wait for connect or fail
max_wait = 10
while max_wait > 0:
    if wlan.status() < 0 or wlan.status() >= 3:
        break
    max_wait -= 1
    print('waiting for connection...')
    time.sleep(1)
    
# Handle connection error
if wlan.status() != 3:
    raise RuntimeError('network connection failed')
else:
    print('Connected')
    status = wlan.ifconfig()
    print( 'ip = ' + status[0] )
    
    
# Open socket
addr = socket.getaddrinfo('0.0.0.0', 80)[0][-1]
s = socket.socket()
s.bind(addr)
s.listen(1)
print('listening on', addr)

def temp():
    #Criar uma instância para poder ler a temperatura
    temp = machine.ADC(machine.ADC.CORE_TEMP)
    #Formula para converter o valor lido para Celsius
    tempCelsius = 27 - (((temp.read_u16()*(3.3/65535)) - 0.706) / 0.001721)                                                                                       
    return str(tempCelsius)

# Listen for connections, serve client
while True:
    try:
        cl, addr = s.accept()
        print('client connected from', addr)
        request = cl.recv(1024)
        print("request:")
        print(request)
        request = str(request)
        led_on = request.find('led=on')
        led_off = request.find('led=off')
        
        print( 'led on = ' + str(led_on))
        print( 'led off = ' + str(led_off))
        
        if led_on == 8:
            print("led on")
            LED.on()
            led.value(1)
        if led_off == 8:
            print("led off")
            LED.off()
            led.value(0)
        
        ledState = '''<h1>LED OFF</h1><img src="https://sentralservers.files.wordpress.com/2016/08/watching-the-console-whilst-rebooting-a-server-at-3am.gif"><br>''' if led.value() == 0 else '''<h1>LED ON</h1><img src="https://media.tenor.com/arqlNu8gyJYAAAAM/cat-cat-jumping.gif"><br>''' # a compact if-else statement
        
        temperatura = " <br><h3>Temperatura: </h3><span id='temperature'>" + temp() + "</span>"

        # Handle temperature request separately
        if '/temperature' in request:
            temperatura = " <br><h3>Temperatura: </h3><span id='temperature'>" + temp() + "</span>"
            cl.send('HTTP/1.0 200 OK\r\nContent-type: text/plain\r\n\r\n')
            cl.send(temperatura)
            cl.close()
            continue

        # Create and send response
        stateis = ledState
        response = html % stateis
        cl.send('HTTP/1.0 200 OK\r\nContent-type: text/html\r\n\r\n')
        cl.send(response)
        cl.close()
        
    except OSError as e:
        cl.close()
        print('connection closed')