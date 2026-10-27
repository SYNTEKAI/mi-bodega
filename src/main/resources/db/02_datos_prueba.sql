-- =========================================================
-- Mi Bodega - Datos de prueba
-- Ejecutar despues de 01_bd_mibodega.sql
-- Las fechas se calculan desde la fecha actual (CURDATE)
-- =========================================================
USE bd_mibodega;

-- ---------------------------------------------------------
-- Permisos (modulo.accion)
-- ---------------------------------------------------------
INSERT INTO permisos (modulo, accion, ambito, descripcion) VALUES
('dashboard','ver','EMPRESA','Ver dashboard'),
('ventas','ver','EMPRESA','Ver ventas'),
('ventas','crear','EMPRESA','Registrar ventas'),
('ventas','anular','EMPRESA','Anular ventas'),
('clientes','ver','EMPRESA','Ver clientes'),
('clientes','crear','EMPRESA','Registrar clientes'),
('clientes','editar','EMPRESA','Editar clientes'),
('clientes','eliminar','EMPRESA','Eliminar clientes'),
('productos','ver','EMPRESA','Ver productos'),
('productos','crear','EMPRESA','Registrar productos'),
('productos','editar','EMPRESA','Editar productos'),
('productos','eliminar','EMPRESA','Eliminar productos'),
('productos','categorias','EMPRESA','Administrar categorías'),
('inventario','ver','EMPRESA','Ver inventario'),
('inventario','ajustar_stock','EMPRESA','Ajustar stock'),
('proveedores','ver','EMPRESA','Ver proveedores'),
('proveedores','crear','EMPRESA','Registrar proveedores'),
('proveedores','editar','EMPRESA','Editar proveedores'),
('proveedores','eliminar','EMPRESA','Eliminar proveedores'),
('recepcion','ver','EMPRESA','Ver recepciones'),
('recepcion','crear','EMPRESA','Registrar recepciones'),
('recepcion','editar_confirmada','EMPRESA','Corregir recepción confirmada'),
('recepcion','anular','EMPRESA','Anular recepciones'),
('recepcion','exportar','EMPRESA','Exportar recepciones'),
('kardex','ver','EMPRESA','Ver kardex'),
('kardex','exportar','EMPRESA','Exportar kardex'),
('reportes','ver','EMPRESA','Ver reportes'),
('reportes','exportar','EMPRESA','Exportar reportes'),
('sedes','ver','EMPRESA','Ver sedes'),
('sedes','crear','EMPRESA','Registrar sedes'),
('sedes','editar','EMPRESA','Editar sedes'),
('sedes','eliminar','EMPRESA','Eliminar sedes'),
('usuarios','ver','EMPRESA','Ver usuarios'),
('usuarios','crear','EMPRESA','Registrar usuarios'),
('usuarios','editar','EMPRESA','Editar usuarios'),
('usuarios','eliminar','EMPRESA','Eliminar usuarios'),
('usuarios','restablecer_password','EMPRESA','Restablecer contraseña'),
('roles','ver','EMPRESA','Ver roles'),
('roles','administrar','EMPRESA','Administrar roles y permisos'),
('comprobantes','ver','EMPRESA','Ver diseño de boleta'),
('comprobantes','configurar','EMPRESA','Configurar boleta'),
('auditoria','ver','EMPRESA','Ver auditoría'),
('dashboard_global','ver','PLATAFORMA','Ver dashboard global'),
('empresas','ver','PLATAFORMA','Ver empresas'),
('empresas','crear','PLATAFORMA','Registrar empresas'),
('empresas','editar','PLATAFORMA','Editar empresas'),
('empresas','cambiar_estado','PLATAFORMA','Cambiar estado de empresas'),
('empresas','entrar','PLATAFORMA','Entrar a una empresa'),
('usuarios_sistema','ver','PLATAFORMA','Ver usuarios internos'),
('usuarios_sistema','crear','PLATAFORMA','Registrar usuarios internos'),
('usuarios_sistema','editar','PLATAFORMA','Editar usuarios internos'),
('usuarios_sistema','restablecer_password','PLATAFORMA','Restablecer contraseña interna'),
('roles_sistema','ver','PLATAFORMA','Ver roles internos'),
('auditoria_global','ver','PLATAFORMA','Ver auditoría global');

