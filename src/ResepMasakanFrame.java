import database.Koneksi;
import database.RecipeDAO;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author abtme
 */
public class ResepMasakanFrame extends javax.swing.JFrame {
    public ResepMasakanFrame() {
        initComponents();
        loadRecipes();  // Memuat resep ke dalam JTable ketika aplikasi dimulai
        setLocationRelativeTo(null); // Pusatkan di layar
        setLayout(null);
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterRecipes();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterRecipes();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterRecipes();
            }
        });

    }
    
    private void filterRecipes() {
        String keyword = txtSearch.getText().toLowerCase().trim();

        // Jika input kosong, tidak perlu query
        if (keyword.isEmpty()) {
            loadRecipes();  // Memuat ulang semua data
            return;
        }

        String sql = "SELECT * FROM recipes WHERE LOWER(name) LIKE ? OR LOWER(code) LIKE ? OR LOWER(category) LIKE ?";

        try (PreparedStatement stmt = Koneksi.getConnection().prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%"; // Pola pencarian
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            // Eksekusi query
            try (ResultSet rs = stmt.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) tableRecipes.getModel();
                model.setRowCount(0); // Menghapus data lama

                while (rs.next()) {
                    String code = rs.getString("code");
                    String name = rs.getString("name");
                    String category = rs.getString("category");
                    String tools = rs.getString("tools");
                    String ingredients = rs.getString("ingredients");
                    String steps = rs.getString("steps");

                    // Menambahkan data ke tabel
                    model.addRow(new Object[]{code, name, category, tools, ingredients, steps});
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtCode.setText("");
        txtName.setText("");
        cmbCategory.setSelectedIndex(0);
        areaTools.setText("");
        areaIngredients.setText("");
        areaSteps.setText("");
    }
        
    private void loadRecipes() {
        String[] columnNames = {"Code", "Name", "Category", "Tools", "Ingredients", "Steps"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0); // 0 baris data

        // Ambil data dari database
        RecipeDAO dao = new RecipeDAO();
        try {
            ResultSet rs = dao.getAllRecipes(); // Ambil semua data resep
            while (rs.next()) {
                String code = rs.getString("code");
                String name = rs.getString("name");
                String category = rs.getString("category");
                String tools = rs.getString("tools");
                String ingredients = rs.getString("ingredients");
                String steps = rs.getString("steps");

                // Menambahkan baris ke model tabel
                model.addRow(new Object[]{code, name, category, tools, ingredients, steps});
            }

            // Set model ke JTable
            tableRecipes.setModel(model);

            // Set Auto Resize Mode untuk kolom agar bisa menyesuaikan lebar
            tableRecipes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // Mengatur lebar kolom secara manual ke 100
            setColumnWidths(tableRecipes, 100); // Lebar kolom 100

            // Mengatur tinggi baris menjadi 50
            tableRecipes.setRowHeight(50); // Set tinggi baris menjadi 50

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load recipes", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setColumnWidths(JTable table, int width) {
        // Menyesuaikan lebar setiap kolom ke ukuran yang ditentukan (100 dalam hal ini)
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
        }
    }
    
    private void saveDataToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");

        // Set filter untuk hanya memilih file CSV
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(null);  // Tampilkan dialog simpan file
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Jika file tidak memiliki ekstensi .csv, tambahkan ekstensi .csv
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }

            // Panggil fungsi untuk menyimpan data ke CSV
            saveTableToCSV(file.getAbsolutePath());
        }
    }
    
    private void saveTableToCSV(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Ambil model dari JTable
            DefaultTableModel model = (DefaultTableModel) tableRecipes.getModel();

            // Menulis header kolom dengan pemisah ;
            writer.write("Code;Name;Category;Tools;Ingredients;Steps"); // Sesuaikan dengan kolom tabel Anda
            writer.newLine();

            // Menulis data baris per baris
            for (int i = 0; i < model.getRowCount(); i++) {
                String code = model.getValueAt(i, 0).toString();
                String name = model.getValueAt(i, 1).toString();
                String category = model.getValueAt(i, 2).toString();
                String tools = model.getValueAt(i, 3).toString();
                String ingredients = model.getValueAt(i, 4).toString();
                String steps = model.getValueAt(i, 5).toString();

                // Membungkus steps dengan tanda kutip agar tetap berada di kolom yang sama meskipun ada line break
                steps = "\"" + steps.replace("\"", "\"\"") + "\"";  // Escape quotes dalam langkah-langkah

                // Menulis data ke file dengan pemisah ;
                writer.write(code + ";");
                writer.write(name + ";");
                writer.write(category + ";");
                writer.write(tools + ";");
                writer.write(ingredients + ";");
                writer.write(steps);  // Menulis langkah-langkah
                writer.newLine();  // Menambahkan baris baru setelah setiap data
            }

            // Menampilkan pesan jika berhasil
            JOptionPane.showMessageDialog(null, "Data berhasil disimpan ke file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menyimpan data.");
        }
    }

    // Method untuk menyimpan data ke database
    private void saveDataToDatabase(String code, String name, String category, String tools, String ingredients, String steps) throws SQLException {
        RecipeDAO recipeDAO = new RecipeDAO();
        recipeDAO.addRecipe(code, name, category, tools, ingredients, steps);
    }
    
    // Method untuk membuka File Chooser dan memproses CSV
    private void importCSV() {
        // Membuat JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih File CSV");

        // Menyaring hanya file CSV
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);

        // Menampilkan dialog untuk memilih file
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            // Setelah file dipilih, proses validasi dan penyimpanan data CSV
            uploadCSVToDatabase(filePath);
        }
    }

    // Method untuk upload CSV dan validasi data
    private void uploadCSVToDatabase(String filePath) {
        StringBuilder duplicateWarnings = new StringBuilder(); // Menampung pesan duplikat
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            // Skip header jika ada
            line = reader.readLine();
            lineNumber++;

            // Membaca baris file CSV satu per satu
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] data = line.split(";");

                // Validasi jumlah kolom dalam CSV
                if (data.length < 6) {
                    System.out.println("Baris " + lineNumber + " memiliki format yang salah, melewati...");
                    continue; // Lewati baris yang tidak valid
                }

                // Menghilangkan tanda petik di sekitar data jika ada
                String code = data[0].trim().replaceAll("\"", "").replaceAll("'", "");
                String name = data[1].trim().replaceAll("\"", "").replaceAll("'", "");
                String category = data[2].trim().replaceAll("\"", "").replaceAll("'", "");
                String tools = data[3].trim().replaceAll("\"", "").replaceAll("'", "");
                String ingredients = data[4].trim().replaceAll("\"", "").replaceAll("'", "");
                String steps = data[5].trim().replaceAll("\"", "").replaceAll("'", "");

                // Validasi apakah code sudah ada di database
                RecipeDAO recipeDAO = new RecipeDAO();
                if (!recipeDAO.isCodeExists(code)) {
                    // Jika Code belum ada, simpan ke database
                    saveDataToDatabase(code, name, category, tools, ingredients, steps);
                } else {
                    // Data sudah ada, tambahkan ke peringatan
                    duplicateWarnings.append("Code ").append(code)
                            .append(" sudah ada di database, baris ke-").append(lineNumber).append("\n");
                }
            }

            // Setelah proses upload selesai, panggil loadRecipes() untuk memperbarui JTable
            loadRecipes();

            // Tampilkan peringatan jika ada duplikat
            if (duplicateWarnings.length() > 0) {
                JOptionPane.showMessageDialog(null, duplicateWarnings.toString(), 
                        "Peringatan Data Duplikat", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Upload CSV selesai dan data berhasil dimuat ulang!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat membaca file.");
        } catch (SQLException ex) {
            Logger.getLogger(ResepMasakanFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menyimpan data ke database.");
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        menuPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        bodyPanel = new javax.swing.JPanel();
        homePanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        entryPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        cmbCategory = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        areaTools = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        areaIngredients = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        areaSteps = new javax.swing.JTextArea();
        btnSave = new javax.swing.JButton();
        btnSave1 = new javax.swing.JButton();
        dataPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableRecipes = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        btnImport = new javax.swing.JButton();
        btnSaveCSV = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        viewPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        areaRecipe = new javax.swing.JTextArea();
        jLabel16 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cooking Recipes App");
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(188, 210, 204));

        menuPanel.setBackground(new java.awt.Color(188, 210, 204));
        menuPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));

        jLabel1.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("My");

        jLabel2.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Recipes");

        jLabel3.setFont(new java.awt.Font("Trebuchet MS", 0, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Books");

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon-main.png"))); // NOI18N

        jButton1.setText("Entry");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Exit");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Home");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout menuPanelLayout = new javax.swing.GroupLayout(menuPanel);
        menuPanel.setLayout(menuPanelLayout);
        menuPanelLayout.setHorizontalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap(29, Short.MAX_VALUE))
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        menuPanelLayout.setVerticalGroup(
            menuPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(menuPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jLabel4)
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bodyPanel.setBackground(new java.awt.Color(188, 210, 204));
        bodyPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 2));
        bodyPanel.setLayout(new java.awt.CardLayout());

        homePanel.setBackground(new java.awt.Color(188, 210, 204));
        homePanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/icon-cook.png"))); // NOI18N

        jLabel15.setFont(new java.awt.Font("Trebuchet MS", 1, 24)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Cooking Recipes App");

        jLabel28.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("__________________________________________");

        javax.swing.GroupLayout homePanelLayout = new javax.swing.GroupLayout(homePanel);
        homePanel.setLayout(homePanelLayout);
        homePanelLayout.setHorizontalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(homePanelLayout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addGroup(homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel28))
                .addContainerGap(47, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(77, 77, 77))
        );
        homePanelLayout.setVerticalGroup(
            homePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, homePanelLayout.createSequentialGroup()
                .addContainerGap(61, Short.MAX_VALUE)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addGap(32, 32, 32)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67))
        );

        bodyPanel.add(homePanel, "card2");

        entryPanel.setBackground(new java.awt.Color(188, 210, 204));

        jLabel6.setFont(new java.awt.Font("Trebuchet MS", 1, 20)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Recipes");

        jLabel7.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Code");

        jLabel8.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Name");

        jLabel9.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Category");

        jLabel10.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Tools");

        jLabel11.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Ingredients");

        jLabel12.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Steps");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Appetizers", "Main Course", "Desserts", "Soups", "Salads", "Snacks", "Beverages", "Breakfast", "Side Dishes", "Vegetarian" }));

        areaTools.setColumns(20);
        areaTools.setRows(5);
        jScrollPane1.setViewportView(areaTools);

        areaIngredients.setColumns(20);
        areaIngredients.setRows(5);
        jScrollPane4.setViewportView(areaIngredients);

        areaSteps.setColumns(20);
        areaSteps.setRows(5);
        jScrollPane5.setViewportView(areaSteps);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnSave1.setText("Clear");
        btnSave1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSave1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout entryPanelLayout = new javax.swing.GroupLayout(entryPanel);
        entryPanel.setLayout(entryPanelLayout);
        entryPanelLayout.setHorizontalGroup(
            entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entryPanelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(entryPanelLayout.createSequentialGroup()
                            .addComponent(btnSave1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(entryPanelLayout.createSequentialGroup()
                            .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel7)
                                .addComponent(jLabel8)
                                .addComponent(jLabel9)
                                .addComponent(jLabel11)
                                .addComponent(jLabel10)
                                .addComponent(jLabel12))
                            .addGap(30, 30, 30)
                            .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(txtName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                                .addComponent(txtCode, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(cmbCategory, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.LEADING)))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        entryPanelLayout.setVerticalGroup(
            entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(entryPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel6)
                .addGap(27, 27, 27)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(18, 18, 18)
                .addGroup(entryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSave1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        bodyPanel.add(entryPanel, "card3");

        dataPanel.setBackground(new java.awt.Color(188, 210, 204));
        dataPanel.setPreferredSize(new java.awt.Dimension(550, 620));

        tableRecipes.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tableRecipes);

        jLabel13.setFont(new java.awt.Font("Trebuchet MS", 1, 20)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Recipes Data");

        jLabel14.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Search");

        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        btnSaveCSV.setText("Export");
        btnSaveCSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveCSVActionPerformed(evt);
            }
        });

        btnView.setText("View");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });

        jButton6.setText("Edit");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        btnDelete.setText("Delete");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataPanelLayout = new javax.swing.GroupLayout(dataPanel);
        dataPanel.setLayout(dataPanelLayout);
        dataPanelLayout.setHorizontalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, dataPanelLayout.createSequentialGroup()
                        .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 501, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dataPanelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        dataPanelLayout.setVerticalGroup(
            dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataPanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(dataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(btnView, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveCSV, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        bodyPanel.add(dataPanel, "card4");

        viewPanel.setBackground(new java.awt.Color(188, 210, 204));

        areaRecipe.setColumns(20);
        areaRecipe.setRows(5);
        jScrollPane3.setViewportView(areaRecipe);

        jLabel16.setFont(new java.awt.Font("Trebuchet MS", 1, 20)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Detail Recipe");

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(0, 18, Short.MAX_VALUE))
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 546, Short.MAX_VALUE)
                .addContainerGap())
        );

        bodyPanel.add(viewPanel, "card5");

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(menuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                    .addComponent(menuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // Menghapus semua komponen yang saat ini ada di bodyPanel
        bodyPanel.removeAll();

        // Menambahkan panel baru (entryPanel) ke bodyPanel
        bodyPanel.add(homePanel);

        // Memperbarui tampilan bodyPanel setelah perubahan dengan me-repaint (menggambar ulang) komponennya
        bodyPanel.repaint();

        // Memvalidasi ulang tata letak bodyPanel agar komponen baru ditampilkan dengan benar
        bodyPanel.revalidate();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Menghapus semua komponen yang saat ini ada di bodyPanel
        bodyPanel.removeAll();

        // Menambahkan panel baru (dataPanel) ke bodyPanel
        bodyPanel.add(dataPanel);

        // Memperbarui tampilan bodyPanel setelah perubahan dengan me-repaint (menggambar ulang) komponennya
        bodyPanel.repaint();

        // Memvalidasi ulang tata letak bodyPanel agar komponen baru ditampilkan dengan benar
        bodyPanel.revalidate();
        
        //Memanggil method load data untuk merefresh resep saat menekan tombol data
        loadRecipes();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Menghapus semua komponen yang saat ini ada di bodyPanel
        bodyPanel.removeAll();

        // Menambahkan panel baru (entryPanel) ke bodyPanel
        bodyPanel.add(entryPanel);

        // Memperbarui tampilan bodyPanel setelah perubahan dengan me-repaint (menggambar ulang) komponennya
        bodyPanel.repaint();

        // Memvalidasi ulang tata letak bodyPanel agar komponen baru ditampilkan dengan benar
        bodyPanel.revalidate();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Tampilkan dialog konfirmasi
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to close this app?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        // Jika user memilih "YES", tutup aplikasi
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // Ambil data dari field input
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        String category = cmbCategory.getSelectedItem().toString();
        String tools = areaTools.getText().trim();
        String ingredients = areaIngredients.getText().trim();
        String steps = areaSteps.getText().trim();

        // Validasi input (opsional)
        if (name.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return; // Menghentikan proses jika ada field yang kosong
        }

        // Menyimpan data ke database (baik untuk baru atau update)
        RecipeDAO dao = new RecipeDAO();

        try {
            // Cek apakah code sudah ada di database
            boolean isCodeExists = dao.isCodeExists(code); // Fungsi untuk memeriksa keberadaan code di database

            // Jika code sudah ada, update data
            if (isCodeExists) {
                // Update resep yang sudah ada
                dao.updateRecipe(code, name, category, tools, ingredients, steps);
                JOptionPane.showMessageDialog(this, "Recipe updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Jika code belum ada, tambahkan resep baru
                dao.addRecipe(code, name, category, tools, ingredients, steps);
                JOptionPane.showMessageDialog(this, "New recipe added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            // Setelah menyimpan, update tampilan tabel
            loadRecipes();  // Memuat ulang data ke JTable

            // Menyembunyikan panel entry setelah save dan menampilkan panel view
            entryPanel.setVisible(false); 
            viewPanel.setVisible(true); 

        } catch (SQLException e) {
            // Tangani pengecualian SQLException dengan memberikan pesan yang lebih jelas
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to save data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Tangani pengecualian lainnya yang mungkin terjadi
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
        // Ambil data yang dipilih dari JTable
        int selectedRow = tableRecipes.getSelectedRow(); // Mendapatkan baris yang dipilih

        if (selectedRow != -1) { // Pastikan ada baris yang dipilih
            // Ambil data dari baris yang dipilih
            String code = tableRecipes.getValueAt(selectedRow, 0).toString();
            String name = tableRecipes.getValueAt(selectedRow, 1).toString();
            String category = tableRecipes.getValueAt(selectedRow, 2).toString();
            String tools = tableRecipes.getValueAt(selectedRow, 3).toString();
            String ingredients = tableRecipes.getValueAt(selectedRow, 4).toString();
            String steps = tableRecipes.getValueAt(selectedRow, 5).toString();

            // Gabungkan data menjadi satu string atau format sesuai kebutuhan
            String recipeDetails = "Name: \n" + name + "\n\n" +
                                   "Category: \n" + category + "\n\n" +
                                   "Tools: \n" + tools + "\n\n" +
                                   "Ingredients: \n" + ingredients + "\n\n" +
                                   "Steps: \n" + steps;

            // Menghapus semua komponen yang saat ini ada di bodyPanel
            bodyPanel.removeAll();

            // Menambahkan panel baru (viewPanel) ke bodyPanel
            bodyPanel.add(viewPanel);

            // Memperbarui tampilan bodyPanel setelah perubahan dengan me-repaint (menggambar ulang) komponennya
            bodyPanel.repaint();

            // Memvalidasi ulang tata letak bodyPanel agar komponen baru ditampilkan dengan benar
            bodyPanel.revalidate();

            areaRecipe.setText(recipeDetails); // Anggap textArea adalah JTextArea yang ada di viewPanel

        } else {
            // Jika tidak ada baris yang dipilih, tampilkan peringatan
            JOptionPane.showMessageDialog(this, "Please select a recipe to view.", "No Recipe Selected", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btnViewActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        int selectedRow = tableRecipes.getSelectedRow(); // Mendapatkan baris yang dipilih
    
        if (selectedRow != -1) { // Jika ada baris yang dipilih
            // Ambil data dari tabel berdasarkan baris yang dipilih
            String code = tableRecipes.getValueAt(selectedRow, 0).toString();
            String name = tableRecipes.getValueAt(selectedRow, 1).toString();
            String category = tableRecipes.getValueAt(selectedRow, 2).toString();
            String tools = tableRecipes.getValueAt(selectedRow, 3).toString();
            String ingredients = tableRecipes.getValueAt(selectedRow, 4).toString();
            String steps = tableRecipes.getValueAt(selectedRow, 5).toString();

            // Menampilkan data di field input dan combo box
            txtCode.setText(code);
            txtName.setText(name);
            cmbCategory.setSelectedItem(category); // Mengatur nilai combo box sesuai kategori
            areaTools.setText(tools);
            areaIngredients.setText(ingredients);
            areaSteps.setText(steps);

            // Menampilkan panel entry untuk edit
            // Menghapus semua komponen yang saat ini ada di bodyPanel
            bodyPanel.removeAll();

            // Menambahkan panel baru (entryPanel) ke bodyPanel
            bodyPanel.add(entryPanel);

            // Memperbarui tampilan bodyPanel setelah perubahan dengan me-repaint (menggambar ulang) komponennya
            bodyPanel.repaint();

            // Memvalidasi ulang tata letak bodyPanel agar komponen baru ditampilkan dengan benar
            bodyPanel.revalidate();
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // Mendapatkan baris yang dipilih di JTable
        int selectedRow = tableRecipes.getSelectedRow();

        // Jika tidak ada baris yang dipilih, tampilkan pesan error
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a recipe to delete", "Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mengambil 'code' dari baris yang dipilih (misalnya, kolom pertama berisi 'code')
        String code = tableRecipes.getValueAt(selectedRow, 0).toString();
        String recipeName = tableRecipes.getValueAt(selectedRow, 1).toString(); // Ambil nama resep untuk ditampilkan dalam konfirmasi

        // Menampilkan dialog konfirmasi
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the recipe \"" + recipeName + "\"?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        // Jika pengguna memilih "YES", lanjutkan penghapusan
        if (confirm == JOptionPane.YES_OPTION) {
            // Membuat instance RecipeDAO dan mencoba menghapus resep
            RecipeDAO dao = new RecipeDAO();
            boolean success = dao.deleteRecipe(code); // Memanggil metode deleteRecipe dari DAO

            if (success) {
                JOptionPane.showMessageDialog(this, "Recipe deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Memuat ulang data di tabel
                loadRecipes(); // Memanggil metode untuk memuat ulang data resep ke JTable
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete recipe", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSave1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSave1ActionPerformed
        clearForm();
    }//GEN-LAST:event_btnSave1ActionPerformed

    private void btnSaveCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveCSVActionPerformed
        saveDataToFile();
    }//GEN-LAST:event_btnSaveCSVActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        importCSV();
    }//GEN-LAST:event_btnImportActionPerformed

    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(ResepMasakanFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ResepMasakanFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ResepMasakanFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ResepMasakanFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ResepMasakanFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaIngredients;
    private javax.swing.JTextArea areaRecipe;
    private javax.swing.JTextArea areaSteps;
    private javax.swing.JTextArea areaTools;
    private javax.swing.JPanel bodyPanel;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSave1;
    private javax.swing.JButton btnSaveCSV;
    private javax.swing.JButton btnView;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JPanel entryPanel;
    private javax.swing.JPanel homePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel menuPanel;
    private javax.swing.JTable tableRecipes;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JPanel viewPanel;
    // End of variables declaration//GEN-END:variables
}
