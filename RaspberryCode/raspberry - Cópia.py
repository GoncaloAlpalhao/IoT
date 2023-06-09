import network
import time
import machine
from machine import Pin, PWM, ADC
from umqtt.simple import MQTTClient
import random
import utime

servo = PWM(Pin(0))
servo.freq(50)
in_min = 0
in_max = 65535

#LED
#Definir o pino do LED
LED = Pin("LED", Pin.OUT)
#Apaga previamente o LED
LED.off()


#WIFI
#Criar instância da interface de rede
wlan = network.WLAN(network.STA_IF)
#Ativar a interface de rede
wlan.active(True)
#Conectar à rede TPSI
wlan.connect("TPSI","tpsi2022")
#Esperar até que esteja conectado
while(wlan.isconnected() == False):
    time.sleep(0.2)
#Imprimir o IP
print("Conectado à rede TPSI\nIP: ", wlan.ifconfig()[0])                    

#MQTT
msgAux = "873234289"              #Valor inicial da variavel (random)
mqtt_server = "192.168.1.147"     #IP do broker MQTT
client_id = "testmqtt"            #ID do cliente MQTT
topic_pub = "temperatura"         #Topico onde vai ser publicada a mensagem
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
    #Obter o tempo atual
    current_time = utime.time()
    #Converter o tempo atual para uma tuplo
    time_tuple = utime.gmtime(current_time)
    #Formatar a string
    time_str = "{:04d}-{:02d}-{:02d} {:02d}:{:02d}:{:02d}".format(time_tuple[0], time_tuple[1], time_tuple[2], time_tuple[3], time_tuple[4], time_tuple[5])
    #Formar a mensagem final
    output = "From Alpalhao ---> " + time_str + "\tTemperatura: " + str(tempCelsius) + " Celsius"
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