-- ---------------------------------------------------------
-- Empresas y sedes
-- ---------------------------------------------------------
INSERT INTO empresas (id_empresa, ruc, razon_social, nombre_comercial, direccion, rubro, telefono, correo, plan, estado, fecha_registro) VALUES
(1,'20601234567','Minimarket Los Andes S.A.C.','Los Andes Market','Av. Los Andes 123, Comas','Minimarket','01-700-1000','contacto@losandes.pe','Estándar','ACTIVA', CURDATE() - INTERVAL 70 DAY),
(2,'20609876543','Bodegas Express Perú E.I.R.L.','Bodegas Express','Jr. Unión 456, Cercado de Lima','Bodega','01-700-2000','ventas@bexpress.pe','Básico','ACTIVA', CURDATE() - INTERVAL 52 DAY),
(3,'20605554443','Market Central Lima S.R.L.','Market Central','Av. Colonial 890, Callao','Minimarket','01-700-3000','admin@mcentral.pe','Estándar','SUSPENDIDA', CURDATE() - INTERVAL 35 DAY),
(4,'20608889991','Bodega Doña Rosa E.I.R.L.','Doña Rosa','Calle Las Flores 210, SJL','Bodega','01-700-4000','rosa@donarosa.pe','Prueba','EN PRUEBA', CURDATE() - INTERVAL 5 DAY);

INSERT INTO sedes (id_sede, id_empresa, codigo, nombre, direccion, distrito, provincia, responsable, telefono, activo, fecha_creacion) VALUES
(1,1,'AND-01','Sede Principal','Av. Los Andes 123','Comas','Lima','Ana Torres','01-555-1001',TRUE, CURDATE() - INTERVAL 70 DAY),
(2,1,'AND-02','Sede Norte','Av. Túpac Amaru 890','Carabayllo','Lima','Lucía Gómez','01-555-1002',TRUE, CURDATE() - INTERVAL 65 DAY),
(3,1,'AND-03','Almacén Central','Calle Los Pinos 45','Comas','Lima','Pedro Ruiz','01-555-1003',TRUE, CURDATE() - INTERVAL 63 DAY),
(4,2,'EXP-01','Sede Principal','Jr. Unión 456','Cercado','Lima','Carlos Soto','01-555-2001',TRUE, CURDATE() - INTERVAL 52 DAY),
(5,2,'EXP-02','Sede Sur','Av. Pachacútec 2210','VMT','Lima','María López','01-555-2002',TRUE, CURDATE() - INTERVAL 49 DAY);

-- ---------------------------------------------------------
-- Roles de cada empresa y sus permisos
-- ---------------------------------------------------------
INSERT INTO roles (id_rol, id_empresa, nombre) VALUES
(1,1,'Administrador'),(2,1,'Supervisor'),(3,1,'Vendedor'),(4,1,'Almacenero'),
(5,1,'Caja'),(6,1,'Gerencia'),(7,1,'Jefe de almacén'),
(8,2,'Administrador'),(9,2,'Vendedor'),(10,2,'Almacenero');

-- Administrador: todos los permisos de empresa
INSERT INTO rol_permisos SELECT 1, id_permiso FROM permisos WHERE ambito = 'EMPRESA';
INSERT INTO rol_permisos SELECT 8, id_permiso FROM permisos WHERE ambito = 'EMPRESA';

INSERT INTO rol_permisos SELECT 2, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard.ver','clientes.ver','inventario.ver','proveedores.ver','recepcion.ver','recepcion.exportar',
 'kardex.ver','kardex.exportar','reportes.ver','reportes.exportar','sedes.ver','auditoria.ver');

INSERT INTO rol_permisos SELECT r.id_rol, p.id_permiso FROM roles r, permisos p
WHERE r.id_rol IN (3,9) AND CONCAT(p.modulo,'.',p.accion) IN
('dashboard.ver','ventas.ver','ventas.crear','clientes.ver','clientes.crear');

