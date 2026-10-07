from sqlalchemy import create_engine, Column, Integer, String, DateTime, Text, ForeignKey, func
from sqlalchemy.orm import declarative_base, sessionmaker, relationship, scoped_session
from datetime import datetime

# Configuración de conexión con MySQL
DATABASE_URL = 'mysql+pymysql://cc5002:programacionweb@localhost:3306/tarea2?charset=utf8mb4'

engine = create_engine(DATABASE_URL, echo=False)
db_session = scoped_session(sessionmaker(autocommit=False, autoflush=False, bind=engine))

Base = declarative_base()


# Modelos
class Voluntario(Base):
    __tablename__ = 'voluntario'
    id = Column(Integer, primary_key=True)
    nombre = Column(String(255), nullable=False)
    email = Column(String(80), nullable=False)
    telefono = Column(String(15), nullable=False)
    fecha_registro = Column(DateTime, nullable=False)
    comuna_id = Column(Integer, ForeignKey('comuna.id'), nullable=False)


class Region(Base):
    __tablename__ = 'region'
    id = Column(Integer, primary_key=True)
    nombre = Column(String(200), nullable=False)


class Comuna(Base):
    __tablename__ = 'comuna'
    id = Column(Integer, primary_key=True)
    nombre = Column(String(200), nullable=False)
    region_id = Column(Integer, ForeignKey('region.id'), nullable=False)

    region = relationship('Region', backref='comunas')


class Ave(Base):
    __tablename__ = 'ave'
    id = Column(Integer, primary_key=True)
    nombre = Column(String(80), nullable=False)


class Avistamiento(Base):
    __tablename__ = 'avistamiento'
    id = Column(Integer, primary_key=True)
    fecha_hora = Column(DateTime, nullable=False)
    lugar = Column(String(200), nullable=False)
    descripcion = Column(Text(500))
    
    ave_id = Column(Integer, ForeignKey('ave.id'), nullable=False)
    voluntario_id = Column(Integer, ForeignKey('voluntario.id'), nullable=False)
    
    ave = relationship('Ave', backref='avistamientos')
    voluntario = relationship('Voluntario', backref='avistamientos')


class Registro(Base):
    __tablename__ = 'registro'
    id = Column(Integer, primary_key=True)
    ruta_archivo = Column(String(300), nullable=False)
    nombre_archivo = Column(String(300), nullable=False)
    avistamiento_id = Column(Integer, ForeignKey('avistamiento.id'), nullable=False)
    
    avistamiento = relationship('Avistamiento', backref='registros')


# Paginador
class Paginador:
    def __init__(self, query, page, per_page):
        total = query.count()
        self.items = query.offset((page - 1) * per_page).limit(per_page).all()
        self.page = page
        self.pages = (total + per_page - 1) // per_page if total > 0 else 1
        self.has_prev = page > 1
        self.has_next = page < self.pages
        self.prev_num = page - 1
        self.next_num = page + 1


# Funciones lecturas
def obtener_ultimos_avistamientos(limit=2):
    return db_session.query(Avistamiento).order_by(Avistamiento.id.desc()).limit(limit).all()


def obtener_regiones():
    return db_session.query(Region).all()


def obtener_comuna_por_id(comuna_id):
    return db_session.get(Comuna, comuna_id)


def obtener_comunas_por_region(region_id):
    return db_session.query(Comuna).filter_by(region_id=region_id).all()


def obtener_aves():
    return db_session.query(Ave).all()


def obtener_avistamiento_por_id(avistamiento_id):
    return db_session.get(Avistamiento, avistamiento_id)


def obtener_avistamientos_paginados(page=1, per_page=4, orden='fecha-desc'):
    query = db_session.query(Avistamiento)

    if orden == 'fecha-asc':
        query = query.order_by(Avistamiento.fecha_hora.asc())
    elif orden == 'lugar':
        query = query.order_by(Avistamiento.lugar.asc())
    elif orden == 'nombre':
        query = query.join(Ave).order_by(Ave.nombre.asc())
    else:
        query = query.order_by(Avistamiento.fecha_hora.desc())

    return Paginador(query, page=page, per_page=per_page)


def obtener_avistamientos_por_dia():
    # Extraemos solo la fecha
    return db_session.query(
        func.date(Avistamiento.fecha_hora).label('fecha'), 
        func.count(Avistamiento.id)
    ).group_by(func.date(Avistamiento.fecha_hora)).order_by('fecha').all()


def obtener_avistamientos_por_ave():
    return db_session.query(
        Ave.nombre, 
        func.count(Avistamiento.id)
    ).join(Avistamiento).group_by(Ave.nombre).all()


def obtener_voluntarios_por_comuna():
    return db_session.query(
        Comuna.nombre, 
        func.count(Voluntario.id)
    ).join(Voluntario).group_by(Comuna.nombre).all()


# Funciones escritura
def crear_voluntario(nombre, email, telefono, comuna_id):
    """Crea un voluntario en la base de datos"""
    try:
        nuevo_voluntario = Voluntario(
            nombre=nombre,
            email=email,
            telefono=telefono,
            fecha_registro=datetime.now(),
            comuna_id=int(comuna_id)
        )
        db_session.add(nuevo_voluntario)
        db_session.commit()
        return nuevo_voluntario
    except Exception as e:
        db_session.rollback()
        raise e


def crear_avistamiento(fecha_hora, lugar, descripcion, ave_id, voluntario_id, nombres_archivos):
    """Crea un avistamiento y su registro multimedia asociado."""
    try:
        # Crear el avistamiento
        nuevo_avistamiento = Avistamiento(
            fecha_hora=fecha_hora,
            lugar=lugar,
            descripcion=descripcion,
            ave_id=ave_id,
            voluntario_id=voluntario_id
        )
        db_session.add(nuevo_avistamiento)

        db_session.flush() # Guardamos para generar el avistamiento.id

        # Nuevo registro multimedia por cada archivo
        for nombre in nombres_archivos:
            ruta_relativa = f"uploads/{nombre}"
            nuevo_registro = Registro(
                nombre_archivo=nombre,
                ruta_archivo=ruta_relativa, 
                avistamiento_id=nuevo_avistamiento.id
            )
            db_session.add(nuevo_registro)

        # Confirmar cambios
        db_session.commit()

        return nuevo_avistamiento
        
    except Exception as e:
        db_session.rollback()
        raise e
