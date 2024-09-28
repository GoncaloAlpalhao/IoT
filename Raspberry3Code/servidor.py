from flask import Flask, render_template, request
import subprocess
import time

# Create the Flask app object
app = Flask(__name__)

# Define the route for the index page
@app.route('/')

# 
# Function to render the index page
#
def index():
	return render_template('configurar_rede.html')
	

# Define the route for the configure network page and the method to handle the POST request
@app.route('/configurar-rede', methods=['POST'])

#
# Function to configure the network
#
def configurar_rede():
    
    try:
        # Get the values from the form
        ssid = request.form['ssid']
        senha = request.form['senha']
        planta = request.form['planta']
        tipo = request.form['tipo']
        
        # Check if the values are not empty
        if not ssid.strip() or not senha.strip() or not planta.strip():
            return 'Erro: todos os campos são obrigatórios.'
			
        # Check if the plant ID is a number
        if not planta.isdigit():
            return 'Erro: o ID da planta deve ser um número.'
	
        # Creates the network block
        network_block = f'\nnetwork={{\n  ssid="{ssid}"\n  psk="{senha}"\n}}\n'
        
        # Appends the block to the wpa_supplicant.conf file to connect to the network
        subprocess.run(['sudo', 'bash', '-c', f'echo \'{network_block}\' >> /etc/wpa_supplicant/wpa_supplicant.conf'], check=True)
            
        # Saves the plant ID to a file
        subprocess.run([f'echo "{planta}" >> /home/pico/PlantaID'],shell=True, check=True)
        
        # Saves the plant Type to a file
        subprocess.run([f'echo "{tipo}" >> /home/pico/PlantaTipo'],shell=True, check=True)
        
        # Disables the AP mode
        subprocess.run(['sudo', 'systemctl', 'stop', 'hostapd'], check=True)
        subprocess.run(['sudo', 'systemctl', 'stop', 'dnsmasq'], check=True)
        
        # Restarts the wpa_supplicant service
        subprocess.run(['sudo', 'systemctl', 'restart', 'wpa_supplicant'], check=True)
        
        # Checks if the wlan0 is up and if not brings it
        subprocess.run(['sudo', 'ifconfig', 'wlan0', 'up'], check=True)
        
        # Waits for a few seconds to allow connection
        time.sleep(10)
        
        # Check if the wlan0 interface has an IP address
        result = subprocess.run(['hostname', '-I'], capture_output=True, text=True)
        ip_addresses = result.stdout.strip().split()
        wlan0_ip = next((ip for ip in ip_addresses if ip.startswith('192.')), None)
        
        if wlan0_ip:
            # If connected, return the success message
            return f'Rede configurada com sucesso! Conectado à rede {ssid} com IP {wlan0_ip}'
        else:
            # If not connected, re-enable AP mode
            subprocess.run(['sudo', 'systemctl', 'start', 'hostapd'], check=True)
            subprocess.run(['sudo', 'systemctl', 'start', 'dnsmasq'], check=True)
            return 'Falha ao conectar à rede. Modo AP reactivado.'

    # If an error occurs, return the error message   
    except Exception as e:
        return f"Erro: {e}"

# Run the app on the local development server
if __name__ == '__main__':
	app.run(host='0.0.0.0', port=5000, debug=True)