INSERT INTO rol_permisos SELECT r.id_rol, p.id_permiso FROM roles r, permisos p
WHERE r.id_rol IN (4,10) AND CONCAT(p.modulo,'.',p.accion) IN
('dashboard.ver','productos.ver','productos.crear','productos.editar','productos.categorias',
 'inventario.ver','inventario.ajustar_stock','proveedores.ver','proveedores.crear','proveedores.editar',
 'recepcion.ver','recepcion.crear','recepcion.exportar','kardex.ver','kardex.exportar');

INSERT INTO rol_permisos SELECT 5, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard.ver','ventas.ver','ventas.crear','ventas.anular','clientes.ver','clientes.crear','reportes.ver');

INSERT INTO rol_permisos SELECT 6, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard.ver','clientes.ver','kardex.ver','kardex.exportar','reportes.ver','reportes.exportar','auditoria.ver');

INSERT INTO rol_permisos SELECT 7, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard.ver','productos.ver','productos.crear','productos.editar','productos.categorias',
 'inventario.ver','inventario.ajustar_stock','proveedores.ver','proveedores.crear','proveedores.editar',
 'recepcion.ver','recepcion.crear','recepcion.editar_confirmada','recepcion.anular','recepcion.exportar',
 'kardex.ver','kardex.exportar','auditoria.ver');

-- ---------------------------------------------------------
-- Usuarios de empresa (claves cifradas con BCrypt)
--   admin/admin123  cajero/cajero123  almacen/almacen123  lucia/lucia123
--   mario/mario123 (inactivo)  carlos/carlos123  maria/maria123  jefe/jefe1234
-- ---------------------------------------------------------
INSERT INTO usuarios (id_usuario, id_empresa, id_sede, usuario, clave_hash, nombre, estado, id_rol_activo) VALUES
(1,1,1,'admin',  '$2a$10$ZKh/KpwZ2GR4i/K4cqDZB.YPy14cRNaoLVJcVSY2v7FrJoRUKs7Zi','Ana Torres','ACTIVO',1),
(2,1,1,'cajero', '$2a$10$majZel0x9gz9q3MRRJ4uouZYf3mRx.UoDp4754pJkkZSM3EBaCD/W','Juan Pérez','ACTIVO',3),
(3,1,3,'almacen','$2a$10$6G9U8XJx.e6Cqua5urqMUOiEmZsR7zo9CmnczS.sQmBrywid8yeEu','Pedro Ruiz','ACTIVO',4),
(4,1,2,'lucia',  '$2a$10$etdopbK3dep4RRx/30IzBO8BJQSeZK1E5IMC8lxI7/hB1hbi9W.5q','Lucía Gómez','ACTIVO',3),
(5,1,1,'mario',  '$2a$10$5feAV3.wKcvW65SyP/ouuOFtOYSGmDLpENXIbSXtUDTEMQ6mi9wdW','Mario Silva','INACTIVO',2),
(6,2,4,'carlos', '$2a$10$FCDG2vw91N93hEHIckIoP.wNaNGfT/KMePolTckrl4yVQfiW4Imua','Carlos Soto','ACTIVO',8),
(7,2,5,'maria',  '$2a$10$rKL/jq/pfFZG/SzWcFjFLOrF3X.j5u5jYfIqTltsqJgB8eb5vhg7m','María López','ACTIVO',9),
(8,1,3,'jefe',   '$2a$10$bFcr8tjdC9JvXSAlkfAIMeVm6YWjROo.TW9ZTWCo7WfbJ78Vz7Fle','Carlos Pérez','ACTIVO',7);

INSERT INTO usuario_roles VALUES
(1,1),(1,6),(2,3),(2,5),(3,4),(4,3),(5,2),(6,8),(7,9),(8,7);

-- ---------------------------------------------------------
-- Plataforma: roles y usuarios internos
--   superadmin/super123  soporte/soporte123  auditor/auditor123
-- ---------------------------------------------------------
INSERT INTO roles_internos (id_rol_interno, nombre, principal) VALUES
(1,'Super Admin',TRUE),(2,'Soporte',FALSE),(3,'Auditor',FALSE);

