#!/usr/bin/python3

import paho.mqtt.client as mqtt
import time
import requests
import json
from datetime import datetime
import cv2
import numpy as np
from picamera2 import Picamera2, Preview
import base64
from geopy.geocoders import Nominatim

# adds a delay of 60 seconds to allow everything to start up
time.sleep(60)

########################################################################################
################################ Variables and Constants ###############################
########################################################################################

# broker configuration
broker_address = "193.137.5.80" # broker IP address
port = 8080 # broker port

# API URL
api_url = "https://dripdrop.danielgraca.com/PHP-API/states"

# Path to the image
image_path = '/home/pico/test.jpg'

# Initialize the geolocator 
#geolocator = Nominatim(user_agent="geoapiExercises")

# Coordinates (Pre defined because the raspberry does not have GPS)
Latitude = "39.602"
Longitude = "-8.4092"

# Variables to store the mode and the irrigation state
modo = ""
estadoRega = ""

# Variables to store the user and plant ID
userID = None
plantID = None

# Variable to store the type ID
typeID = None

# Get the location
#location = geolocator.reverse(Latitude+","+Longitude)

# Get the address
#address = location.raw['address']

# Get the city and the country
#city = address.get('city', '')
#country = address.get('country', '')

########################################################################################
######################################### MQTT #########################################
########################################################################################

#
# Callback function to handle the connection to the broker
#
# param client: the client instance for this callback
# param userdata: the private user data as set in Client()
# param flags: response flags sent by the broker
# param rc: the connection result
#
def on_connect(client, userdata, flags, rc): 
        print(f"Conectado com código de resultado: {rc}")
        client.subscribe("manMode")
        client.subscribe("rega")
        
#
# Callback function to handle the messages received
#
# param client: the client instance for this callback
# param userdata: the private user data as set in Client()
# param message: an instance of MQTTMessage. This is a class with members topic, payload, qos, retain
#
def on_message(client, userdata, message):

        # Use the global variables
        global modo
        global estadoRega

        # Print the message received
        print(f"Mensagem recebida no tópico {message.topic}: {message.payload.decode()}")

        # Check the topic
        if message.topic == "manMode":
                # Update the mode variable with the message received on the topic "manMode"
                modo = message.payload.decode()
        elif message.topic == "rega":
                # Update the estadoRega variable with the message received on the topic "rega"
                estadoRega = message.payload.decode()


client = mqtt.Client()                # Create the MQTT client
client.on_connect = on_connect        # Set the on_connect callback function
client.on_message = on_message        # Set the on_message callback function

#
# Function to reconnect to the broker
#
def reconnect():
        # Print the error message
        print("Falha ao conectar ao broker MQTT. Tentando reconectar...")
        time.sleep(5)

        # Try to reconnect to the broker
        try:
                client.reconnect()

        # If an error occurs, print the error message
        except OSError as e:
                print(f"Erro ao reconectar: {e}")
                
########################################################################################
###################################### Functions #######################################
########################################################################################

#
# Function to calculate the NDVI
#
# param image_path: the path to the image
# return: the mean value of the NDVI
#
def calculate_ndvi(image_path):
        
        # Read the image
	imagem = cv2.imread(image_path)

	# We are using a blue filter so we can get the NIR from the blue channel
	nir = imagem[:, :, 0].astype(float)

	# Gets the red channel from the image
	red = imagem[:, :, 2].astype(float)

	# Calculate the NDVI using the formula (NIR - RED) / (NIR + RED) and ignore the division by zero
	with np.errstate(divide='ignore', invalid='ignore'):
		
		# Calculate the NDVI
		ndvi = (nir - red) / (nir + red)
		
		# Replace the NaN values with 0
		ndvi[np.isnan(ndvi)] = 0
		
	# Calculate the mean of the NDVI values
	ndvi_mean = np.mean(ndvi)
        
        # Return the mean value
	return ndvi_mean

#
# Function to capture a photo
#         
def capture_photo():
        # Create a new Picamera2 object
	picam2 = Picamera2()

        # Create a new configuration for the camera
	camera_config = picam2.create_still_configuration(main={"size": (1920, 1080)}, lores={"size": (640, 480)}, display="lores")
        # Configure the camera with the new configuration
	picam2.configure(camera_config)

        # Start the camera
	picam2.start()
        # Wait for 2 seconds
	time.sleep(2)
        # Capture the photo and save it to the file "test.jpg"
	picam2.capture_file("test.jpg")

        # Stop the camera
	picam2.close()

