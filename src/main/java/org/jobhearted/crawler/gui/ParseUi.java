package org.jobhearted.crawler.gui;

import org.jobhearted.crawler.processing.Parser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA for JobHearted.
 * User: Morlack
 * Date: 12/15/13
 * Time: 12:59 PM
 */
public class ParseUi {
    final JFileChooser fc = new JFileChooser();

    private JTextField tfFileInput;
    private JPanel parsePanel;
    private JButton btSelect;
    private JCheckBox cbParseSkills;
    private JCheckBox cbParseEducation;
    private JCheckBox cbParsePofile;
    private JButton btStart;

    private ParseUi() {
        btSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Handle open button action.
                if (e.getSource() == btSelect) {
                    int returnVal = fc.showOpenDialog(btSelect);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        tfFileInput.setText(file.getAbsolutePath());

                    }
                }
            }
        });

        btStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == btStart) {
                    Parser parser = new Parser(tfFileInput.getText(), cbParsePofile.isSelected(), cbParseSkills.isSelected(), cbParseEducation.isSelected());
                    ExecutorService damn = Executors.newSingleThreadExecutor();
                    damn.execute(parser);

                }
            }
        });
    }


    public static ParseUi createparseUi() {
        ParseUi ui = new ParseUi();
        JFrame frame = new JFrame("JobHearted LinkedIn JSON parser");
        frame.setContentPane(ui.parsePanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return ui;
    }
}
