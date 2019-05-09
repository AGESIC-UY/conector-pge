package gub.agesic.connector.dataaccess.entity;

/**
 * Created by adriancur on 19/12/17.
 */
public class Certificado {
    private String alias;
    private String tipo;
    private String proveedor;
    private String fechaCreacion;
    private String fechaVencimiento;

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(final String tipo) {
        this.tipo = tipo;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(final String proveedor) {
        this.proveedor = proveedor;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(final String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(final String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
