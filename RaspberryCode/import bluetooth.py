import bluetooth

# Define the Bluetooth service UUID and characteristic UUID
SERVICE_UUID = bluetooth.UUID('0000180d-0000-1000-8000-00805f9b34fb')
CHARACTERISTIC_UUID = bluetooth.UUID('00002a37-0000-1000-8000-00805f9b34fb')

# Callback function for receiving data
def receive_data(data):
    print('Received:', data.decode())

# Initialize the Bluetooth adapter
bluetooth.init()

# Create a BLE peripheral instance
peripheral = bluetooth.BLEPeripheral()

# Create a BLE service and characteristic
service = peripheral.add_service(SERVICE_UUID)
characteristic = service.add_characteristic(CHARACTERISTIC_UUID, bluetooth.FLAG_READ | bluetooth.FLAG_NOTIFY)

# Set the callback function for receiving data
characteristic.set_callback(receive_data)

# Start advertising the BLE service
peripheral.start_advertising()

while True:
    # Process any pending Bluetooth events
    bluetooth.process()

