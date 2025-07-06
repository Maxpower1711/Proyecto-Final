package view;

import controller.DatosCompartidos;
import controller.ExportarExcel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.Producto;
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

public class JFRecetaMedica extends javax.swing.JFrame {
    
    DefaultTableModel modeloTabla;

    public JFRecetaMedica() {
        initComponents();
        setLocationRelativeTo(null);
        configurarTabla();
    }
    
    void configurarTabla() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Producto");
        modeloTabla.addColumn("Precio");
        modeloTabla.addColumn("Cantidad Vendida");
        modeloTabla.addColumn("Stock");
        Table.setModel(modeloTabla);
    }

    void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    String getNombreProducto() {
        return cboProducto.getSelectedItem().toString();
    }

    double getPrecio() {
        return Double.parseDouble(txtPrecio.getText().trim());
    }

    int getCantidad() {
        return Integer.parseInt(txtCantidad.getText().trim());
    }

    void mensaje(String m, String tipo) {
        JOptionPane.showMessageDialog(this, m, tipo, JOptionPane.INFORMATION_MESSAGE);
    }
    
    void enviarCorreoConProductos() {
        String emailFrom = "maxpower14567@gmail.com";
        String passwordFrom = "kkmd lcni mfoe udcv";
        String emailTo = JOptionPane.showInputDialog(this, "Ingrese el correo de destino:", "Enviar Productos por Correo", JOptionPane.QUESTION_MESSAGE);

        if (emailTo == null || emailTo.trim().isEmpty()) {
            mensaje("No se ingresó correo de destino", "Error");
            return;
        }

        // Construir el contenido del correo desde el JTable
        StringBuilder contenido = new StringBuilder();
        contenido.append("Listado de productos registrados y vendidos en la veterinaria:\n\n");

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            contenido.append("Producto: ").append(modeloTabla.getValueAt(i, 0)).append(", ");
            contenido.append("Precio: ").append(modeloTabla.getValueAt(i, 1)).append(", ");
            contenido.append("Cantidad Vendida: ").append(modeloTabla.getValueAt(i, 2)).append(", ");
            contenido.append("Stock Actual: ").append(modeloTabla.getValueAt(i, 3)).append("\n");
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
            mCorreo.setSubject("Listado de Productos Registrados");
            mCorreo.setText(contenido.toString(), "UTF-8", "plain");

            Transport mTransport = mSession.getTransport("smtp");
            mTransport.connect(emailFrom, passwordFrom);
            mTransport.sendMessage(mCorreo, mCorreo.getRecipients(Message.RecipientType.TO));
            mTransport.close();

            mensaje("Correo enviado correctamente", "Confirmación");
        } catch (MessagingException ex) {
            Logger.getLogger(JFRecetaMedica.class.getName()).log(Level.SEVERE, null, ex);
            mensaje("Error al enviar el correo: " + ex.getMessage(), "Error");
        }
    }

    void listar() {
        limpiarTabla();
        if (DatosCompartidos.productos.tamaño() > 0) {
            for (int i = 0; i < DatosCompartidos.productos.tamaño(); i++) {
                Producto p = DatosCompartidos.productos.obtener(i);
                Object[] fila = {
                    p.getNombre(),
                    p.getPrecio(),
                    p.getCantidad(),
                    p.getStock()
                };
                modeloTabla.addRow(fila);
            }
            mensaje("Total de productos: " + DatosCompartidos.productos.tamaño(), "Información");
        } else {
            mensaje("No hay productos registrados", "Información");
        }
    }

    void ingresar() {
        String nombre = getNombreProducto();
        double precio = getPrecio();
        int cantidadVendida = getCantidad();

        Producto productoExistente = DatosCompartidos.productos.buscarPorNombre(nombre);

        if (productoExistente != null) {
            if (productoExistente.getStock() >= cantidadVendida) {
                // Descontar stock al producto original
                productoExistente.setStock(productoExistente.getStock() - cantidadVendida);

                // Registrar esta venta como un nuevo objeto Producto para historial
                Producto venta = new Producto(nombre, precio, cantidadVendida);
                venta.setStock(productoExistente.getStock()); // mostrar stock actualizado
                DatosCompartidos.productos.adicionar(venta);

                listar();
                mensaje("Producto vendido y stock actualizado correctamente", "Confirmación");

                // Guardar en archivo .txt
                try (PrintWriter pw = new PrintWriter(new FileWriter("recetas.txt", true))) {
                    pw.println("--COMPROBANTE DE PAGO-- \n Producto: " + venta.getNombre());
                    pw.println("Precio: " + venta.getPrecio());
                    pw.println("Cantidad Vendida: " + venta.getCantidad());
                    pw.println("Stock Actual: " + venta.getStock());
                    pw.println("---------------------------------");
                } catch (IOException e) {
                    mensaje("Error al guardar en archivo: " + e.getMessage(), "Error");
                }
            } else {
                mensaje("Stock insuficiente. Stock actual: " + productoExistente.getStock(), "Error");
            }
        } else {
            // Producto no existe, crear con stock inicial de 200
            Producto nuevo = new Producto(nombre, precio, cantidadVendida);
            nuevo.setStock(200 - cantidadVendida);
            DatosCompartidos.productos.adicionar(nuevo);
            listar();
            mensaje("Producto registrado con stock inicial y venta aplicada", "Confirmación");

            // Guardar en archivo .txt
            try (PrintWriter pw = new PrintWriter(new FileWriter("recetas.txt", true))) {
                pw.println("Producto: " + nuevo.getNombre());
                pw.println("Precio: " + nuevo.getPrecio());
                pw.println("Cantidad Vendida: " + nuevo.getCantidad());
                pw.println("Stock Actual: " + nuevo.getStock());
                pw.println("---------------------------------");
            } catch (IOException e) {
                mensaje("Error al guardar en archivo: " + e.getMessage(), "Error");
            }
        }
    }

    void consultar() {
        limpiarTabla();
        Producto p = DatosCompartidos.productos.buscarPorNombre(getNombreProducto());
        if (p != null) {
            Object[] fila = {
                p.getNombre(),
                p.getPrecio(),
                p.getCantidad(),
                p.getStock()
            };
            modeloTabla.addRow(fila);
        } else {
            mensaje("Producto no encontrado", "Error");
        }
    }

    void modificar() {
        Producto p = DatosCompartidos.productos.buscarPorNombre(getNombreProducto());
        if (p != null) {
            p.setPrecio(getPrecio());
            p.setCantidad(getCantidad());
            listar();
            mensaje("Producto modificado correctamente", "Confirmación");
        } else {
            mensaje("Producto no encontrado", "Error");
        }
    }

    void eliminar() {
        Producto p = DatosCompartidos.productos.buscarPorNombre(getNombreProducto());
        if (p != null) {
            DatosCompartidos.productos.eliminar(p);
            listar();
            mensaje("Producto eliminado correctamente", "Confirmación");
        } else {
            mensaje("Producto no encontrado", "Error");
        }
    }

    void borrar() {
        txtPrecio.setText("");
        txtCantidad.setText("");
        cboProducto.setSelectedIndex(0);
        txtPrecio.requestFocus();
    }

    void cerrar() {
        dispose();
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
        boolean visible = (opcion == 0 || opcion == 2); // ingresar o modificar

        txtPrecio.setVisible(visible);
        txtCantidad.setVisible(visible);
        lblPrecio.setVisible(visible);
        lblCantidad.setVisible(visible);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        cboProducto = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cboOpcion = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        lblPrecio = new javax.swing.JLabel();
        txtPrecio = new javax.swing.JTextField();
        btnLimpiar = new javax.swing.JButton();
        btnProcesar = new javax.swing.JButton();
        btnCerrar = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Table = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        btnCorreo = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        jLabel1.setText("RECETA MEDICA");

        cboProducto.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Analgesicos ", "Inmunológicos", "Antiparasitarios", "Vacunas" }));

        jLabel2.setText("Producto:");

        cboOpcion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ingresar", "consultar", "modificar", "eliminar" }));
        cboOpcion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cboOpcionItemStateChanged(evt);
            }
        });

        jLabel6.setText("Opcion:");

        lblCantidad.setText("Cantidad:");

        lblPrecio.setText("Precio:");

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

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jButton2.setText("Exportar a excel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(lblCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cboProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(58, 58, 58)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnLimpiar)
                                .addGap(31, 31, 31)
                                .addComponent(btnProcesar)
                                .addGap(31, 31, 31)
                                .addComponent(btnCerrar))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2))))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(310, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(278, 278, 278))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cboOpcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCantidad))
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPrecio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrecio)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel2)))
                .addGap(38, 38, 38)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpiar)
                    .addComponent(btnProcesar)
                    .addComponent(btnCerrar))
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCorreo)
                .addGap(39, 39, 39))
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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ExportarExcel obj;

        try {
            obj = new ExportarExcel();
            obj.exportarExcel(Table);
        } catch (IOException ex) {
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnCorreoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCorreoActionPerformed
        enviarCorreoConProductos();
    }//GEN-LAST:event_btnCorreoActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new JFRecetaMedica().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable Table;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JButton btnCorreo;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnProcesar;
    private javax.swing.JComboBox<String> cboOpcion;
    private javax.swing.JComboBox<String> cboProducto;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblPrecio;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtPrecio;
    // End of variables declaration//GEN-END:variables
}
