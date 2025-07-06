package view;

import controller.DatosCompartidos;
import controller.ArregloCliente;
import controller.ExportarExcel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Cliente;
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

public class JFCliente extends javax.swing.JFrame {
    
    ArregloCliente c = DatosCompartidos.clientes;
    DefaultTableModel modeloTabla;

    public JFCliente() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
    }
    
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("DNI");
        modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Edad");
        modeloTabla.addColumn("Código Mascota");
        Table.setModel(modeloTabla);
    }
    void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    String getDni() {
        return txtDni.getText().trim();
    }

    String getNombre() {
        return txtNombre.getText().trim();
    }

    int getEdad() {
        return Integer.parseInt(txtEdad.getText().trim());
    }

    int getCodigoMascota() {
        return Integer.parseInt(txtCodigoMascota.getText().trim());
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void enviarCorreoConClientes() {
        String emailFrom = "maxpower14567@gmail.com";
        String passwordFrom = "kkmd lcni mfoe udcv";
        String emailTo = JOptionPane.showInputDialog(this, "Ingrese el correo de destino:", "Enviar Clientes por Correo", JOptionPane.QUESTION_MESSAGE);

        if (emailTo == null || emailTo.trim().isEmpty()) {
            mensaje("No se ingresó correo de destino", "Error");
            return;
        }

        // Construir el contenido del correo desde el JTable
        StringBuilder contenido = new StringBuilder();
        contenido.append("Listado de clientes registrados en la veterinaria:\n\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            contenido.append("DNI: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("Nombre: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Edad: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Código Mascota: ").append(modeloTabla.getValueAt(i, 3)).append("\n");
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
            mCorreo.setText(contenido.toString(), "UTF-8", "plain");

            Transport mTransport = mSession.getTransport("smtp");
            mTransport.connect(emailFrom, passwordFrom);
            mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
            mTransport.close();

            mensaje("Correo enviado correctamente", "Confirmación");
        } catch (MessagingException ex) {
            Logger.getLogger(JFCliente.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    // Método listar todos los clientes en el Table
    void listar() {
        limpiarTabla();
        if (c.tamaño() > 0) {
            for (int i = 0; i < c.tamaño(); i++) {
                Cliente cli = c.obtener(i);
                Object[] fila = {
                    cli.getDni(),
                    cli.getNombre(),
                    cli.getEdad(),
                    cli.getCodigoMascota()
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de Clientes: " + c.tamaño(), "Información");
        } else {
            mensaje("No hay clientes registrados", "Información");
        }
    }

    // Método ingresar
    void ingresar() {
        Cliente cli = c.buscarPorDni(getDni());
        if (cli == null) {
            String dni = getDni();
            String nombre = getNombre();
            int edad = getEdad();
            int codigoMascota = getCodigoMascota();

            cli = new Cliente(dni, nombre, edad, codigoMascota);
            c.adicionar(cli);

            // Guardar en archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("clientes.txt", true))) {
                writer.write("DNI: " + dni + ", Nombre: " + nombre + ", Edad: " + edad + ", Código Mascota: " + codigoMascota);
                writer.newLine();
            } catch (IOException e) {
                mensaje("Error al guardar el cliente en el archivo", "Error");
            }

            listar();
            mensaje("Cliente ingresado correctamente", "Confirmación");
        } else {
            mensaje("DNI ya registrado", "Error");
        }
    }

    // Método consultar un cliente y mostrarlo en el Table
    void consultar() {
        limpiarTabla();
        Cliente cli = c.buscarPorDni(getDni());
        if (cli != null) {
            Object[] fila = {
                cli.getDni(),
                cli.getNombre(),
                cli.getEdad(),
                cli.getCodigoMascota()
            };
            modeloTabla.addRow(fila);
        } else {
            mensaje("Cliente no existe", "Error");
        }
    }

    // Método modificar
    void modificar() {
        Cliente cli = c.buscarPorDni(getDni());
        if (cli != null) {
            cli.setNombre(getNombre());
            cli.setEdad(getEdad());
            cli.setCodigoMascota(getCodigoMascota());
            listar();
            mensaje("Cliente modificado correctamente", "Confirmación");
        } else {
            mensaje("Cliente no existe", "Error");
        }
    }

    // Método eliminar
    void eliminar() {
        Cliente cli = c.buscarPorDni(getDni());
        if (cli != null) {
            c.eliminar(cli);
            listar();
            mensaje("Cliente eliminado correctamente", "Confirmación");
        } else {
            mensaje("Cliente no existe", "Error");
        }
    }

    // Método borrar campos de entrada
    void borrar() {
        txtDni.setText("");
        txtNombre.setText("");
        txtEdad.setText("");
        txtCodigoMascota.setText("");
        txtDni.requestFocus();
    }

    // Método procesar según opción del combo
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

    // Mostrar u ocultar campos según opción seleccionada
    void seleccionar() {
        int opcion = cboOpcion.getSelectedIndex();
        boolean visible = (opcion == 0 || opcion == 2); // ingresar o modificar

        txtNombre.setVisible(visible);
        lblNombre.setVisible(visible);
        txtEdad.setVisible(visible);
        lblEdad.setVisible(visible);
        txtCodigoMascota.setVisible(visible);
        lblCodigoMascota.setVisible(visible);
    }

    void cerrar() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        cboOpcion = new javax.swing.JComboBox<>();
        txtDni = new javax.swing.JTextField();
        txtEdad = new javax.swing.JTextField();
        btnLimpiar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtCodigoMascota = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        lblEdad = new javax.swing.JLabel();
        lblCodigoMascota = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel2.setText("REGISTRO DE CLIENTES");

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

        lblNombre.setText("Nom. y Apelli.:");

        jLabel1.setText("Opcion:");

        lblCodigo.setText("DNI:");

        lblEdad.setText("Edad:");

        lblCodigoMascota.setText("Codigo de mascota:");

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(163, 163, 163)
                                    .addComponent(lblCodigo)
                                    .addGap(53, 53, 53))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(83, 83, 83)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblCodigoMascota)
                                        .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(32, 32, 32)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addGap(53, 53, 53))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(lblEdad)
                                        .addGap(59, 59, 59)))))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtNombre)
                                .addGap(27, 27, 27)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(92, 92, 92)
                                        .addComponent(btnCerrar)
                                        .addGap(13, 13, 13))
                                    .addComponent(btnLimpiar)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(39, 39, 39)
                                        .addComponent(btnProcesar))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtEdad, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtCodigoMascota, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(24, 24, 24))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(162, 204, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(38, 38, 38))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(142, 142, 142))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(236, 236, 236))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCodigo))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNombre))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtEdad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblEdad))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCodigoMascota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCodigoMascota)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLimpiar)
                            .addComponent(btnCerrar))
                        .addGap(12, 12, 12)
                        .addComponent(btnProcesar)
                        .addGap(18, 18, 18)
                        .addComponent(btnCorreo)
                        .addGap(12, 12, 12)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
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
        enviarCorreoConClientes();
    }//GEN-LAST:event_btnCorreoActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFCliente().setVisible(true);
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
    private javax.swing.JLabel lblCodigoMascota;
    private javax.swing.JLabel lblEdad;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JTextField txtCodigoMascota;
    private javax.swing.JTextField txtDni;
    private javax.swing.JTextField txtEdad;
    private javax.swing.JTextField txtNombre;
    // End of variables declaration//GEN-END:variables
}
