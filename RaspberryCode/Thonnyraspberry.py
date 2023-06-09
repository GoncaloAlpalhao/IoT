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
#Criar instância da interface de rede
wlan = network.WLAN(network.STA_IF)
#Ativar a interface de rede
wlan.active(True)
#Conectar à rede TPSI
wlan.connect("Galaxy S20 FE 5G","EpaEnfim123")
#Esperar até que esteja conectado
while(wlan.isconnected() == False):
    time.sleep(0.2)
#Imprimir o IP
print("Conectado à rede TPSI\nIP: ", wlan.ifconfig()[0])                    

#MQTT
global msgAux                     #Variavel global para anti spam
msgAux = "873234289"              #Valor inicial da variavel (random)
mqtt_server = "192.168.228.127"     #IP do broker MQTT
client_id = "testmqtt"            #ID do cliente MQTT
topic_pub = "temperatura"         #Topico onde vai ser publicada a mensagem
topic_sub = "LED"                 #Topico onde o raspberry vai subscrever

#Função de subscrição
def sub_cb(topic, msg):
    global msgAux
    #Converter a mensagem para string, passar para minúsculas e remover espaços
    msg = msg.decode('utf-8').lower().strip()
    #Se a mensagem for igual à anterior, sai da função para evitar o spam de mensagens
    if(msg == msgAux):
        return
    print("New message on topic {}".format(topic.decode('utf-8')), ": ", msg)
    if msg == "on":
        LED.on()
    elif msg == "off":
        LED.off()
    #Se a mensagem não for "on" ou "off", o LED pisca 5 vezes para indicar que a mensagem não foi reconhecida
    else:
        blink(5,0.1)
    #Guardar a mensagem atual para a comparar com a próxima
    msgAux = msg

#Função de conexão ao broker MQTT
def mqtt_connect():
    client = MQTTClient(client_id, mqtt_server, user="goncalo", password="123456", keepalive=60)
    client.set_callback(sub_cb)                                                                       
    client.set_last_will(topic_pub, "Mission Failed, Raspberry down!", retain=False, qos=0)
    client.connect(clean_session=False)
    print("Connected to %s MQTT broker" % (mqtt_server))
    #Piscar o LED para indicar ao cliente que está conectado
    blink(3,0.333)
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

#Main (permite a subscrição e publicação em simultâneo)
try:
    client = mqtt_connect()
except OSError as e:
    reconnect()
while True:
    
    #Subscrever ao topico para modificar o estado do LED
    client.subscribe(topic_sub)
    #Publicar a mensagem no topico
    client.publish(topic_pub, message(), retain=True)
    time.sleep(1)

