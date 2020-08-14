package gub.agesic.connector.dataaccess.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adriancur on 19/12/17.
 */
public class KeystoreModalData {

    private List<Certificado> certificados;
    private String nombre;
    private String nombreModal;

    public List<Certificado> getCertificados() {
        if (certificados == null || certificados.isEmpty()) {
            certificados = new ArrayList<Certificado>();
        }
        return certificados;
    }

    public void setCertificados(final List<Certificado> certificados) {
        this.certificados = certificados;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    public String getNombreModal() {
        return nombreModal;
    }

    public void setNombreModal(final String nombreModal) {
        this.nombreModal = nombreModal;
    }
}
