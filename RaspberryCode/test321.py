from machine import ADC, Pin, I2C        #importing relevant modules & classes
from time import sleep
import bme280       #importing BME280 library

i2c=I2C(0,sda=Pin(0), scl=Pin(1), freq=400000)    #initializing the I2C method 

while True:
    bme = bme280.BME280(i2c=i2c)          #BME280 object created
    soil = ADC(Pin(26)) # Soil moisture PIN reference
    #Calibraton values
    min_moisture=19200
    max_moisture=49300
    moisture = (max_moisture-soil.read_u16())*100/(max_moisture-min_moisture)
     # print values
    print("moisture: " + "%.2f" % moisture +"% (adc: "+str(soil.read_u16())+")")
    sleep(2)           #delay of 2s