#
# Function to get the plant type
#
# param tipo: the type of the plant
# return: the minimum and maximum humidity values for the plant in an object
#
def get_plant_type(tipo):

        # Gets from the API the data of the plant type
        try:
                # Send a request to the API
                response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/types/{tipo}////rasp")

                # Decode the JSON response
                plant_type = response.json()

                # Store the data in the object
                return {
                        "minH": plant_type["min_humidity"],
                        "maxH": plant_type["max_humidity"]
                }
        # If an error occurs while sending the request to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f"Erro ao conectar com a API: {e}")
                return None

        
        # # Read the JSON file with the plant types
        # try:
        #         # Open the file
        #         with open('tipos.json', 'r') as file:
        #                 # Read the content of the file
        #                 conteudo = file.read()
        #                 #print(f"JSON: {conteudo}")
        #                 # Decode the JSON content
        #                 dados = json.loads(conteudo)
        # # If the file is not found, return an error message
        # except FileNotFoundError:
        #         return 'Erro: o arquivo tipos.json não foi encontrado.'
        # # If an error occurs while decoding the JSON file, return an error message
        # except json.JSONDecodeError as e:
        #         return f'Erro: não foi possível decodificar o arquivo JSON. Detalhes: {e}'
                
        # # Search for the plant type in the JSON file
        # for plantaTipo in dados:
        #         # Check if the plant type is the same as the one received
        #         if plantaTipo['nome'].lower() == tipo.lower():
        #                 # Return the minimum and maximum humidity values for the plant
        #                 return {
        #                 "minH": plantaTipo["minH"],
        #                 "maxH": plantaTipo["maxH"]
        #                 }
                
        # # If the plant type is not found, return an error message
        # return f'Erro: Tipo "{tipo}" não encontrado.'


#
# Function to send the data to the API
#
# param data: the data to send
#
def send_data_to_api(data):
        try:
                # Headers for the request to the API
                headers = {
                        "Content-Type": "application/json"
                }
                # Send the data to the API
                try:
                        response = requests.post(api_url, headers=headers, json=data)
                        # Check the status code of the response
                        if response.status_code == 201:
                                print("Data successfully sent to the API!")
                        else:
                                # If an error occurs while sending the data, print the status code
                                print(f"Failed to send data to the API. Status code: {response.status_code}")
                # If an error occurs while sending the data, print the error message
                except requests.exceptions.RequestException as e:
                        print(f"Erro ao conectar com a API: {e}")
        # If an error occurs while decoding the JSON file, print the error message
        except json.JSONDecodeError as e:
                print(f"Erro ao decodificar a mensagem JSON: {e}")

#
# Function to get the sensor data
#
# param minMaxH: the minimum and maximum humidity values for the plant
#
def get_sensor_data(minMaxH):
        
        # Variables to store the data
        data = {
                "plant":1,                                              # Plant ID
                "humidity_air":50,                                      # Air humidity
                "temperature":25.0,                                     # Temperature
                "wind_direction":"N",                                   # Wind direction
                "wind_speed":10.0,                                      # Wind speed
                "precipitation":0.0,                                    # Precipitation
                "humidity_soil":60,                                     # Soil humidity
                "date": datetime.now().strftime('%Y-%m-%d'),            # Date
                "time": datetime.now().strftime('%H:%M:%S'),            # Hour
                "ndvi": 0,                                              # NDVI
                "image": "NA",                                          # Image
                "irrigation": "NA"                                      # Irrigation state
        }
        
        # Variables to store the last plant ID
        last_line = "0"
        
########################################## Plant ID ##############################################

        # Read the last plant ID from the file
        try:
                # Open the file
                with open('PlantaID', 'r') as file:
                        # Read the lines of the file
                        lines = file.readlines()
                        # Check if there are lines in the file
                        if lines:
                                # Get the last line
                                last_line = lines[-1].strip()
        # If the file is not found, print an error message
        except FileNotFoundError:
                print('Ficheiro "PlantaID" não encontrado.')
        # If an error occurs while reading the file, print the error message
        except Exception as e:
                print(f'Erro ao ler o ficheiro "PlantaID": {e}')

        # Store the last plant ID in the data object
        data["plant"] = last_line

########################################## NDVI and Image ##############################################

        # Capture a new photo
        capture_photo()
        	
        # Calculate the NDVI of the image, round it to 3 decimal places and store it in the data object
        data["ndvi"] = calculate_ndvi(image_path).round(3)

        # Encode the image in base64 and store it in the data object
        try:
                # Open the image file
                with open(image_path, 'rb') as image_file:
                        # Encode the image in base64
                        encoded_image = base64.b64encode(image_file.read()).decode('utf-8')
                        # Store the encoded image in the data object
                        data["image"] = encoded_image
        # If an error occurs while encoding the image, print the error message
        except Exception as e:
                print(f"Erro ao codificar a imagem: {e}")

