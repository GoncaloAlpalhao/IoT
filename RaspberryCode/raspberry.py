import wifimgr
import network
import time
import machine
from machine import Pin, I2C
from umqtt.simple import MQTTClient
import random
import utime

#LED
#Definir o pino do LED
LED = Pin("LED", Pin.OUT)
#Apaga previamente o LED
LED.off()
#Inicializar o módulo I2C
i2c=I2C(0,sda=Pin(0), scl=Pin(1), freq=400000)

#WIFI
#Inicializar o módulo WIFI
wlan = wifimgr.get_connection()
#Verificar se a conexão foi bem sucedida
if wlan is None:
    print("Could not initialize the network connection.")
    while True:
        pass  #No trespassing :D
#Imprimir o IP
print(wlan.ifconfig()[0])                 

#MQTT
mqtt_server = "192.168.1.144"     #IP do broker MQTT
client_id = "testmqtt"            #ID do cliente MQTT
topic_pub = "temperatura"         #Topico onde vai ser publicada a mensagem
topic_sub = "LED"                 #Topico onde o raspberry vai subscrever

#Função de subscrição
def sub_cb(topic, msg):
    msg = msg.decode('utf-8').lower().strip()
    print("New message on topic {}".format(topic.decode('utf-8')), ": ", msg)
    if msg == "on":
        LED.on()
    elif msg == "off":
        LED.off()
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
    blink(3,0.333)
    client.subscribe(topic_sub)
    LED.on()
    return client

#Função de reconexão ao broker MQTT
def reconnect():
    print("Failed to connect to MQTT broker. Retrying...")
    time.sleep(5)
    machine.reset()

#Função para criar output
def message():
    #Criar uma instância para poder ler a temperatura
    temp = machine.ADC(machine.ADC.CORE_TEMP)
    #Formula para converter o valor lido para Celsius
    tempCelsius = 27 - (((temp.read_u16()*(3.3/65535)) - 0.706) / 0.001721)                                                                                       
    #Formar a mensagem final
    output = "\tTemperatura: " + str(tempCelsius) + " Celsius"
    return str(output)

#Função para fazer o LED piscar "max" vezes com um intervalo de "pulse" segundos
def blink(max, pulse):
    for i in range(max):
        LED.off()
        time.sleep(pulse)
        LED.on()
        time.sleep(pulse)

#Função para iterar o subscritor de 1s em 1s durante 4s
def sub_delay():
    for i in range(4):
        client.check_msg()
        time.sleep(1)

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
        client.publish(topic_pub, message(), qos=1, retain=True)
        time.sleep(1)
    except OSError as e:
        reconnect()


