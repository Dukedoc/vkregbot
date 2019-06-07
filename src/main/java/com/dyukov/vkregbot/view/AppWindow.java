package com.dyukov.vkregbot.view;

import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.view.output.OutputPanel;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;

public class AppWindow extends JFrame {

    private OutputPanel outputPanel;
    private StartButtonPanel startButtonPanel;

    public AppWindow() throws ServiceException {
        setUIDesign();
        setSize(480, 780);
        setTitle("VkRegBot");
        initPanels();
        addWindowClosingListener();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setUIDesign() throws ServiceException {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch (Exception e) {
            throw new ServiceException("Failed to set UI Manager.", e);
        }
    }

    private void initPanels() throws ServiceException {
        DataInputPanel dataInputPanel = new DataInputPanel(this);
        getContentPane().setLayout(new BorderLayout());
        startButtonPanel = new StartButtonPanel(dataInputPanel);
        getContentPane().add(startButtonPanel, BorderLayout.SOUTH);
        getContentPane().add(dataInputPanel, BorderLayout.NORTH);
        outputPanel = new OutputPanel();
        getContentPane().add(outputPanel, BorderLayout.CENTER);
    }

    private void addWindowClosingListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Timer timer = startButtonPanel.getTimer();
                if (timer != null)
                    timer.cancel();
                System.exit(0);
            }
        });
    }

    public JTextPane getLogPane() {
        return outputPanel.getLogPane();
    }
    
}