INSERT INTO rol_interno_permisos SELECT 1, id_permiso FROM permisos WHERE ambito = 'PLATAFORMA';
INSERT INTO rol_interno_permisos SELECT 2, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard_global.ver','empresas.ver','empresas.entrar','usuarios_sistema.ver','usuarios_sistema.restablecer_password','auditoria_global.ver');
INSERT INTO rol_interno_permisos SELECT 3, id_permiso FROM permisos WHERE CONCAT(modulo,'.',accion) IN
('dashboard_global.ver','empresas.ver','auditoria_global.ver');

INSERT INTO usuarios_internos (id_usuario_interno, id_rol_interno, nombre, correo, usuario, clave_hash) VALUES
(1,1,'Administrador General','admin@mibodega.pe','superadmin','$2a$10$qcYm03Bo2MpWmrz2/ktfh.Am3FFlEXN9nZUlDphg1KP8wP6cD/ysq'),
(2,2,'Rosa Medina','soporte@mibodega.pe','soporte','$2a$10$wPqZFf29yXrHIxKX.ocVB.P4tNVk2fFvmxyf2BB.MXzVs8oC.nDr.'),
(3,3,'Iván Quispe','auditor@mibodega.pe','auditor','$2a$10$VWV5Ktqtx/C5jqiO/2zPheaFo0eNL7EIAiZ6ir1yNp6eh22NlsR.2');

-- ---------------------------------------------------------
-- Catalogo
-- ---------------------------------------------------------
INSERT INTO categorias (id_categoria, id_empresa, nombre) VALUES
(1,1,'Abarrotes'),(2,1,'Bebidas'),(3,1,'Lácteos'),(4,1,'Limpieza'),(5,1,'Snacks'),
(6,2,'Abarrotes'),(7,2,'Snacks');

INSERT INTO productos (id_producto, id_empresa, id_categoria, codigo_barras, nombre, marca, descripcion, presentacion, unidad, empaque, unidades_por_empaque, precio_compra, precio_venta, stock_minimo) VALUES
(1,1,1,'7750182000123','Arroz Costeño 5kg','Costeño','Arroz extra graneado','bolsa 5 kg','bolsa','Fardo',10,21.00,24.50,10),
(2,1,3,'7750182000456','Leche Gloria Evaporada','Gloria','Leche evaporada entera','lata 400 g','lata','Caja',48,3.40,4.20,24),
(3,1,2,'7750182000789','Gaseosa Inca Kola 1.5L','Inca Kola','No retornable','botella 1.5 L','botella','Paquete',6,5.80,7.50,12),
(4,1,3,'7750182000234','Yogurt Gloria Fresa 1L','Gloria','Sabor fresa','botella 1 L','botella','Caja',12,5.10,6.80,8),
(5,1,4,'7750182000567','Detergente Bolívar 780g','Bolívar','Aroma floral','bolsa 780 g','bolsa','Caja',10,7.90,9.90,6),
(6,1,1,'7750182000890','Aceite Primor 1L','Primor','Aceite vegetal','botella 1 L','botella','Caja',12,8.80,10.50,12),
(7,2,6,'7750182001111','Fideos Don Vittorio 1kg','Don Vittorio','Spaghetti','bolsa 1 kg','bolsa','Fardo',20,4.60,5.90,15),
(8,2,7,'7750182002222','Papas Lays Clasicas','Lays','Papas fritas clásicas','bolsa 42 g','bolsa','Caja',30,2.40,3.50,20);

INSERT INTO proveedores (id_proveedor, id_empresa, tipo_doc, documento, razon_social, nombre_comercial, contacto, telefono, correo, direccion, observaciones) VALUES
(1,1,'RUC','20100055519','Distribuidora Andina S.A.','Dist. Andina','Jorge Salas','01-555-0101','pedidos@andina.pe','Av. Argentina 2450, Lima','Entrega martes y viernes'),
(2,1,'RUC','20100066620','Alicorp S.A.A.','Alicorp Mayorista','Paola Ríos','01-555-0202','ventas@alicorp.pe','Av. Venezuela 2470, Lima',NULL),
(3,2,'RUC','20100077731','Makro Supermayorista S.A.','Makro Cercado','Luis Vega','01-555-0303','empresas@makro.pe','Av. Colonial 1200, Lima','Pago al contado');

