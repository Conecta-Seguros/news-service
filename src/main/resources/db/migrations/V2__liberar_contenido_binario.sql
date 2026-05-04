-- Libera el espacio ocupado por los binarios en DB para registros que ya tienen
-- su archivo en el filesystem (ruta_archivo NOT NULL). Los bytes son redundantes
-- desde que LocalFileStorageAdapter guarda en disco en cada carga.
UPDATE archivos_descuentos SET contenido = NULL WHERE ruta_archivo IS NOT NULL;
UPDATE archivos_novedades SET contenido = NULL WHERE ruta_archivo IS NOT NULL;
