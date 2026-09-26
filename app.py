from flask import Flask, render_template, request, redirect, url_for
from flask_sqlalchemy import SQLAlchemy
import os

app = Flask(__name__)

# Configuración de base de datos (Docker MySQL)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://cc5002:programacionweb@localhost:3306/tarea2?charset=utf8mb4'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Configuración para guardar archivos subidos
app.config['UPLOAD_FOLDER'] = 'static/uploads'

# init BD
db = SQLAlchemy(app)

# Definición de Modelos
class Region(db.Model):
    __tablename__ = 'region'
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(200), nullable=False)

class Comuna(db.Model):
    __tablename__ = 'comuna'
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(200), nullable=False)
    region_id = db.Column(db.Integer, db.ForeignKey('region.id'), nullable=False)
    # Relación para acceder desde una comuna a su región
    region = db.relationship('Region', backref=db.backref('comunas', lazy=True))


# RUTAS DE LA APLICACIÓN

# 1. Portada
@app.route('/')
def index():
    return render_template('index.html')

    
# 2. Registrar Voluntario
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        # recibir los datos del formulario, validar en el servidor y guardar en BD
        pass 

    regiones_db = Region.query.all()

    return render_template('login.html', regiones=regiones_db)


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