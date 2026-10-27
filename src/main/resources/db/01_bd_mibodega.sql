-- =========================================================
-- Mi Bodega - Base de datos
-- Curso Integrador I: Sistemas - Software (UTP)
-- Motor: MySQL 8
-- =========================================================

DROP DATABASE IF EXISTS bd_mibodega;
CREATE DATABASE bd_mibodega CHARACTER SET utf8mb4;
USE bd_mibodega;

-- ---------------------------------------------------------
-- ORGANIZACION
-- ---------------------------------------------------------

CREATE TABLE empresas (
  id_empresa       INT AUTO_INCREMENT PRIMARY KEY,
  ruc              CHAR(11)     NOT NULL UNIQUE,
  razon_social     VARCHAR(150) NOT NULL,
  nombre_comercial VARCHAR(100) NULL,
  direccion        VARCHAR(200) NULL,
  rubro            VARCHAR(60)  NULL,
  telefono         VARCHAR(20)  NULL,
  correo           VARCHAR(100) NULL,
  plan             VARCHAR(30)  NOT NULL DEFAULT 'Estándar',
  estado           ENUM('ACTIVA','EN PRUEBA','SUSPENDIDA','INACTIVA') NOT NULL DEFAULT 'EN PRUEBA',
  logo             MEDIUMBLOB   NULL,
  logo_tipo        VARCHAR(40)  NULL,
  fecha_registro   DATE         NOT NULL
);

CREATE TABLE sedes (
  id_sede        INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa     INT          NOT NULL,
  codigo         VARCHAR(10)  NOT NULL,
  nombre         VARCHAR(100) NOT NULL,
  direccion      VARCHAR(200) NOT NULL,
  distrito       VARCHAR(60)  NOT NULL,
  provincia      VARCHAR(60)  NOT NULL DEFAULT 'Lima',
  responsable    VARCHAR(100) NULL,
  telefono       VARCHAR(20)  NULL,
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  fecha_creacion DATE         NOT NULL,
  UNIQUE (id_empresa, codigo),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

-- ---------------------------------------------------------
-- SEGURIDAD
-- ---------------------------------------------------------

-- Catalogo de permisos con formato modulo.accion
CREATE TABLE permisos (
  id_permiso  INT AUTO_INCREMENT PRIMARY KEY,
  modulo      VARCHAR(40)  NOT NULL,
  accion      VARCHAR(40)  NOT NULL,
  ambito      ENUM('EMPRESA','PLATAFORMA') NOT NULL,
  descripcion VARCHAR(120) NOT NULL,
  UNIQUE (modulo, accion)
);

CREATE TABLE roles (
  id_rol     INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa INT         NOT NULL,
  nombre     VARCHAR(60) NOT NULL,
  activo     BOOLEAN     NOT NULL DEFAULT TRUE,
  UNIQUE (id_empresa, nombre),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

CREATE TABLE rol_permisos (
  id_rol     INT NOT NULL,
  id_permiso INT NOT NULL,
  PRIMARY KEY (id_rol, id_permiso),
  FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE,
  FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso)
);

CREATE TABLE usuarios (
  id_usuario         INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa         INT          NOT NULL,
  id_sede            INT          NOT NULL,
  usuario            VARCHAR(40)  NOT NULL UNIQUE,
  clave_hash         CHAR(60)     NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  estado             ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  debe_cambiar_clave BOOLEAN      NOT NULL DEFAULT FALSE,
  id_rol_activo      INT          NULL,
  tema               VARCHAR(10)  NOT NULL DEFAULT 'claro',
  ultimo_acceso      DATETIME     NULL,
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede),
  FOREIGN KEY (id_rol_activo) REFERENCES roles(id_rol)
);

