import wifimgr
import machine
from machine import Pin, I2C, ADC
import time
import network
from umqtt.simple import MQTTClient
from dht20 import DHT20

#LED
#Definir o pino do LED
LED = Pin("LED", Pin.OUT)
#Apaga previamente o LED
LED.off()
#Inicializar o módulo I2C
i2c0_sda = Pin(0)
i2c0_scl = Pin(1)
i2c0 = I2C(0, sda=i2c0_sda, scl=i2c0_scl)
#Inicializar o sensor de humidade do solo
soil = ADC(Pin(26))
#Valores de calibração
min_moisture=19200
max_moisture=49300
#Inicializar o sensor DHT20
dht20 = DHT20(0x38, i2c0)

#WIFI
#Inicializar o módulo WIFI
wlan = wifimgr.get_connection()
#Esperar até que esteja conectado
if wlan is None:
    print("Could not initialize the network connection.")
    while True:
        pass  #No trespassing :D
#Imprimir o IP
print(wlan.ifconfig()[0])                  

#MQTT
mqtt_server = "192.168.1.144"    #IP do broker MQTT
client_id = "testmqtt3428709"     #ID do cliente MQTT
topic_pub = "temperatura"         #Topico onde vai ser publicada a mensagem Last Will
topic_sub = "LED"                 #Topico onde o raspberry vai subscrever
is_Raining = False                #Variável para indicar se está a chover ou não
manual = False                    #Variável para indicar se o sistema está em modo manual ou não

#Função de subscrição
def sub_cb(topic, msg):
    global is_Raining
    global manual
    msg = msg.decode('utf-8').lower().strip()
    print("New message on topic {}".format(topic.decode('utf-8')), ": ", msg)
    #Se a mensagem for "on" ou "off", o LED liga ou desliga para simular o sistema de rega
    if msg == "on" and manual == True:
        client.publish("sistemaRega", msg, retain=True)
        LED.on()
    elif msg == "off" and manual == True:
        client.publish("sistemaRega", msg, retain=True)
        LED.off()
    #Se a mensagem for "rain" ou "norain", a variável is_Raining é atualizada para simular o Rule Engine
    elif msg == "rain" and manual == False:
        is_Raining = True
    elif msg == "norain" and manual == False:
        is_Raining = False
    #Se a mensagem for "manual" ou "automatic", a variável manual é atualizada para simular o Rule Engine
    elif msg == "manual":
        manual = True
        client.publish("manMode", "manual", retain=True)
    elif msg == "automatic":
        manual = False
        client.publish("manMode", "automatico", retain=True)
    #Se a mensagem não for "on" ou "off", o LED pisca 5 vezes para indicar que a mensagem não foi reconhecida
    else:
        blink(5,0.1)

#Função de conexão ao broker MQTT
def mqtt_connect():
    client = MQTTClient(client_id, mqtt_server, user="goncalo", password="123456", keepalive=60)
    client.set_callback(sub_cb)                                                                       
    client.set_last_will(topic_pub, "Mission Failed, Raspberry down!", retain=False, qos=0)
    client.connect(clean_session=False)
    print("Connected to %s MQTT broker" % (mqtt_server))
    #Piscar o LED para indicar ao cliente que está conectado
    blink(2,0.25)
    client.subscribe(topic_sub)
    LED.on()
    return client

#Função de reconexão ao broker MQTT
def reconnect():
    print("Failed to connect to MQTT broker. Retrying...")
    time.sleep(5)
    machine.reset()

#Função para fazer o LED piscar "max" vezes com um intervalo de "pulse" segundos
def blink(max, pulse):
    for i in range(max):
        LED.off()
        time.sleep(pulse)
        LED.on()
        time.sleep(pulse)

#Função para iterar o subscritor de 1s em 1s durante 4s
def sub_delay():
    for i in range(12):
        client.check_msg()
        time.sleep(0.25)

#Função para o sensor DHT20
def messagePublisher():
    global manual
    measurements = dht20.measurements
    moisture = (max_moisture-soil.read_u16())*100/(max_moisture-min_moisture)
    time.sleep(0.2)
    #Criar uma instância para poder ler a temperatura
    temp = machine.ADC(machine.ADC.CORE_TEMP)
    #Formula para converter o valor lido para Celsius
    tempCelsius = 27 - (((temp.read_u16()*(3.3/65535)) - 0.706) / 0.001721)
    time.sleep(0.2)
    #Publicar a humidade do solo
    moisturePub = str(moisture)[:5]
    client.publish("humidadeSolo", moisturePub, retain=True)
    time.sleep(0.2)
    #Publicar a temperatura
    tempPub = str(measurements['t'])[:5]
    client.publish("temperatura", tempPub, retain=True)
    time.sleep(0.2)
    #Publicar a humidade do ar
    humidadeArPub = str(measurements['rh'])[:5]
    client.publish("humidadeAr", humidadeArPub, retain=True)
    time.sleep(0.2)
    #Publicar a temperatura do cpu
    tempCpuPub = str(tempCelsius)[:5]
    client.publish("temperaturaCpu", tempCpuPub, retain=True)
    time.sleep(0.2)
    #Check ao sistema de rega
    rainCheck(moisturePub)
    time.sleep(0.2)

#Função que verifica se a humidade do solo está dentro dos limites e se está a chover não rega
def rainCheck(soil_moisture):
    soil_moisture = float(soil_moisture)
    global manual
    if manual == True:
        return
    if soil_moisture < 50 and is_Raining == False:
        LED.on()
        client.publish("sistemaRega", "on", retain=True)
    elif soil_moisture < 20 and is_Raining == True:
        LED.on()
        client.publish("sistemaRega", "on", retain=True)
    else:
        LED.off()
        client.publish("sistemaRega", "off", retain=True)

#Main (permite a subscrição e publicação em simultâneo)
try:
    client = mqtt_connect()
except OSError as e:
    reconnect()
while True:
    try:
        #Subscrever ao topico para modificar o estado do LED
        sub_delay()
        #Publicar a mensagem no topico
        messagePublisher()
        time.sleep(1)
    except OSError as e:
        reconnect()