INSERT INTO clientes (id_cliente, id_empresa, tipo_doc, documento, nombre, telefono, correo, direccion, fecha_registro) VALUES
(1,1,'DNI','45871236','Rosa Huamán Quispe','987654321','rosa.hq@gmail.com','Jr. Los Olivos 120, Comas', CURDATE() - INTERVAL 68 DAY),
(2,1,'DNI','70123489','Carlos Mendoza Ríos','956321478','cmendoza@hotmail.com',NULL, CURDATE() - INTERVAL 66 DAY),
(3,1,'RUC','20512345678','Restaurante El Buen Sabor S.A.C.','01-521-4455','compras@buensabor.pe','Av. Belaúnde 890, Comas', CURDATE() - INTERVAL 60 DAY),
(4,1,'DNI','41236587','Lucia Paredes Soto','912458736',NULL,NULL, CURDATE() - INTERVAL 50 DAY),
(5,1,'DNI','46985214','Miguel Ángel Torres','934125698','matorres@gmail.com',NULL, CURDATE() - INTERVAL 40 DAY),
(6,2,'DNI','40125896','Pedro Castillo Vega','987111222',NULL,NULL, CURDATE() - INTERVAL 45 DAY),
(7,2,'DNI','73659841','Ana María Ríos','956333444','anamaria@gmail.com',NULL, CURDATE() - INTERVAL 44 DAY);

INSERT INTO correlativos VALUES (1,'GR',3),(2,'GR',1),(1,'B001',0),(2,'B001',0);

INSERT INTO config_comprobantes (id_empresa, plantilla, pie, mensaje, notas) VALUES
(1,'CLASICO','¡Gracias por su compra!','Síguenos en @losandesmarket','No se aceptan devoluciones pasadas 48 horas'),
(2,'COMPACTO','Gracias por su preferencia',NULL,NULL);

-- ---------------------------------------------------------
-- Recepciones, lotes y kardex inicial
-- ---------------------------------------------------------
INSERT INTO recepciones (id_recepcion, id_empresa, id_sede, id_proveedor, numero, guia_proveedor, fecha_hora, estado, observaciones, id_usuario) VALUES
(1,1,1,1,'GR-000001','T001-000341', NOW() - INTERVAL 37 DAY,'CONFIRMADA',NULL,3),
(2,1,2,2,'GR-000002','F001-004512', NOW() - INTERVAL 33 DAY,'CONFIRMADA','1 caja de aceite con envases abollados (se aceptó)',3),
(3,2,4,3,'GR-000001','EG07-000112', NOW() - INTERVAL 35 DAY,'CONFIRMADA',NULL,6),
(4,1,3,2,'GR-000003','F001-004590', NOW() - INTERVAL 1 DAY,'BORRADOR',NULL,3);

INSERT INTO lotes (id_lote, id_empresa, id_sede, id_producto, id_recepcion, cantidad_ingresada, cantidad_actual, costo_unitario, fecha_vencimiento) VALUES
(1,1,1,1,1,50,42,21.00, CURDATE() + INTERVAL 140 DAY),
(2,1,1,2,1,96,30,3.40,  CURDATE() + INTERVAL 12 DAY),
(3,1,1,2,1,48,48,3.45,  CURDATE() + INTERVAL 45 DAY),
(4,1,1,3,1,60,18,5.80,  CURDATE() + INTERVAL 90 DAY),
(5,1,1,4,1,24,6,5.10,   CURDATE() + INTERVAL 6 DAY),
(6,1,2,1,2,30,30,21.20, CURDATE() + INTERVAL 160 DAY),
(7,1,2,3,2,48,44,5.85,  CURDATE() + INTERVAL 110 DAY),
(8,1,2,5,2,20,20,7.90,  CURDATE() + INTERVAL 600 DAY),
(9,1,2,6,2,36,9,8.80,   CURDATE() + INTERVAL 200 DAY),
(10,2,4,7,3,40,33,4.60, CURDATE() + INTERVAL 290 DAY),
(11,2,4,8,3,60,52,2.40, CURDATE() + INTERVAL 35 DAY);