################################ Weather API - Air Humidity, Temperature, Wind Speed and Precipitation ################################

        # Get the weather data from the OpenWeatherMap API
        try:
                # Send a request to the API
                response = requests.get(f"http://api.openweathermap.org/data/2.5/weather?q=Tomar,pt&units=metric&APPID=faafa1805916ffb1a6eaadd8b29757b9")
                # Decode the JSON response
                weather = response.json()
                
                # Store the weather data in the data object
                data["humidity_air"] = weather["main"]["humidity"]    # Air humidity
                data["wind_speed"] = weather["wind"]["speed"]         # Wind speed
                data["temperature"] = weather["main"]["temp"]         # Temperature
                
                # Check if there is a rain value in the weather data
                if "rain" in weather:
                        # Store the precipitation value in the data object
                        data["precipitation"] = weather["rain"]["1h"]  # Precipitation
                else:
                        # If there is no rain value, store 0 in the data object
                        data["precipitation"] = 0.0
        # If an error occurs while sending the request to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f"Erro ao conectar com a API do tempo: {e}")
           
############################################## Irrigation ##############################################

        # Check the mode of the irrigation
        if modo == "automatico":  

                # If the humidity is below the minimum value
                if (data["humidity_soil"] < minMaxH["minH"] ):
                        # Turn on the irrigation
                        data["irrigation"] = "ON"

                # If the humidity is above the minimum value and below the maximum value
                elif (data["humidity_soil"] > minMaxH["minH"] & data["humidity_soil"] < minMaxH["maxH"]):
                        # Checks the Precipitation value
                        if (data["precipitation"] > 0.3):
                                # If the precipitation is above 0.3, turn off the irrigation
                                data["irrigation"] = "OFF" 
                        # If the precipitation is below 0.3
                        else:
                                # Turn on the irrigation
                                data["irrigation"] = "ON"

                # If the humidity is above the maximum value
                else:
                        # Turn off the irrigation
                        data["irrigation"] = "OFF"

        # If the mode is manual
        else:
                # Check the irrigation state received from the broker and store it in the data object
                if (estadoRega == '"ON"'):
                        data["irrigation"] = "ON"
                else:
                        data["irrigation"] = "OFF"

############################################## MQTT and API ############################################## 

        # Send the data to the API
        try:
                send_data_to_api(data)
        # If an error occurs while sending the data to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f'Erro ao conectar com a API: {e}')
                
        # Publish the data to the MQTT broker
        try: 
                #client.publish("test_topic",json.dumps(data))
                client.publish("humidadeSolo",json.dumps(data["humidity_soil"]), retain=True)
                client.publish("humidadeAr",json.dumps(data["humidity_air"]), retain=True)
                client.publish("temperatura",json.dumps(data["temperature"]), retain=True)
                client.publish("direcaoVento",json.dumps(data["wind_direction"]), retain=True)
                client.publish("velocidadeVento",json.dumps(data["wind_speed"]), retain=True)
                client.publish("precipitacao",json.dumps(data["precipitation"]), retain=True)
                client.publish("ndvi",json.dumps(data["ndvi"]), retain=True)
                client.publish("rega",json.dumps(data["irrigation"]), retain=True)
        # If an error occurs while publishing the data to the MQTT broker, print the error message
        except TypeError as e:
                print(f"Erro ao serializar os dados: {e}")

        # Wait for 30 seconds
        time.sleep(30)

########################################################################################
###################################### Main Code #######################################
########################################################################################

try:
        ###################################### Device ######################################
        # Get the Mac address of the device (WIFI)
        mac = open('/sys/class/net/wlan0/address').readline()

        # Variable to store the device existence in the API
        exists = False

        # Checks if the device is already in the API
        try:
                # Send a request to the API
                response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/devices/{mac}////rasp")
                # Check the status code of the response
                if response.status_code == 200:
                        print("Device already exists in the API!")
                        exists = True
                else:
                        # If the device is not in the API, print the status code
                        print(f"Device not found in the API. Status code: {response.status_code}")
                        exists = False
        # If an error occurs while sending the request to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f"Erro ao conectar com a API: {e}")

        # Create the device in the API
        if not exists:
                try:
                        # Data to create the device
                        device = {
                                "id": mac,
                        }
                        # Send the data to the API
                        response = requests.post("https://dripdrop.danielgraca.com/PHP-API/devices", json=device)
                        # Check the status code of the response
                        if response.status_code == 201:
                                print("Device successfully created in the API!")
                        else:
                                # If an error occurs while creating the device, print the status code
                                print(f"Failed to create device in the API. Status code: {response.status_code}")
                # If an error occurs while sending the data to the API, print the error message
                except requests.exceptions.RequestException as e:
                        print(f"Erro ao conectar com a API: {e}")

        ###################################### Device Data (User and plant) ######################################

        # Gets from the API the last data of the device (id, fk_user, fk_plant)
        try:
                while (userID == None or plantID == None):
                        # Send a request to the API
                        response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/devices/{mac}////rasp")
                        # Decode the JSON response
                        device_data = response.json()
                        # Store the last data in the data object
                        userID = device_data["fk_user"]
                        plantID = device_data["fk_plant"]
                        # Wait for 5 seconds
                        time.sleep(5)
        # If an error occurs while sending the request to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f"Erro ao conectar com a API: {e}")
        

