package ir.doorpa.gui;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import ir.doorpa.Xor;
import ir.doorpa.main;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainForm extends JFrame {
    private JPanel mainPanel;
    private JTextField textFieldFilePath;
    private JButton browseButton;
    private JComboBox comboBoxXorSize;
    private JTextField textFieldCurrentValueINT;
    private JTextField textFieldNewValue;
    private JButton setValueButton;
    private JButton exportXORButton;
    private JTextField textFieldCurrentValueHEX;
    private JTextField textFieldCurrentValueBIN;
    private JLabel labelFileSize;
    private JLabel lableStatus;
    private JLabel labelPadding;

    static final private Xor xor = new Xor();
    static private int selectedXorSize = 0;

    private final static String patternHex = "^(0x)[0-9a-f]+$";
    private final static String patternBin = "^(0b)[0-1]+$";
    private final static String patternInt = ("^-?[1-9]\\d*|0$");
    public MainForm() {
        super("Binary XOR");

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                //fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setAcceptAllFileFilterUsed(false);
                /* Set extension filter */
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Binary Files", "bin", "dat"));

                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    main.setBinaryFile(selectedFile);
                    textFieldFilePath.setText(selectedFile.getAbsolutePath());
                    labelFileSize.setText(selectedFile.length() + " Bytes");
                }
            }
        });
        setValueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int inputMode = 0;
                Pattern mypattern;
                Matcher mymatcher;
                String input = textFieldNewValue.getText();
                /* test for input being hex */
                mypattern = Pattern.compile(patternHex, Pattern.CASE_INSENSITIVE);
                mymatcher = mypattern.matcher(input);
                if (mymatcher.matches()){
                    inputMode = 1;
                }

                /* test for input being binary */
                mypattern = Pattern.compile(patternBin, Pattern.CASE_INSENSITIVE);
                mymatcher = mypattern.matcher(input);
                if (mymatcher.matches()){
                    inputMode = 2;
                }
                /* test for input being integer */
                mypattern = Pattern.compile(patternInt, Pattern.CASE_INSENSITIVE);
                mymatcher = mypattern.matcher(input);
                if (mymatcher.matches()){
                    inputMode = 3;
                }

                if (inputMode == 0){
                    /* a match was not been found */
                    lableStatus.setText("not a valid value");
                    return;
                }
                lableStatus.setText("value set");

                /* store the new value according to the selected XOR size */
                BigInteger bigInteger = null;
                byte[] value = null;
                StringBuffer sb = null;
                switch (inputMode){
                    case 1:
                        /* value is in hex */
                        sb = new StringBuffer(input.substring(2));
                        if(sb.length() % 2 != 0){
                            sb.insert(0,"0");
                        }

                        value = HexBin.decode(sb.toString());
                        /* set value in Xor*/
                        xor.setXorValue(value);
                        break;
                    case 2:
                        /* value is in binary */
                        bigInteger = new BigInteger(input.substring(2),2);
                        sb = new StringBuffer(bigInteger.toString(16));
                        if(sb.substring(0,1).equals("-")){
                            sb.deleteCharAt(0);
                        }
                        if(sb.length() % 2 != 0){
                            sb.insert(0,"0");
                        }
                        value = HexBin.decode(sb.toString());
                        /* set value in Xor*/
                        xor.setXorValue(value);
                        break;
                    case 3:
                        /* value is integer */
                        bigInteger = new BigInteger(input,10);
                        /* set value in Xor*/
                        xor.setXorValue(bigInteger.toByteArray());
                        break;
                }

                /* update the 3 modes of display */
                textFieldCurrentValueBIN.setText(xor.keyAsBin());
                textFieldCurrentValueHEX.setText(xor.keyAsHex());
                textFieldCurrentValueINT.setText(xor.keyAsInt());

                /* check for export button enable */
                checkExportButtonEnable();

                File file = main.getBinaryFile();
                if(file != null){
                    int i = xor.neededPadding((int)file.length());
                    if(i != 0){
                        labelPadding.setText("+"+i);
                    }else{
                        labelPadding.setText("None");
                    }
                }

            }
        });
        exportXORButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File result = xor.doXor(main.getBinaryFile());
                    JOptionPane.showMessageDialog(null, "XOR done and exported to " + result.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                }catch (Exception ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        comboBoxXorSize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedXorSize = comboBoxXorSize.getSelectedIndex();
                for (Xor.XorSize item: Xor.XorSize.values()) {
                    if(item.ordinal() == selectedXorSize){
                        xor.setXorSize(item);
                    }
                }
            }
        });

        /* init this form */
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }

    private void checkExportButtonEnable(){
        if(main.getBinaryFile() != null && xor.getXorValue() != null){
            /* enable export button */
            exportXORButton.setEnabled(true);
        }
    }
}