INSERT INTO recepcion_detalles (id_recepcion, id_producto, empaque, contenido, cantidad_empaques, total_unidades, costo_unitario, fecha_vencimiento, id_lote)
SELECT l.id_recepcion, l.id_producto, p.empaque, p.unidades_por_empaque,
       l.cantidad_ingresada DIV p.unidades_por_empaque, l.cantidad_ingresada, l.costo_unitario, l.fecha_vencimiento, l.id_lote
FROM lotes l JOIN productos p ON p.id_producto = l.id_producto;

INSERT INTO recepcion_detalles (id_recepcion, id_producto, empaque, contenido, cantidad_empaques, total_unidades, costo_unitario, fecha_vencimiento, id_lote)
VALUES (4,5,'Caja',10,3,30,7.90, CURDATE() + INTERVAL 700 DAY, NULL);

-- Entrada de cada lote
INSERT INTO kardex (id_empresa, id_sede, id_producto, id_lote, tipo, cantidad, costo_unitario, documento, id_recepcion, id_proveedor, id_usuario, fecha_hora)
SELECT l.id_empresa, l.id_sede, l.id_producto, l.id_lote, 'ENTRADA', l.cantidad_ingresada, l.costo_unitario,
       CONCAT('RECEPCION ', r.numero, ' - guia ', r.guia_proveedor), r.id_recepcion, r.id_proveedor, r.id_usuario, r.fecha_hora
FROM lotes l JOIN recepciones r ON r.id_recepcion = l.id_recepcion;

-- Ajuste por conteo fisico del yogurt (lote 5)
INSERT INTO kardex (id_empresa, id_sede, id_producto, id_lote, tipo, cantidad, costo_unitario, documento, id_usuario, fecha_hora)
VALUES (1,1,4,5,'AJUSTE',-2,5.10,'AJUSTE: conteo físico',3, NOW() - INTERVAL 2 DAY);

-- Salida resumen de lo vendido antes de usar el sistema
INSERT INTO kardex (id_empresa, id_sede, id_producto, id_lote, tipo, cantidad, costo_unitario, documento, id_usuario, fecha_hora)
SELECT l.id_empresa, l.id_sede, l.id_producto, l.id_lote, 'SALIDA',
       l.cantidad_ingresada - l.cantidad_actual - IF(l.id_lote = 5, 2, 0), l.costo_unitario,
       'VENTAS DEL PERÍODO (resumen)', IF(l.id_empresa = 1, 2, 7), NOW() - INTERVAL 3 DAY
FROM lotes l
WHERE l.cantidad_ingresada - l.cantidad_actual - IF(l.id_lote = 5, 2, 0) > 0;