-- Un usuario puede tener varios roles y trabaja con uno activo
CREATE TABLE usuario_roles (
  id_usuario INT NOT NULL,
  id_rol     INT NOT NULL,
  PRIMARY KEY (id_usuario, id_rol),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
  FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- Equipo interno de la plataforma (no pertenece a una empresa)
CREATE TABLE roles_internos (
  id_rol_interno INT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(60) NOT NULL UNIQUE,
  principal      BOOLEAN     NOT NULL DEFAULT FALSE,
  activo         BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE rol_interno_permisos (
  id_rol_interno INT NOT NULL,
  id_permiso     INT NOT NULL,
  PRIMARY KEY (id_rol_interno, id_permiso),
  FOREIGN KEY (id_rol_interno) REFERENCES roles_internos(id_rol_interno) ON DELETE CASCADE,
  FOREIGN KEY (id_permiso) REFERENCES permisos(id_permiso)
);

CREATE TABLE usuarios_internos (
  id_usuario_interno INT AUTO_INCREMENT PRIMARY KEY,
  id_rol_interno     INT          NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  correo             VARCHAR(100) NOT NULL,
  usuario            VARCHAR(40)  NOT NULL UNIQUE,
  clave_hash         CHAR(60)     NOT NULL,
  estado             ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  debe_cambiar_clave BOOLEAN      NOT NULL DEFAULT FALSE,
  tema               VARCHAR(10)  NOT NULL DEFAULT 'claro',
  ultimo_acceso      DATETIME     NULL,
  FOREIGN KEY (id_rol_interno) REFERENCES roles_internos(id_rol_interno)
);

-- ---------------------------------------------------------
-- COMERCIAL
-- ---------------------------------------------------------

CREATE TABLE categorias (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa   INT         NOT NULL,
  nombre       VARCHAR(60) NOT NULL,
  activo       BOOLEAN     NOT NULL DEFAULT TRUE,
  UNIQUE (id_empresa, nombre),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

-- El producto no guarda stock: el stock esta en los lotes
CREATE TABLE productos (
  id_producto          INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa           INT           NOT NULL,
  id_categoria         INT           NOT NULL,
  codigo_barras        VARCHAR(20)   NOT NULL,
  nombre               VARCHAR(120)  NOT NULL,
  marca                VARCHAR(60)   NULL,
  descripcion          VARCHAR(200)  NULL,
  presentacion         VARCHAR(60)   NULL,
  unidad               VARCHAR(20)   NOT NULL DEFAULT 'unidad',
  empaque              VARCHAR(20)   NOT NULL DEFAULT 'Unidad',
  unidades_por_empaque INT           NOT NULL DEFAULT 1,
  precio_compra        DECIMAL(10,2) NOT NULL DEFAULT 0,
  precio_venta         DECIMAL(10,2) NOT NULL,
  stock_minimo         INT           NOT NULL DEFAULT 0,
  activo               BOOLEAN       NOT NULL DEFAULT TRUE,
  UNIQUE (id_empresa, codigo_barras),
  CHECK (precio_venta >= 0),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE proveedores (
  id_proveedor     INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa       INT          NOT NULL,
  tipo_doc         ENUM('RUC','DNI') NOT NULL DEFAULT 'RUC',
  documento        VARCHAR(11)  NOT NULL,
  razon_social     VARCHAR(150) NOT NULL,
  nombre_comercial VARCHAR(100) NULL,
  contacto         VARCHAR(100) NULL,
  telefono         VARCHAR(20)  NULL,
  correo           VARCHAR(100) NULL,
  direccion        VARCHAR(200) NULL,
  observaciones    VARCHAR(200) NULL,
  activo           BOOLEAN      NOT NULL DEFAULT TRUE,
  UNIQUE (id_empresa, documento),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

CREATE TABLE clientes (
  id_cliente     INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa     INT          NOT NULL,
  tipo_doc       ENUM('DNI','RUC') NOT NULL DEFAULT 'DNI',
  documento      VARCHAR(11)  NOT NULL,
  nombre         VARCHAR(150) NOT NULL,
  telefono       VARCHAR(20)  NULL,
  correo         VARCHAR(100) NULL,
  direccion      VARCHAR(200) NULL,
  fecha_registro DATE         NOT NULL,
  UNIQUE (id_empresa, documento),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

-- Ultimo numero usado por serie: GR (recepciones) y B001 (boletas)
CREATE TABLE correlativos (
  id_empresa INT        NOT NULL,
  serie      VARCHAR(5) NOT NULL,
  ultimo     INT        NOT NULL DEFAULT 0,
  PRIMARY KEY (id_empresa, serie),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

-- ---------------------------------------------------------
-- INVENTARIO
-- ---------------------------------------------------------

CREATE TABLE recepciones (
  id_recepcion       INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa         INT          NOT NULL,
  id_sede            INT          NOT NULL,
  id_proveedor       INT          NOT NULL,
  numero             VARCHAR(12)  NOT NULL,
  guia_proveedor     VARCHAR(30)  NOT NULL,
  fecha_hora         DATETIME     NOT NULL,
  estado             ENUM('BORRADOR','CONFIRMADA','MODIFICADA','ANULADA') NOT NULL DEFAULT 'BORRADOR',
  observaciones      VARCHAR(300) NULL,
  id_usuario         INT          NULL,
  id_usuario_interno INT          NULL,
  UNIQUE (id_empresa, numero),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede),
  FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  FOREIGN KEY (id_usuario_interno) REFERENCES usuarios_internos(id_usuario_interno)
);

-- Stock por sede y fecha de vencimiento
CREATE TABLE lotes (
  id_lote            INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa         INT           NOT NULL,
  id_sede            INT           NOT NULL,
  id_producto        INT           NOT NULL,
  id_recepcion       INT           NULL,
  cantidad_ingresada INT           NOT NULL,
  cantidad_actual    INT           NOT NULL,
  costo_unitario     DECIMAL(10,4) NOT NULL,
  fecha_vencimiento  DATE          NULL,
  CHECK (cantidad_actual >= 0),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_recepcion) REFERENCES recepciones(id_recepcion)
);

CREATE TABLE recepcion_detalles (
  id_detalle        INT AUTO_INCREMENT PRIMARY KEY,
  id_recepcion      INT           NOT NULL,
  id_producto       INT           NOT NULL,
  empaque           VARCHAR(20)   NOT NULL,
  contenido         INT           NOT NULL,
  cantidad_empaques INT           NOT NULL,
  total_unidades    INT           NOT NULL,
  costo_unitario    DECIMAL(10,4) NOT NULL,
  fecha_vencimiento DATE          NULL,
  id_lote           INT           NULL,
  FOREIGN KEY (id_recepcion) REFERENCES recepciones(id_recepcion) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);

-- Historial de correcciones de una recepcion confirmada
CREATE TABLE recepcion_modificaciones (
  id_modificacion INT AUTO_INCREMENT PRIMARY KEY,
  id_recepcion    INT          NOT NULL,
  fecha_hora      DATETIME     NOT NULL,
  usuario_nombre  VARCHAR(100) NOT NULL,
  rol_nombre      VARCHAR(60)  NOT NULL,
  motivo          VARCHAR(300) NOT NULL,
  cambios         TEXT         NOT NULL,
  FOREIGN KEY (id_recepcion) REFERENCES recepciones(id_recepcion)
);

-- ---------------------------------------------------------
-- VENTAS
-- ---------------------------------------------------------

CREATE TABLE ventas (
  id_venta           INT AUTO_INCREMENT PRIMARY KEY,
  id_empresa         INT           NOT NULL,
  id_sede            INT           NOT NULL,
  id_cliente         INT           NULL,
  id_usuario         INT           NULL,
  id_usuario_interno INT           NULL,
  numero_comprobante VARCHAR(15)   NOT NULL,
  fecha_hora         DATETIME      NOT NULL,
  metodo_pago        ENUM('EFECTIVO','YAPE','TARJETA','TRANSFERENCIA') NOT NULL,
  total              DECIMAL(10,2) NOT NULL,
  monto_recibido     DECIMAL(10,2) NULL,
  vuelto             DECIMAL(10,2) NULL,
  estado             ENUM('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA',
  motivo_anulacion   VARCHAR(200)  NULL,
  UNIQUE (id_empresa, numero_comprobante),
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede),
  FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  FOREIGN KEY (id_usuario_interno) REFERENCES usuarios_internos(id_usuario_interno)
);

CREATE TABLE venta_detalles (
  id_detalle      INT AUTO_INCREMENT PRIMARY KEY,
  id_venta        INT           NOT NULL,
  id_producto     INT           NOT NULL,
  cantidad        INT           NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal        DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- De que lotes salio cada linea vendida (FEFO)
CREATE TABLE venta_detalle_lotes (
  id_detalle INT NOT NULL,
  id_lote    INT NOT NULL,
  cantidad   INT NOT NULL,
  PRIMARY KEY (id_detalle, id_lote),
  FOREIGN KEY (id_detalle) REFERENCES venta_detalles(id_detalle),
  FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);

-- Movimientos de inventario. En AJUSTE la cantidad lleva signo.
CREATE TABLE kardex (
  id_movimiento      BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_empresa         INT           NOT NULL,
  id_sede            INT           NOT NULL,
  id_producto        INT           NOT NULL,
  id_lote            INT           NOT NULL,
  tipo               ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
  cantidad           INT           NOT NULL,
  costo_unitario     DECIMAL(10,4) NOT NULL,
  documento          VARCHAR(120)  NOT NULL,
  id_venta           INT           NULL,
  id_recepcion       INT           NULL,
  id_proveedor       INT           NULL,
  id_usuario         INT           NULL,
  id_usuario_interno INT           NULL,
  fecha_hora         DATETIME      NOT NULL,
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_lote) REFERENCES lotes(id_lote),
  FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
  FOREIGN KEY (id_recepcion) REFERENCES recepciones(id_recepcion),
  FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
  FOREIGN KEY (id_usuario_interno) REFERENCES usuarios_internos(id_usuario_interno)
);

-- Diseno de la boleta (una fila por empresa)
CREATE TABLE config_comprobantes (
  id_empresa           INT          PRIMARY KEY,
  plantilla            ENUM('CLASICO','MODERNO','COMPACTO') NOT NULL DEFAULT 'CLASICO',
  ancho                ENUM('58','80') NOT NULL DEFAULT '80',
  encabezado           VARCHAR(100) NULL,
  pie                  VARCHAR(100) NULL,
  mensaje              VARCHAR(150) NULL,
  notas                VARCHAR(200) NULL,
  mostrar_logo         BOOLEAN      NOT NULL DEFAULT TRUE,
  mostrar_razon_social BOOLEAN      NOT NULL DEFAULT TRUE,
  mostrar_direccion    BOOLEAN      NOT NULL DEFAULT TRUE,
  mostrar_cliente      BOOLEAN      NOT NULL DEFAULT TRUE,
  mostrar_cajero       BOOLEAN      NOT NULL DEFAULT TRUE,
  mostrar_vuelto       BOOLEAN      NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
);

-- ---------------------------------------------------------
-- AUDITORIA (solo insercion)
-- ---------------------------------------------------------

CREATE TABLE auditoria (
  id_auditoria   BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_empresa     INT          NULL,
  id_sede        INT          NULL,
  usuario_tipo   ENUM('EMPRESA','INTERNO') NOT NULL,
  id_usuario     INT          NOT NULL,
  usuario_nombre VARCHAR(100) NOT NULL,
  rol_nombre     VARCHAR(60)  NOT NULL,
  modulo         VARCHAR(40)  NOT NULL,
  accion         VARCHAR(60)  NOT NULL,
  descripcion    VARCHAR(500) NOT NULL,
  ip             VARCHAR(45)  NULL,
  fecha_hora     DATETIME     NOT NULL,
  FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
  FOREIGN KEY (id_sede) REFERENCES sedes(id_sede)
);

DELIMITER //
CREATE TRIGGER trg_auditoria_no_update BEFORE UPDATE ON auditoria
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoria no se puede modificar';
END//

CREATE TRIGGER trg_auditoria_no_delete BEFORE DELETE ON auditoria
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La auditoria no se puede eliminar';
END//
DELIMITER ;

-- Indices para las consultas mas usadas
CREATE INDEX idx_lotes_producto_sede ON lotes (id_producto, id_sede, fecha_vencimiento);
CREATE INDEX idx_ventas_fecha ON ventas (id_empresa, fecha_hora);
CREATE INDEX idx_kardex_fecha ON kardex (id_empresa, fecha_hora);
CREATE INDEX idx_auditoria_fecha ON auditoria (id_empresa, fecha_hora);

-- Vista de stock por producto y sede
CREATE VIEW v_stock AS
SELECT l.id_empresa, l.id_sede, l.id_producto, SUM(l.cantidad_actual) AS stock
FROM lotes l
GROUP BY l.id_empresa, l.id_sede, l.id_producto;
