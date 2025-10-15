package conectaseguros.co.news_service.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.storage")
public class StorageConfig {

    private String directorioBase = "/var/conecta-seguros/archivos";
    private boolean habilitarCompresion = true;
    private long tiempoRetencionDias = 365;
    private boolean habilitarBackup = true;

    // Getters y Setters
    public String getDirectorioBase() { return directorioBase; }
    public void setDirectorioBase(String directorioBase) { this.directorioBase = directorioBase; }

    public boolean isHabilitarCompresion() { return habilitarCompresion; }
    public void setHabilitarCompresion(boolean habilitarCompresion) { this.habilitarCompresion = habilitarCompresion; }

    public long getTiempoRetencionDias() { return tiempoRetencionDias; }
    public void setTiempoRetencionDias(long tiempoRetencionDias) { this.tiempoRetencionDias = tiempoRetencionDias; }

    public boolean isHabilitarBackup() { return habilitarBackup; }
    public void setHabilitarBackup(boolean habilitarBackup) { this.habilitarBackup = habilitarBackup; }
}