###################################### Plant Type ######################################

        # # Variable to store the name of the plant type from the file
        # last_line_type = "NA"
        
        # # Gets the last plant type given to the plant from the file "PlantaTipo"
        # try:
        #         # Read the last plant type from the file
        #         with open('PlantaTipo', 'r') as file:
        #                 # Read the lines
        #                 lines = file.readlines()
        #                 # Check if there are lines in the file
        #                 if lines:
        #                         # Get the last line
        #                         last_line_type = lines[-1].strip()
        # # If the file is not found, print an error message
        # except FileNotFoundError:
        #         print('Ficheiro "PlantaTipo" não encontrado.')
        # # If an error occurs while reading the file, print the error message
        # except Exception as e:
        #         print(f'Erro ao ler o ficheiro "PlantaTipo": {e}')
        
        # # Get the minimum and maximum humidity values for the plant
        # minMaxH = get_plant_type(last_line_type)

        # Gets from the API the type of the plant
        try:
                while (typeID == None):
                        # Send a request to the API
                        response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/plants/{plantID}////rasp")
                        # Decode the JSON response
                        plant_data = response.json()
                        # Store the last data in the data object
                        typeID = plant_data["fk_type"]
                        # Wait for 5 seconds
                        time.sleep(5)
        # If an error occurs while sending the request to the API, print the error message
        except requests.exceptions.RequestException as e:
                print(f"Erro ao conectar com a API: {e}")

        # Get the minimum and maximum humidity values for the plant
        minMaxH = get_plant_type(typeID)

        
###################################### MQTT ######################################

        # Connect to the broker and start the loop
        client.connect(broker_address, port, 60)
        client.loop_start()

# If an error occurs while connecting to the broker, print the error message     
except OSError as e:
        reconnect()

####################################### MAIN ########################################

# Main loop to get the sensor data every 30 minutes
while True:
        # Try to get the sensor data
        try:
                # Checks if its 11AM or 3PM (Day)
                if datetime.now().hour == 11 or datetime.now().hour == 15:
                        # Verify if the plant and its type are the same
                        #Verify if the plant is the same

                        # Gets from the API the last data of the device (id, fk_user, fk_plant)
                        try:
                                # Send a request to the API
                                response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/devices/{mac}////rasp")
                                # Decode the JSON response
                                device_data = response.json()
                                # Store the last data in the data object
                                newUserID = device_data["fk_user"]
                                newPlantID = device_data["fk_plant"]
                                # Wait for 5 seconds
                                time.sleep(5)

                                # If the user ID or the plant ID are different from the last ones
                                if (newUserID != userID or newPlantID != plantID):
                                        # Update the user ID and the plant ID
                                        userID = newUserID
                                        plantID = newPlantID

                                        # Gets from the API the type of the plant
                                        try:
                                                
                                                # Send a request to the API
                                                response = requests.get(f"https://dripdrop.danielgraca.com/PHP-API/plants/{plantID}////rasp")
                                                # Decode the JSON response
                                                plant_data = response.json()
                                                # Store the last data in the data object
                                                typeID = plant_data["fk_type"]
                                                # Wait for 5 seconds
                                                time.sleep(5)
                                        # If an error occurs while sending the request to the API, print the error message
                                        except requests.exceptions.RequestException as e:
                                                print(f"Erro ao conectar com a API: {e}")

                                        # Get the minimum and maximum humidity values for the plant
                                        minMaxH = get_plant_type(typeID)

                        # If an error occurs while sending the request to the API, print the error message
                        except requests.exceptions.RequestException as e:
                                print(f"Erro ao conectar com a API: {e}")

                        # Get the sensor data
                        get_sensor_data(minMaxH)
                        # Wait for 30 minutes
                        time.sleep(1800)
                else:
                        # Wait for 30 minutes
                        time.sleep(1800)
                
        # If an error occurs while getting the sensor data, print the error message
        except OSError as e:
                print(f"Erro ao obter os dados dos sensores: {e}")
                reconnect()