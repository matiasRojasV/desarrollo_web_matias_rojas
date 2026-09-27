from flask import Flask, render_template, request, jsonify, redirect, url_for
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from werkzeug.utils import secure_filename
import os
import re

app = Flask(__name__)

# Configuración de base de datos (Docker MySQL)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://cc5002:programacionweb@localhost:3306/tarea2?charset=utf8mb4'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Configuración para guardar archivos subidos
app.config['UPLOAD_FOLDER'] = 'static/uploads'
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

# init BD
db = SQLAlchemy(app)


# Definición de Modelos
class Voluntario(db.Model):
    __tablename__ = 'voluntario'
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(255), nullable=False)
    email = db.Column(db.String(80), nullable=False)
    telefono = db.Column(db.String(15), nullable=False)
    fecha_registro = db.Column(db.DateTime, nullable=False)
    comuna_id = db.Column(db.Integer, db.ForeignKey('comuna.id'), nullable=False)


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


class Ave(db.Model):
    __tablename__ = 'ave'
    id = db.Column(db.Integer, primary_key=True)
    nombre = db.Column(db.String(100), nullable=False)


class Avistamiento(db.Model):
    __tablename__ = 'avistamiento'
    id = db.Column(db.Integer, primary_key=True)
    fecha_hora = db.Column(db.DateTime, nullable=False)
    lugar = db.Column(db.String(200), nullable=False)
    descripcion = db.Column(db.Text)
    
    # Claves foráneas
    ave_id = db.Column(db.Integer, db.ForeignKey('ave.id'), nullable=False)
    voluntario_id = db.Column(db.Integer, db.ForeignKey('voluntario.id'), nullable=False)
    
    # Relaciones
    ave = db.relationship('Ave', backref='avistamientos')
    voluntario = db.relationship('Voluntario', backref='avistamientos')


class Registro(db.Model):
    __tablename__ = 'registro'
    id = db.Column(db.Integer, primary_key=True)
    ruta_archivo = db.Column(db.String(300), nullable=False)
    nombre_archivo = db.Column(db.String(300), nullable=False)
    avistamiento_id = db.Column(db.Integer, db.ForeignKey('avistamiento.id'), nullable=False)
    
    # Relación inversa para poder sacar la foto desde el avistamiento
    avistamiento = db.relationship('Avistamiento', backref=db.backref('registros', lazy=True))



# RUTAS DE LA APLICACIÓN
# Portada
@app.route('/')
def index():
    ultimos2 = Avistamiento.query.order_by(Avistamiento.id.desc()).limit(2).all()
    return render_template('index.html', ultimos_avistamientos =ultimos2)

    
# Registrar Voluntario
@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        # 2.1. Recibir datos del formulario
        nombre = request.form.get('nombre', '').strip()
        email = request.form.get('email', '').strip()
        celular = request.form.get('celular', '').strip()
        region_id = request.form.get('region', '').strip()
        comuna_id = request.form.get('comuna', '').strip()

        # 2.2. Validaciones obligatorias en el Servidor
        errores = []

        if not nombre or len(nombre) < 3:
            errores.append("El nombre completo debe tener al menos 3 caracteres.")
        
        regex_email = r'^[^\s@]+@[^\s@]+\.[^\s@]+$'
        if not email or not re.match(regex_email, email):
            errores.append("Debe ingresar un correo electrónico válido.")

        regex_celular = r'^(\+?56)?9\d{8}$'
        if not celular or not re.match(regex_celular, celular):
            errores.append("El celular debe ser un número válido de Chile (ej: +56912345678 o 912345678).")

        if not region_id:
            errores.append("Debe seleccionar una región.")

        if not comuna_id:
            errores.append("Debe seleccionar una comuna.")
        else:
            # Validar que la comuna exista en la BD
            comuna_obj = Comuna.query.get(comuna_id)
            if not comuna_obj:
                errores.append("La comuna seleccionada no es válida.")

        # 2.3. Si hay errores de validación, recargamos con los mensajes
        if errores:
            regiones_db = Region.query.all()
            return render_template('login.html', regiones=regiones_db, errores=errores)

        # 2.4. Guardar voluntario en la Base de Datos según el esquema exacto
        nuevo_voluntario = Voluntario(
            nombre=nombre,
            email=email,
            telefono=celular,
            fecha_registro=datetime.now(),
            comuna_id=int(comuna_id)
        )

        try:
            db.session.add(nuevo_voluntario)
            db.session.commit()
            return redirect(url_for('index'))
        
        except Exception as e:
            db.session.rollback()
            regiones_db = Region.query.all()
            return render_template('login.html', regiones=regiones_db, errores=["Error interno al guardar en la base de datos."])

    # Si la petición es GET
    regiones_db = Region.query.all()
    return render_template('login.html', regiones=regiones_db)


