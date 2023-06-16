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
mqtt_server = "192.168.72.212"    #IP do broker MQTT
client_id = "testmqtt3428709"     #ID do cliente MQTT
topic_pub = "temperatura"         #Topico onde vai ser publicada a mensagem Last Will
topic_sub = "LED"                 #Topico onde o raspberry vai subscrever

#Função de subscrição
def sub_cb(topic, msg):
    msg = msg.decode('utf-8').lower().strip()
    print("New message on topic {}".format(topic.decode('utf-8')), ": ", msg)
    dp = 1
    if msg == "on":
        LED.on()
        dp += 1
    elif msg == "off":
        LED.off()
        dp += 1
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
    for i in range(3):
        client.check_msg()
        time.sleep(1)

#Função para o sensor BME280
def messagePublisher():
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