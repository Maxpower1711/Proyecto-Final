package view;

import controller.ArregloEmpleado;
import controller.DatosCompartidos;
import controller.ExportarExcel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Empleado;
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

public class JFEmpleado extends javax.swing.JFrame {
    
    ArregloEmpleado e = DatosCompartidos.empleados;
    DefaultTableModel modeloTabla;

    public JFEmpleado() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
    }
    
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Código");
        modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Cargo");
        modeloTabla.addColumn("Sueldo");
        Table.setModel(modeloTabla);
    }

    void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    int getCodigo() {
        return Integer.parseInt(txtCodigo.getText().trim());
    }

    String getNombre() {
        return txtNombre.getText().trim();
    }

    String getCargo() {
        return cboCargo.getSelectedItem().toString();
    }

    double getSueldo() {
        return Double.parseDouble(txtSueldo.getText().trim());
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void enviarCorreoConEmpleados() {
        String emailFrom = "maxpower14567@gmail.com";
        String passwordFrom = "kkmd lcni mfoe udcv";
        String emailTo = JOptionPane.showInputDialog(this, "Ingrese el correo de destino:", "Enviar Empleados por Correo", JOptionPane.QUESTION_MESSAGE);

        if (emailTo == null || emailTo.trim().isEmpty()) {
            mensaje("No se ingresó correo de destino", "Error");
            return;
        }

        // Construir el contenido del correo desde el JTable
        StringBuilder contenido = new StringBuilder();
        contenido.append("Listado de empleados registrados en la veterinaria:\n\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            contenido.append("Código: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("Nombre: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Cargo: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Sueldo: ").append(modeloTabla.getValueAt(i, 3)).append("\n");
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
            mCorreo.setSubject("Listado de Empleados Registrados");
            mCorreo.setText(contenido.toString(), "UTF-8", "plain");

            Transport mTransport = mSession.getTransport("smtp");
            mTransport.connect(emailFrom, passwordFrom);
            mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
            mTransport.close();

            mensaje("Correo enviado correctamente", "Confirmación");
        } catch (MessagingException ex) {
            Logger.getLogger(JFEmpleado.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    // Listar todos los empleados en la tabla
    void listar() {
        limpiarTabla();
        if (e.tamaño() > 0) {
            for (int i = 0; i < e.tamaño(); i++) {
                Empleado emp = e.obtener(i);
                Object[] fila = {
                    emp.getCodigo(),
                    emp.getNombres(),
                    emp.getCargo(),
                    emp.getSueldo()
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de empleados: " + e.tamaño(), "Información");
        } else {
            mensaje("No hay empleados registrados", "Información");
        }
    }

    // Ingresar un empleado
    void ingresar() {
        Empleado emp = e.buscar(getCodigo());
        if (emp == null) {
            int codigo = getCodigo();
            String nombre = getNombre();
            String cargo = getCargo();
            double sueldo = getSueldo();

            emp = new Empleado(codigo, nombre, cargo, sueldo);
            e.adicionar(emp);

            // Guardar en archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("empleados.txt", true))) {
                writer.write("Código: " + codigo + ", Nombre: " + nombre + ", Cargo: " + cargo + ", Sueldo: " + sueldo);
                writer.newLine();
            } catch (IOException ex) {
                mensaje("Error al guardar el empleado en el archivo", "Error");
            }

            listar();
            mensaje("Empleado ingresado correctamente", "Confirmación");
        } else {
            mensaje("Código ya registrado", "Error");
        }
    }

    // Consultar un empleado
    void consultar() {
        limpiarTabla();
        Empleado emp = e.buscar(getCodigo());
        if (emp != null) {
            Object[] fila = {
                emp.getCodigo(),
                emp.getNombres(),
                emp.getCargo(),
                emp.getSueldo()
            };
            modeloTabla.addRow(fila);
        } else {
            mensaje("Empleado no existe", "Error");
        }
    }

    // Modificar un empleado
    void modificar() {
        Empleado emp = e.buscar(getCodigo());
        if (emp != null) {
            emp.setNombres(getNombre());
            emp.setCargo(getCargo());
            emp.setSueldo(getSueldo());
            listar();
            mensaje("Empleado modificado correctamente", "Confirmación");
        } else {
            mensaje("Empleado no existe", "Error");
        }
    }

    // Eliminar un empleado
    void eliminar() {
        Empleado emp = e.buscar(getCodigo());
        if (emp != null) {
            e.eliminar(emp);
            listar();
            mensaje("Empleado eliminado correctamente", "Confirmación");
        } else {
            mensaje("Empleado no existe", "Error");
        }
    }

    // Borrar campos de entrada
    void borrar() {
        txtCodigo.setText("");
        txtNombre.setText("");
        cboCargo.setSelectedIndex(0);
        txtSueldo.setText("");
        txtCodigo.requestFocus();
    }

    // Procesar según opción del combo
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
        cboCargo.setVisible(visible);
        lblCargo.setVisible(visible);
        txtSueldo.setVisible(visible);
        lblSueldo.setVisible(visible);
    }

    void cerrar() {
        dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtNombre = new javax.swing.JTextField();
        txtSueldo = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        cboOpcion = new javax.swing.JComboBox<>();
        lblCargo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblSueldo = new javax.swing.JLabel();
        btnRegresar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        btnProcesar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        lblNombre = new javax.swing.JLabel();
        cboCargo = new javax.swing.JComboBox<>();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Opcion:");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel2.setText("REGISTRO DE EMPLEADOS");

        lblCodigo.setText("Codigo:");

        cboOpcion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ingresar", "consultar", "modificar", "eliminar" }));
        cboOpcion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOpcionItemStateChanged(evt);
            }
        });

        lblCargo.setText("Cargo:");

        lblSueldo.setText("Sueldo:");

        btnRegresar.setText("REGRESAR");
        btnRegresar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegresarActionPerformed(evt);
            }
        });

        btnLimpiar.setText("Limpiar");
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
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

        jButton4.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton4.setText("Exportar a excel");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        lblNombre.setText("Nom. y Apelli.:");

        cboCargo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--SELECCIONE--", "Asistentes", "Peluquería Canina", "Administrativo", "Personal de Limpieza" }));

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(137, 137, 137)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(lblCodigo))
                        .addGap(43, 43, 43))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSueldo)
                            .addComponent(lblCargo))
                        .addGap(38, 38, 38)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnLimpiar)
                                .addGap(38, 38, 38)
                                .addComponent(btnCerrar))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(btnProcesar)))
                        .addGap(73, 73, 73))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cboCargo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSueldo, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(92, 92, 92))))
            .addGroup(layout.createSequentialGroup()
                .addGap(188, 188, 188)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(252, 252, 252))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addComponent(lblCodigo)
                        .addGap(51, 51, 51)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboCargo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCargo))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSueldo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSueldo))
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblNombre))
                                .addGap(7, 7, 7))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(42, 42, 42)
                                    .addComponent(btnProcesar))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(btnLimpiar)
                                    .addComponent(btnCerrar))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCorreo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegresar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboOpcionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cboOpcionItemStateChanged
        seleccionar();
    }//GEN-LAST:event_cboOpcionItemStateChanged

    private void btnRegresarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegresarActionPerformed
        JFMenuPersonal menuPersonal = new JFMenuPersonal();
        menuPersonal.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_btnRegresarActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        borrar();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnProcesarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProcesarActionPerformed
        procesar();
    }//GEN-LAST:event_btnProcesarActionPerformed

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarActionPerformed
        cerrar();
    }//GEN-LAST:event_btnCerrarActionPerformed

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
        enviarCorreoConEmpleados();
    }//GEN-LAST:event_btnCorreoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFEmpleado().setVisible(true);
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
    private javax.swing.JComboBox<String> cboCargo;
    private javax.swing.JComboBox<String> cboOpcion;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCargo;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblSueldo;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtSueldo;
    // End of variables declaration//GEN-END:variables
}
