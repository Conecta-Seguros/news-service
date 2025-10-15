package conectaseguros.co.news_service.descuentos.infrastructure.input.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteExistenceResponseDTO {
    private String cedula;
    private boolean exists;
    private String message;
    private ClienteBasicInfoDTO clienteInfo;

    // Constructors
    public ClienteExistenceResponseDTO() {}

    public ClienteExistenceResponseDTO(String cedula, boolean exists, String message, ClienteBasicInfoDTO clienteInfo) {
        this.cedula = cedula;
        this.exists = exists;
        this.message = message;
        this.clienteInfo = clienteInfo;
    }

    // Getters and Setters
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public boolean exists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ClienteBasicInfoDTO clienteInfo() { return clienteInfo; }
    public void setClienteInfo(ClienteBasicInfoDTO clienteInfo) { this.clienteInfo = clienteInfo; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClienteBasicInfoDTO {
        private Long id;
        private String nombres;
        private String apellidos;
        private String nombreCompleto;
        private String estado;
        private Integer seccionalId;
        private boolean activo;

        // Constructors
        public ClienteBasicInfoDTO() {}

        // Getters and Setters
        public Long id() { return id; }
        public void setId(Long id) { this.id = id; }

        public String nombres() { return nombres; }
        public void setNombres(String nombres) { this.nombres = nombres; }

        public String apellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }

        public String nombreCompleto() { return nombreCompleto; }
        public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

        public String estado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public Integer seccionalId() { return seccionalId; }
        public void setSeccionalId(Integer seccionalId) { this.seccionalId = seccionalId; }

        public boolean activo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
    }
}
