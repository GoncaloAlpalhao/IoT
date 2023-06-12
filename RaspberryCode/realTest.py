from machine import Pin, I2C, ADC
from utime import sleep

from dht20 import DHT20


i2c0_sda = Pin(0)
i2c0_scl = Pin(1)
i2c0 = I2C(0, sda=i2c0_sda, scl=i2c0_scl)
soil = ADC(Pin(26)) # Soil moisture PIN reference
#Calibraton values
min_moisture=19200
max_moisture=49300
dht20 = DHT20(0x38, i2c0)

while True:
    measurements = dht20.measurements
    moisture = (max_moisture-soil.read_u16())*100/(max_moisture-min_moisture)
    print(f"Soil moisture: {moisture} %")
    print(f"Temperature: {measurements['t']} °C, humidity: {measurements['rh']} %RH")
    sleep(1)