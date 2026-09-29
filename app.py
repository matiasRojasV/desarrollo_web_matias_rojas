from flask import Flask, render_template, request, jsonify, redirect, url_for, session
from db import *
from datetime import datetime
from werkzeug.utils import secure_filename
import os
import re

app = Flask(__name__)

#Configuración para usar sesiones en Flask
app.secret_key = 'clave_secreta_super_segura_para_sesiones'

app.config['UPLOAD_FOLDER'] = 'static/uploads'
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)


@app.teardown_appcontext
def shutdown_session(exception=None):
    db_session.remove()


# RUTAS DE LA APP
@app.route('/')
def index():
    ultimos2 = obtener_ultimos_avistamientos(limit=2)

    exito = request.args.get('exito')
    mensaje_exito = None
    if exito == 'avistamiento':
        mensaje_exito = "¡El avistamiento se ha registrado exitosamente!"

    return render_template('index.html', ultimos_avistamientos=ultimos2, mensaje_exito=mensaje_exito)

    
# Registrar Voluntario
@app.route('/login', methods=['GET', 'POST'])
def login():
    # Detectar si viene por falta de sesión
    alerta_sesion = request.args.get('alerta')
    errores = []

    if alerta_sesion == 'requerido':
        errores.append("Debes registrarte o iniciar sesión para poder informar un avistamiento.")

    if request.method == 'POST':
        nombre = request.form.get('nombre', '').strip()
        email = request.form.get('email', '').strip()
        celular = request.form.get('celular', '').strip()
        region_id = request.form.get('region', '').strip()
        comuna_id = request.form.get('comuna', '').strip()

        # Validaciones
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
            comuna_obj = obtener_comuna_por_id(comuna_id)
            if not comuna_obj:
                errores.append("La comuna seleccionada no es válida.")
        
        if errores:
            return render_template('login.html', regiones=obtener_regiones(), errores=errores)

        try:
            nuevo_voluntario = crear_voluntario(nombre, email, celular, comuna_id)
            session['voluntario_id'] = nuevo_voluntario.id
            session['voluntario_nombre'] = nuevo_voluntario.nombre
            
            return render_template('exitoLogin.html', nombre=nuevo_voluntario.nombre)
        
        except Exception:
            return render_template('login.html', regiones=obtener_regiones(), errores=errores)

    # GET
    return render_template('login.html', regiones=obtener_regiones(), errores=errores)


@app.route('/get_comunas/<int:region_id>')
def get_comunas(region_id):
    comunas = obtener_comunas_por_region(region_id)
    comunas_json = [{'id': c.id, 'nombre': c.nombre} for c in comunas]
    return jsonify({'comunas': comunas_json})


@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('index'))


@app.route('/avistamiento', methods=['GET', 'POST'])
def avistamiento():
    # Validar que exista una sesión activa
    voluntario_id = session.get('voluntario_id')
    if not voluntario_id:
        return redirect(url_for('login', alerta='requerido'))

    if request.method == 'POST':
        ave_id = request.form.get('ave_id') 
        lugar = request.form.get('lugar', '').strip()
        descripcion = request.form.get('descripcion', '').strip()
        fecha = request.form.get('fecha', '').strip()
        hora = request.form.get('hora', '').strip()
        archivos = request.files.getlist('multimedia')

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
        
        if not archivos or archivos[0].filename == '':
            errores.append("Debe adjuntar al menos foto o video del avistamiento.")

        if errores:
            return render_template('avistamiento.html', errores=errores, aves=obtener_aves())

        nombres_guardados = []
        try:
            for archivo in archivos:
                if archivo and archivo.filename != '':
                    nombre_seguro = secure_filename(archivo.filename)        
                    ruta_guardado = os.path.join(app.config['UPLOAD_FOLDER'], nombre_seguro)
                    archivo.save(ruta_guardado)
                    nombres_guardados.append(nombre_seguro)

            crear_avistamiento(
                fecha_hora=fecha_hora,
                lugar=lugar,
                descripcion=descripcion,
                ave_id=ave_id,
                voluntario_id=voluntario_id,
                nombre_archivo=nombres_guardados
            )
            return redirect(url_for('index', exito='avistamiento'))
            
        except Exception as e:
            for nombre in nombres_guardados:
                ruta_error = os.path.join(app.config['UPLOAD_FOLDER'], nombre)
                if os.path.exists(ruta_error):
                    os.remove(ruta_error)

            return render_template('avistamiento.html', errores=["Error interno al guardar el avistamiento en la base de datos."], aves=obtener_aves())

    # GET
    return render_template('avistamiento.html', aves=obtener_aves())


@app.route('/avistamiento/<int:id>')
def detalleAvistamiento(id):
    avistamiento = obtener_avistamiento_por_id(id)
    
    # Manejo de la respuesta HTTP
    if not avistamiento:
        return render_template('404.html'), 404
        
    return render_template('detalleAvistamiento.html', avistamiento=avistamiento)


@app.route('/listAvistamientos')
def listAvistamientos():
    page = request.args.get('page', 1, type=int)
    orden = request.args.get('orden', 'fecha-desc')

    avistamientos = obtener_avistamientos_paginados(page=page, per_page=4, orden=orden)
    return render_template('listAvistamientos.html', avistamientos=avistamientos, orden_actual=orden)


@app.route('/dashboard')
def dashboard():
    return render_template('dashboard.html')


if __name__ == '__main__':
    app.run(debug=True)