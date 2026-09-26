from flask import Flask, render_template, request, redirect, url_for
from flask_sqlalchemy import SQLAlchemy
import os

app = Flask(__name__)

# Configuración de base de datos
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://cc5002:programacionweb@localhost:3306/tarea2'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Configuración para guardar archivos subidos
app.config['UPLOAD_FOLDER'] = 'static/uploads'

db = SQLAlchemy(app)

# MODELOS DE CLASES (Region, Comuna, Voluntario, etc.)

# RUTAS DE LA APLICACIÓN

# 1. Portada
@app.route('/')
def index():
    # consultar los últimos 2 avistamientos de la BD
    return render_template('index.html')

# 2. Registrar Voluntario
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        # recibir los datos del formulario, validar en el servidor y guardar en BD
        pass 
    return render_template('login.html')

# 3. Registrar Avistamiento
@app.route('/avistamiento', methods=['GET', 'POST'])
def avistamiento():
    if request.method == 'POST':
        # recibir los datos, validarás en el servidor, guardarás la imagen en UPLOAD_FOLDER y los datos en la BD
        pass
    return render_template('avistamiento.html')

# 4. Métricas (Dashboard)
@app.route('/dashboard')
def dashboard():
    return render_template('dashboard.html')

if __name__ == '__main__':
    app.run(debug=True)