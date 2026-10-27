-- =========================================================
-- Usuario de base de datos que usa la aplicacion
-- Solo tiene permisos de lectura y escritura (no puede borrar tablas)
-- Ejecutar con el usuario root de MySQL
-- =========================================================
CREATE USER IF NOT EXISTS 'app_mibodega'@'localhost' IDENTIFIED BY 'MiBodega2026';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON bd_mibodega.* TO 'app_mibodega'@'localhost';
FLUSH PRIVILEGES;