-- ---------------------------------------------------------
-- Historial de ventas de los ultimos 60 dias (para dashboard y reportes)
-- No descuenta lotes: representa ventas ya resumidas en el kardex
-- ---------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_generar_ventas_demo()
BEGIN
  DECLARE v_dia INT DEFAULT 60;
  DECLARE v_emp INT;
  DECLARE v_n INT;
  DECLARE v_i INT;
  DECLARE v_items INT;
  DECLARE v_j INT;
  DECLARE v_venta INT;
  DECLARE v_prod INT;
  DECLARE v_precio DECIMAL(10,2);
  DECLARE v_cant INT;
  DECLARE v_total DECIMAL(10,2);
  DECLARE v_num INT;
  DECLARE v_pago VARCHAR(15);
  DECLARE v_fecha DATETIME;

  DO RAND(2026);
  WHILE v_dia >= 1 DO
    SET v_emp = 1;
    WHILE v_emp <= 2 DO
      SET v_n = 3 + FLOOR(RAND() * 5) + IF(DAYOFWEEK(CURDATE() - INTERVAL v_dia DAY) IN (1,7), 3, 0);
      IF v_emp = 2 THEN SET v_n = GREATEST(1, FLOOR(v_n * 0.6)); END IF;
      SET v_i = 1;
      WHILE v_i <= v_n DO
        SET v_fecha = TIMESTAMP(CURDATE() - INTERVAL v_dia DAY, MAKETIME(9 + FLOOR(RAND() * 12), FLOOR(RAND() * 60), 0));
        SET v_pago = ELT(1 + FLOOR(RAND() * 6), 'EFECTIVO','EFECTIVO','EFECTIVO','YAPE','TARJETA','TRANSFERENCIA');
        UPDATE correlativos SET ultimo = ultimo + 1 WHERE id_empresa = v_emp AND serie = 'B001';
        SELECT ultimo INTO v_num FROM correlativos WHERE id_empresa = v_emp AND serie = 'B001';

        INSERT INTO ventas (id_empresa, id_sede, id_cliente, id_usuario, numero_comprobante, fecha_hora, metodo_pago, total)
        VALUES (v_emp,
                IF(v_emp = 1, 1 + FLOOR(RAND() * 2), 4 + FLOOR(RAND() * 2)),
                IF(RAND() < 0.35, IF(v_emp = 1, 1 + FLOOR(RAND() * 5), 6 + FLOOR(RAND() * 2)), NULL),
                IF(v_emp = 1, ELT(1 + FLOOR(RAND() * 3), 2, 4, 1), ELT(1 + FLOOR(RAND() * 2), 7, 6)),
                CONCAT('B001-', LPAD(v_num, 6, '0')), v_fecha, v_pago, 0);
        SET v_venta = LAST_INSERT_ID();

        SET v_total = 0;
        SET v_items = 1 + FLOOR(RAND() * 3);
        SET v_j = 1;
        WHILE v_j <= v_items DO
          SELECT id_producto, precio_venta INTO v_prod, v_precio
          FROM productos WHERE id_empresa = v_emp ORDER BY RAND() LIMIT 1;
          SET v_cant = 1 + FLOOR(RAND() * 3);
          INSERT INTO venta_detalles (id_venta, id_producto, cantidad, precio_unitario, subtotal)
          VALUES (v_venta, v_prod, v_cant, v_precio, v_cant * v_precio);
          SET v_total = v_total + v_cant * v_precio;
          SET v_j = v_j + 1;
        END WHILE;

        UPDATE ventas SET total = v_total WHERE id_venta = v_venta;
        IF v_pago = 'EFECTIVO' THEN
          UPDATE ventas
          SET monto_recibido = CASE WHEN v_total <= 10 THEN 10 WHEN v_total <= 20 THEN 20 WHEN v_total <= 50 THEN 50 ELSE 100 END,
              vuelto = (CASE WHEN v_total <= 10 THEN 10 WHEN v_total <= 20 THEN 20 WHEN v_total <= 50 THEN 50 ELSE 100 END) - v_total
          WHERE id_venta = v_venta;
        END IF;
        SET v_i = v_i + 1;
      END WHILE;
      SET v_emp = v_emp + 1;
    END WHILE;
    SET v_dia = v_dia - 1;
  END WHILE;
END//
DELIMITER ;

CALL sp_generar_ventas_demo();

-- Auditoria inicial
INSERT INTO auditoria (id_empresa, id_sede, usuario_tipo, id_usuario, usuario_nombre, rol_nombre, modulo, accion, descripcion, ip, fecha_hora) VALUES
(1,1,'EMPRESA',3,'Pedro Ruiz','Almacenero','Inventario','MODIFICAR STOCK','Lote L-5 (Yogurt Gloria Fresa 1L): de 8 a 6 unidades. Motivo: conteo físico','192.168.1.31', NOW() - INTERVAL 2 DAY),
(2,4,'EMPRESA',6,'Carlos Soto','Administrador','Usuarios','CREAR USUARIO','Creó al usuario María López (Vendedor, Sede Sur)','192.168.2.10', NOW() - INTERVAL 40 DAY);
