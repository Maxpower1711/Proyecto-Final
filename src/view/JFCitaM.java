package view;

import controller.ArregloCita;
import controller.DatosCompartidos;
import controller.ExportarExcel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Cita;
import model.Cliente;
import model.Mascota;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JFCitaM extends javax.swing.JFrame {
    
    ArregloCita citas = DatosCompartidos.citas;
    DefaultTableModel modeloTabla;

    public JFCitaM() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
    }
    
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nro. Cita");
        modeloTabla.addColumn("DNI Cliente");
        modeloTabla.addColumn("Nombre Cliente");
        modeloTabla.addColumn("Cod. Mascota");
        modeloTabla.addColumn("Raza Mascota");
        modeloTabla.addColumn("Síntomas");
        modeloTabla.addColumn("Estado"); // NUEVA COLUMNA
        Table.setModel(modeloTabla);
    }

    void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    String getNroCita() {
        return txtNroCita.getText().trim();
    }

    String getDniCliente() {
        return txtDniC.getText().trim();
    }

    int getCodigoMascota() {
        return Integer.parseInt(txtCodigoM.getText().trim());
    }

    String getSintomas() {
        return txtSintomas.getText().trim();
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void enviarCorreoConCitas() {
        String emailFrom = "maxpower14567@gmail.com";
        String passwordFrom = "kkmd lcni mfoe udcv";
        String emailTo = JOptionPane.showInputDialog(this, "Ingrese el correo de destino:", "Enviar Citas por Correo", JOptionPane.QUESTION_MESSAGE);

        if (emailTo == null || emailTo.trim().isEmpty()) {
            mensaje("No se ingresó correo de destino", "Error");
            return;
        }

        // Construir el contenido del correo desde el JTable
        StringBuilder contenido = new StringBuilder();
        contenido.append("Listado de citas registradas en la veterinaria:\n\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            contenido.append("Nro. Cita: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("DNI Cliente: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Nombre Cliente: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Cod. Mascota: ").append(modeloTabla.getValueAt(i, 3)).append(", ");
            contenido.append("Raza Mascota: ").append(modeloTabla.getValueAt(i, 4)).append(", ");
            contenido.append("Síntomas: ").append(modeloTabla.getValueAt(i, 5)).append(", ");
            contenido.append("Estado: ").append(modeloTabla.getValueAt(i, 6)).append("\n");
        }

        // Configuración de propiedades de envío
        Properties mProperties = new Properties();
        mProperties.put("mail.smtp.host", "smtp.gmail.com");
        mProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        mProperties.setProperty("mail.smtp.starttls.enable", "true");
        mProperties.setProperty("mail.smtp.port", "587");
        mProperties.setProperty("mail.smtp.user", emailFrom);
        mProperties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        mProperties.setProperty("mail.smtp.auth", "true");

        Session mSession = Session.getDefaultInstance(mProperties);

        try {
            MimeMessage mCorreo = new MimeMessage(mSession);
            mCorreo.setFrom(new InternetAddress(emailFrom));
            mCorreo.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            mCorreo.setSubject("Listado de Citas Registradas");
            mCorreo.setText(contenido.toString(), "UTF-8", "plain");

            Transport mTransport = mSession.getTransport("smtp");
            mTransport.connect(emailFrom, passwordFrom);
            mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
            mTransport.close();

            mensaje("Correo enviado correctamente", "Confirmación");
        } catch (MessagingException ex) {
            Logger.getLogger(JFCitaM.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    void listar() {
        limpiarTabla();
        if (citas.tamaño() > 0) {
            for (int i = 0; i < citas.tamaño(); i++) {
                Cita c = citas.obtener(i);
                Cliente cli = c.getCliente();
                Mascota mas = c.getMascota();

                Object[] fila = {
                    c.getNroCita(),
                    cli.getDni(),
                    cli.getNombre(),
                    mas.getCodigo(),
                    mas.getRaza(),
                    c.getSintomas(),
                    c.getEstado() == null ? "No atendida" : c.getEstado() // NUEVO
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de citas: " + citas.tamaño(), "Información");
        } else {
            mensaje("No hay citas registradas", "Información");
        }
    }

    void ingresar() {
        Cita cita = citas.buscar(getNroCita());
        if (cita == null) {
            String dni = getDniCliente();
            int codigoM = getCodigoMascota();

            Cliente cliente = DatosCompartidos.clientes.buscarPorDni(dni);
            Mascota mascota = DatosCompartidos.mascotas.buscar(codigoM);

            if (cliente == null) {
                mensaje("DNI del cliente no existe", "Error");
                return;
            }
            if (mascota == null) {
                mensaje("Código de la mascota no existe", "Error");
                return;
            }

            cita = new Cita(getNroCita(), cliente, mascota, getSintomas());
            cita.setEstado("No atendida"); // SE AGREGA ESTADO INICIAL

            citas.adicionar(cita);

            // Guardar en archivo .txt
            try (PrintWriter pw = new PrintWriter(new FileWriter("citas.txt", true))) {
                pw.println("--FICHA MEDICA--");
                pw.println("N° Cita: " + cita.getNroCita());
                pw.println("DNI Cliente: " + cliente.getDni());
                pw.println("Nombre Cliente: " + cliente.getNombre());
                pw.println("Código Mascota: " + mascota.getCodigo());
                pw.println("Raza Mascota: " + mascota.getRaza());
                pw.println("Síntomas: " + cita.getSintomas());
                pw.println("Estado: " + (cita.getEstado() == null ? "No atendida" : cita.getEstado()));
                pw.println("---------------------------------");
            } catch (IOException e) {
                mensaje("Error al guardar la cita en el archivo: " + e.getMessage(), "Error");
            }

            listar();
            mensaje("Cita ingresada correctamente", "Confirmación");
        } else {
            mensaje("Número de cita ya registrado", "Error");
        }
    }

    void consultar() {
        limpiarTabla();
        Cita cita = citas.buscar(getNroCita());
        if (cita != null) {
            Cliente cli = cita.getCliente();
            Mascota mas = cita.getMascota();

            Object[] fila = {
                cita.getNroCita(),
                cli.getDni(),
                cli.getNombre(),
                mas.getCodigo(),
                mas.getRaza(),
                cita.getSintomas(),
                cita.getEstado() == null ? "No atendida" : cita.getEstado()
            };
            modeloTabla.addRow(fila);
        } else {
            mensaje("Cita no existe", "Error");
        }
    }

    void modificar() {
        Cita cita = citas.buscar(getNroCita());
        if (cita != null) {
            String dni = getDniCliente();
            int codigoM = getCodigoMascota();

            Cliente cliente = DatosCompartidos.clientes.buscarPorDni(dni);
            Mascota mascota = DatosCompartidos.mascotas.buscar(codigoM);

            if (cliente == null) {
                mensaje("DNI del cliente no existe", "Error");
                return;
            }
            if (mascota == null) {
                mensaje("Código de la mascota no existe", "Error");
                return;
            }

            cita.setCliente(cliente);
            cita.setMascota(mascota);
            cita.setSintomas(getSintomas());

            listar();
            mensaje("Cita modificada correctamente", "Confirmación");
        } else {
            mensaje("Cita no existe", "Error");
        }
    }

    void eliminar() {
        Cita cita = citas.buscar(getNroCita());
        if (cita != null) {
            citas.eliminar(cita);
            listar();
            mensaje("Cita eliminada correctamente", "Confirmación");
        } else {
            mensaje("Cita no encontrada", "Error");
        }
    }

    void borrar() {
        txtNroCita.setText("");
        txtDniC.setText("");
        txtCodigoM.setText("");
        txtSintomas.setText("");
        txtNroCita.requestFocus();
    }

    void procesar() {
        switch (cboOpcion.getSelectedIndex()) {
            case 0 -> ingresar();
            case 1 -> consultar();
            case 2 -> modificar();
            case 3 -> eliminar();
        }
    }

    void seleccionar() {
        int opcion = cboOpcion.getSelectedIndex();
        boolean visible = (opcion == 0 || opcion == 2);

        txtDniC.setVisible(visible);
        lblDni.setVisible(visible);
        txtCodigoM.setVisible(visible);
        lblCodigo.setVisible(visible);
        txtSintomas.setVisible(visible);
        lblSintomas.setVisible(visible);
    }

    void cerrar() {
        dispose();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblDni = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        lblSintomas = new javax.swing.JLabel();
        txtNroCita = new javax.swing.JTextField();
        txtCodigoM = new javax.swing.JTextField();
        txtSintomas = new javax.swing.JTextField();
        txtDniC = new javax.swing.JTextField();
        cboOpcion = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        btnLimpiar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        btnRegresar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel1.setText("CITA MEDICA-VETERINARIA");

        jLabel2.setText("Nro. de Cita:");

        lblDni.setText("Dni del cliente:");

        lblCodigo.setText("Codigo de mascota:");

        lblSintomas.setText("Sintomas:");

        cboOpcion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ingresar", "consultar", "modificar", "eliminar" }));
        cboOpcion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOpcionItemStateChanged(evt);
            }
        });

        jLabel6.setText("Opcion:");

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnProcesar.setText("Procesar");
        btnProcesar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProcesarActionPerformed(evt);
            }
        });

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(Table);

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("Exportar a excel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        btnCorreo.setText("ENVIAR CORREO");
        btnCorreo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCorreoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(275, 275, 275))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(96, 96, 96)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(38, 38, 38)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblCodigo)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(46, 46, 46)
                                        .addComponent(lblSintomas, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(28, 28, 28))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addGap(50, 50, 50))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(lblDni, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtNroCita, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDniC, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCodigoM, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(70, 70, 70)
                                        .addComponent(btnLimpiar)
                                        .addGap(40, 40, 40)
                                        .addComponent(btnCerrar))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(55, 55, 55)
                                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnProcesar)
                                        .addGap(48, 48, 48))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSintomas, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(41, 41, 41)
                                .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 622, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(172, 172, 172)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtNroCita, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDni)
                            .addComponent(txtDniC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCodigo)
                            .addComponent(txtSintomas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLimpiar)
                            .addComponent(btnCerrar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnProcesar)
                        .addGap(18, 18, 18)
                        .addComponent(btnCorreo)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSintomas)
                            .addComponent(txtCodigoM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboOpcionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboOpcionItemStateChanged
        seleccionar();
    }//GEN-LAST:event_cboOpcionItemStateChanged

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        borrar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcesarActionPerformed
        procesar();
    }//GEN-LAST:event_btnProcesarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        cerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        JFMenuCliente menuCliente = new JFMenuCliente();
        menuCliente.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        ExportarExcel obj;

        try {
            obj = new ExportarExcel();
            obj.exportarExcel(Table);
        } catch (IOException ex) {
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCorreoActionPerformed
        enviarCorreoConCitas();
    }//GEN-LAST:event_btnCorreoActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFCitaM().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnCorreo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnProcesar;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox<String> cboOpcion;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDni;
    private javax.swing.JLabel lblSintomas;
    private javax.swing.JTextField txtCodigoM;
    private javax.swing.JTextField txtDniC;
    private javax.swing.JTextField txtNroCita;
    private javax.swing.JTextField txtSintomas;
    // End of variables declaration//GEN-END:variables
}
