package view;

import controller.ArregloMascota;
import controller.DatosCompartidos;
import controller.ExportarExcel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Mascota;
import java.io.BufferedWriter;
import java.io.FileWriter;
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

public class JFMascota extends javax.swing.JFrame {
    
    ArregloMascota m = DatosCompartidos.mascotas;
    DefaultTableModel modeloTabla;

    public JFMascota() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
    }   
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Código");
        modeloTabla.addColumn("Edad");
        modeloTabla.addColumn("Raza");
        modeloTabla.addColumn("Peso (KG)");
        modeloTabla.addColumn("Nro. Vacunas");
        Table.setModel(modeloTabla);
    }

    void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    int getCodigo() {
        return Integer.parseInt(txtCodigo.getText().trim());
    }

    int getEdad() {
        return Integer.parseInt(txtEdad.getText().trim());
    }

    String getRaza() {
        return txtRaza.getText().trim();
    }

    double getPeso() {
        return Double.parseDouble(txtPeso.getText().trim());
    }

    int getNumeroVacunas() {
        return Integer.parseInt(txtNumero.getText().trim());
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void enviarCorreoConMascotas() {
        String emailFrom = "maxpower14567@gmail.com";
        String passwordFrom = "kkmd lcni mfoe udcv";
        String emailTo = JOptionPane.showInputDialog(this, "Ingrese el correo de destino:", "Enviar Mascotas por Correo", JOptionPane.QUESTION_MESSAGE);

        if (emailTo == null || emailTo.trim().isEmpty()) {
            mensaje("No se ingresó correo de destino", "Error");
            return;
        }

        // Construir el contenido del correo desde el JTable
        StringBuilder contenido = new StringBuilder();
        contenido.append("Listado de mascotas registradas en la veterinaria:\n\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            contenido.append("Código: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("Edad: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Raza: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Peso (KG): ").append(modeloTabla.getValueAt(i, 3)).append(", ");
            contenido.append("Nro. Vacunas: ").append(modeloTabla.getValueAt(i, 4)).append("\n");
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
            mCorreo.setSubject("Listado de Mascotas Registradas");
            mCorreo.setText(contenido.toString(), "UTF-8", "plain");

            Transport mTransport = mSession.getTransport("smtp");
            mTransport.connect(emailFrom, passwordFrom);
            mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
            mTransport.close();

            mensaje("Correo enviado correctamente", "Confirmación");
        } catch (MessagingException ex) {
            Logger.getLogger(JFMascota.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    // Método listar
    void listar() {
        limpiarTabla();
        if (m.tamaño() > 0) {
            for (int i = 0; i < m.tamaño(); i++) {
                Mascota masco = m.obtener(i);
                Object[] fila = {
                    masco.getCodigo(),
                    masco.getEdad(),
                    masco.getRaza(),
                    masco.getPeso(),
                    masco.getNumeroVacunas()
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de Mascotas: " + m.tamaño(), "Información");
        } else {
            mensaje("No hay mascotas registradas", "Información");
        }
    }

    // Guardar en archivo
    void guardarEnArchivo(Mascota masco) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("mascotas.txt", true))) {
            bw.write("Código: " + masco.getCodigo() + ", ");
            bw.write("Edad: " + masco.getEdad() + ", ");
            bw.write("Raza: " + masco.getRaza() + ", ");
            bw.write("Peso: " + masco.getPeso() + "kg, ");
            bw.write("Nro. de vacunas: " + masco.getNumeroVacunas());
            bw.newLine();
        } catch (IOException e) {
            mensaje("Error al guardar en archivo: " + e.getMessage(), "Error");
        }
    }

    // Método ingresar
    void ingresar() {
        Mascota masco = m.buscar(getCodigo());
        if (masco == null) {
            int codigo = getCodigo();
            int edad = getEdad();
            String raza = getRaza();
            double peso = getPeso();
            int numVacunas = getNumeroVacunas();

            masco = new Mascota(codigo, edad, raza, peso, numVacunas);
            m.adicionar(masco);
            guardarEnArchivo(masco);
            listar();
            mensaje("Mascota ingresada correctamente", "Confirmación");
        } else {
            mensaje("Código ya registrado", "Error");
        }
    }

    // Método consultar
    void consultar() {
        limpiarTabla();
        Mascota masco = m.buscar(getCodigo());
        if (masco != null) {
            Object[] fila = {
                masco.getCodigo(),
                masco.getEdad(),
                masco.getRaza(),
                masco.getPeso(),
                masco.getNumeroVacunas()
            };
            modeloTabla.addRow(fila);
        } else {
            mensaje("Mascota no existe", "Error");
        }
    }

    // Método modificar
    void modificar() {
        Mascota masco = m.buscar(getCodigo());
        if (masco != null) {
            masco.setEdad(getEdad());
            masco.setRaza(getRaza());
            masco.setPeso(getPeso());
            masco.setNumeroVacunas(getNumeroVacunas());
            listar();
            mensaje("Mascota modificada correctamente", "Confirmación");
        } else {
            mensaje("Mascota no existe", "Error");
        }
    }

    // Método eliminar
    void eliminar() {
        Mascota masco = m.buscar(getCodigo());
        if (masco != null) {
            m.eliminar(masco);
            listar();
            mensaje("Mascota eliminada correctamente", "Confirmación");
        } else {
            mensaje("Mascota no existe", "Error");
        }
    }

    // Método borrar campos
    void borrar() {
        txtCodigo.setText("");
        txtEdad.setText("");
        txtRaza.setText("");
        txtPeso.setText("");
        txtNumero.setText("");
        txtCodigo.requestFocus();
    }

    // Método procesar
    void procesar() {
        switch (cboOpcion.getSelectedIndex()) {
            case 0:
                ingresar();
                break;
            case 1:
                consultar();
                break;
            case 2:
                modificar();
                break;
            default:
                eliminar();
        }
    }

    void seleccionar() {
        int opcion = cboOpcion.getSelectedIndex();
        boolean visible = (opcion == 0 || opcion == 2);

        txtEdad.setVisible(visible);
        lblEdad.setVisible(visible);
        txtRaza.setVisible(visible);
        lblRaza.setVisible(visible);
        txtPeso.setVisible(visible);
        lblPeso.setVisible(visible);
        lblKg.setVisible(visible);
        txtNumero.setVisible(visible);
        lblNumero.setVisible(visible);
    }

    void cerrar() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtPeso = new javax.swing.JTextField();
        txtNumero = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        lblRaza = new javax.swing.JLabel();
        lblPeso = new javax.swing.JLabel();
        lblNumero = new javax.swing.JLabel();
        cboOpcion = new javax.swing.JComboBox<>();
        txtCodigo = new javax.swing.JTextField();
        txtRaza = new javax.swing.JTextField();
        btnLimpiar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lblEdad = new javax.swing.JLabel();
        txtEdad = new javax.swing.JTextField();
        lblKg = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Opcion:");

        lblCodigo.setText("Codigo de la mascota:");

        lblRaza.setText("Raza:");

        lblPeso.setText("Peso:");

        lblNumero.setText("Nro. Vacunas:");

        cboOpcion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ingresar", "consultar", "modificar", "eliminar" }));
        cboOpcion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOpcionItemStateChanged(evt);
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

        btnCerrar.setText("Cerrar");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel2.setText("REGISTRO DE MASCOTA");

        lblEdad.setText("Edad:");

        lblKg.setText("KG.");

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
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(140, 140, 140)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(82, 82, 82)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblPeso)
                                    .addComponent(lblRaza))
                                .addGap(27, 27, 27)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtPeso, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(lblKg, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtRaza, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblCodigo)
                                        .addGap(26, 26, 26))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(lblEdad, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtEdad, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(73, 73, 73)
                                .addComponent(jLabel1)
                                .addGap(26, 26, 26)
                                .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lblNumero)
                                .addGap(28, 28, 28)
                                .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(84, 84, 84)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(btnLimpiar)
                                .addGap(34, 34, 34)
                                .addComponent(btnCerrar))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(53, 53, 53)
                                .addComponent(btnProcesar))
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(55, 55, 55))))
            .addGroup(layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 541, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(226, 226, 226))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCodigo)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEdad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEdad))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblRaza)
                            .addComponent(txtRaza, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPeso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPeso)
                            .addComponent(lblKg)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnLimpiar)
                                    .addComponent(btnCerrar))
                                .addGap(61, 61, 61))
                            .addComponent(btnProcesar, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addComponent(btnCorreo)))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumero)
                    .addComponent(txtNumero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
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
        enviarCorreoConMascotas();
    }//GEN-LAST:event_btnCorreoActionPerformed

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JFMascota.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFMascota.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFMascota.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFMascota.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFMascota().setVisible(true);
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
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblEdad;
    private javax.swing.JLabel lblKg;
    private javax.swing.JLabel lblNumero;
    private javax.swing.JLabel lblPeso;
    private javax.swing.JLabel lblRaza;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtEdad;
    private javax.swing.JTextField txtNumero;
    private javax.swing.JTextField txtPeso;
    private javax.swing.JTextField txtRaza;
    // End of variables declaration//GEN-END:variables
}