# Obtener comunas de la region
@app.route('/get_comunas/<int:region_id>')
def get_comunas(region_id):
    # Consulta a la base de datos: Comunas donde el region_id coincida
    comunas = Comuna.query.filter_by(region_id=region_id).all()
    
    # Convertimos los objetos a un formato JSON
    comunas_json = [{'id': c.id, 'nombre': c.nombre} for c in comunas]
    return jsonify({'comunas': comunas_json})


# Registrar Avistamiento
@app.route('/avistamiento', methods=['GET', 'POST'])
def avistamiento():
    if request.method == 'POST':
        # 1. Obtener campos del formulario
        ave_id = request.form.get('ave_id') 
        lugar = request.form.get('lugar', '').strip()
        descripcion = request.form.get('descripcion', '').strip()
        fecha = request.form.get('fecha', '').strip()
        hora = request.form.get('hora', '').strip()
        archivo = request.files.get('multimedia')

        # 2. Validaciones en el Servidor
        errores = []

        if not ave_id:
            errores.append("Debe seleccionar un ave de la lista.")

        if not lugar:
            errores.append("El lugar del avistamiento es obligatorio.")

        if not fecha or not hora:
            errores.append("Debe seleccionar tanto la fecha como la hora del avistamiento.")

        fecha_hora = None
        if fecha and hora:
            try:
                fecha_hora = datetime.strptime(f"{fecha} {hora}", "%Y-%m-%d %H:%M")
            except ValueError:
                errores.append("El formato de fecha u hora no es válido.")

        if not archivo or archivo.filename == '':
            errores.append("Debe adjuntar una foto o video del avistamiento.")

        if errores:
            aves_db = Ave.query.all()
            return render_template('avistamiento.html', errores=errores, aves=aves_db)

        voluntario_obj = Voluntario.query.order_by(Voluntario.id.desc()).first()
        if not voluntario_obj:
            aves_db = Ave.query.all()
            return render_template('avistamiento.html', errores=["Debe registrar al menos un voluntario antes de reportar un avistamiento."], aves=aves_db)

        # Crear el avistamiento según las columnas de la BD
        nuevo_avistamiento = Avistamiento(
            fecha_hora=fecha_hora,
            lugar=lugar,
            descripcion=descripcion,
            ave_id=int(ave_id),
            voluntario_id=voluntario_obj.id
        )
        db.session.add(nuevo_avistamiento)
        db.session.flush()

        # Guardar archivo e insertar registro
        if archivo:
            nombre_seguro = secure_filename(archivo.filename)
            ruta_guardado = os.path.join(app.config['UPLOAD_FOLDER'], nombre_seguro)
            archivo.save(ruta_guardado)

            nuevo_registro = Registro(
                ruta_archivo=f"static/uploads/{nombre_seguro}",
                nombre_archivo=nombre_seguro,
                avistamiento_id=nuevo_avistamiento.id
            )
            db.session.add(nuevo_registro)

        db.session.commit()
        return redirect(url_for('index'))

    # Método GET
    aves_db = Ave.query.all()
    return render_template('avistamiento.html', aves=aves_db)  


# Lista de avistamientos
@app.route('/listAvistamientos')
def listAvistamientos():
    # Obtener todos los avistamientos ordenados por fecha descendente
    todos_los_avistamientos = Avistamiento.query.order_by(Avistamiento.fecha_hora.desc()).all()
    return render_template('listAvistamientos.html', avistamientos=todos_los_avistamientos)


# 4. Métricas (Dashboard)
@app.route('/dashboard')
def dashboard():
    return render_template('dashboard.html')

if __name__ == '__main__':
    app.run(debug=True)