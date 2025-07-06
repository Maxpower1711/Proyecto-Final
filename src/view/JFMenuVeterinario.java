package view;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Cita;
import controller.DatosCompartidos;
import controller.ExportarExcel;
import java.io.IOException;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JFMenuVeterinario extends javax.swing.JFrame {
    
    DefaultTableModel modeloTabla;

    public JFMenuVeterinario() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
        
    }
    
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("N° Cita");
        modeloTabla.addColumn("DNI Cliente");
        modeloTabla.addColumn("Código Mascota");
        modeloTabla.addColumn("Síntomas");
        modeloTabla.addColumn("Estado"); // NUEVA COLUMNA
        Table.setModel(modeloTabla);
    }

    void limpiarTabla() {
        modeloTabla.setRowCount(0);
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
            contenido.append("N° Cita: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("DNI Cliente: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Código Mascota: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Síntomas: ").append(modeloTabla.getValueAt(i, 3)).append(", ");
            contenido.append("Estado: ").append(modeloTabla.getValueAt(i, 4)).append("\n");
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
            Logger.getLogger(JFMenuVeterinario.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    void listar() {
        limpiarTabla();
        if (DatosCompartidos.citas.tamaño() > 0) {
            for (int i = 0; i < DatosCompartidos.citas.tamaño(); i++) {
                Cita c = DatosCompartidos.citas.obtener(i);
                Object[] fila = {
                    c.getNroCita(),
                    c.getCliente().getDni(),
                    c.getMascota().getCodigo(),
                    c.getSintomas(),
                    c.getEstado() == null ? "No atendida" : c.getEstado()
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de citas: " + DatosCompartidos.citas.tamaño(), "Información");
        } else {
            mensaje("No hay citas registradas", "Información");
        }
    }

    void atenderCita() {
        String nroCita = JOptionPane.showInputDialog(this, "Ingrese el número de cita a atender:");
        if (nroCita == null || nroCita.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un número de cita.");
            return;
        }

        for (int i = 0; i < DatosCompartidos.citas.tamaño(); i++) {
            Cita c = DatosCompartidos.citas.obtener(i);
            if (c.getNroCita().equalsIgnoreCase(nroCita.trim())) {
                c.setEstado("Atendida");
                listar();
                JOptionPane.showMessageDialog(this, "La cita N° " + nroCita + " fue atendida.");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No se encontró la cita N° " + nroCita + ".");
    }

    void eliminarCita() {
        String nroCita = JOptionPane.showInputDialog(this, "Ingrese el número de cita a eliminar:");
        if (nroCita == null || nroCita.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un número de cita.");
            return;
        }

        for (int i = 0; i < DatosCompartidos.citas.tamaño(); i++) {
            Cita c = DatosCompartidos.citas.obtener(i);
            if (c.getNroCita().equalsIgnoreCase(nroCita.trim())) {
                DatosCompartidos.citas.eliminar(c);
                listar();
                JOptionPane.showMessageDialog(this, "La cita N° " + nroCita + " fue eliminada.");
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "No se encontró la cita N° " + nroCita + ".");
    }

    void borrar() {
        limpiarTabla();
    }

    void cerrar() {
        dispose();
    }

    void procesar() {
        int opcion = cboOpcion.getSelectedIndex();
        switch (opcion) {
            case 0 -> listar();
            case 1 -> atenderCita();
            case 2 -> eliminarCita();
            default -> JOptionPane.showMessageDialog(this, "Seleccione una opción válida.");
        }
        
        String comentario = txtComentario.getText().trim();
        if (!comentario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Comentario adicional: " + comentario, "Comentario", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No se añadieron comentarios adicionales.", "Comentario", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        cboOpcion = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnRecetaMedica = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtComentario = new javax.swing.JTextField();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel1.setText("MEDICO-VETERINARIO");

        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        cboOpcion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Consultar", "Atender", "Eliminar" }));
        cboOpcion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOpcionItemStateChanged(evt);
            }
        });

        jLabel6.setText("Opcion:");

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

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

        btnRecetaMedica.setText("EMITIR RECETA");
        btnRecetaMedica.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecetaMedicaActionPerformed(evt);
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

        jLabel2.setText("Comentarios adicionales:");

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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102)
                .addComponent(btnRecetaMedica, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(129, 129, 129))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(145, 145, 145)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(67, 67, 67)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addComponent(btnLimpiar)
                                .addGap(56, 56, 56)
                                .addComponent(btnProcesar)
                                .addGap(51, 51, 51)
                                .addComponent(btnCerrar))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(42, 42, 42)
                                    .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(46, 46, 46))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(67, 67, 67)
                                            .addComponent(jLabel6)
                                            .addGap(49, 49, 49))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtComentario, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtComentario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpiar)
                    .addComponent(btnProcesar)
                    .addComponent(btnCerrar))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCorreo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRecetaMedica, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        JFMenu menuPrincipal = new JFMenu();
        menuPrincipal.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void cboOpcionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboOpcionItemStateChanged
        
    }//GEN-LAST:event_cboOpcionItemStateChanged

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        cerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        borrar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcesarActionPerformed
        procesar();
    }//GEN-LAST:event_btnProcesarActionPerformed

    private void btnRecetaMedicaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecetaMedicaActionPerformed
        JFRecetaMedica recetaMedica = new JFRecetaMedica();
        this.setVisible(true);
        recetaMedica.setVisible(true);
    }//GEN-LAST:event_btnRecetaMedicaActionPerformed

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
                new JFMenuVeterinario().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnCorreo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnProcesar;
    private javax.swing.JButton btnRecetaMedica;
    private javax.swing.JButton btnRegresar;
    private javax.swing.JComboBox<String> cboOpcion;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField txtComentario;
    // End of variables declaration//GEN-END:variables
}
