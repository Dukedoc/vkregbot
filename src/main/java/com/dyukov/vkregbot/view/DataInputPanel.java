package com.dyukov.vkregbot.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyukov.vkregbot.config.AppProperties;
import com.dyukov.vkregbot.exceptions.ServiceException;
import com.dyukov.vkregbot.util.DateTimeUtil;
import com.dyukov.vkregbot.view.elements.ChangeColorLabel;
import com.dyukov.vkregbot.view.elements.TextInputField;
import com.dyukov.vkregbot.vk.RegistrationMethod;

public class DataInputPanel extends JPanel implements DataRetrievable {

    private ChangeColorLabel teamNameLabel;
    private ChangeColorLabel groupIdLabel;
    private ChangeColorLabel profileLinkLabel;
    private ChangeColorLabel runTimeLabel;
    private TextInputField teamNameInput;
    private TextInputField profileLinkInput;
    private JComboBox teamMembersCountBox;
    private JComboBox<String> registrationMethodBox;
    private TextInputField groupIdInput;
    private TextInputField runTimeInput;
    private JComboBox<Integer> dayNumberInput;
    private AppWindow parentWindow;

    private AppProperties properties;
    private Logger logger = LoggerFactory.getLogger(DataInputPanel.class);

    DataInputPanel(AppWindow parentWindow) throws ServiceException {
        this.parentWindow = parentWindow;
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        setBorder(new EmptyBorder(0, 5, 0, 0));

        add(createLabel(getProperty("swing.reg.method.label")));
        add(createRegistrationMethodBox());
        groupIdLabel = createLabel(getProperty("swing.group.id.label"));
        add(groupIdLabel);
        add(createGroupIdInput(groupIdLabel));
        runTimeLabel = createLabel(getProperty("swing.run.time.label"));
        add(runTimeLabel);
        add(createRunTimeInput(runTimeLabel));
        teamNameLabel = createLabel(getProperty("swing.team.name.label"));
        add(teamNameLabel);
        add(createTeamNameInput(teamNameLabel));
        profileLinkLabel = createLabel(getProperty("swing.profile.link.label"));
        add(profileLinkLabel);
        add(createProfileLinkInput(profileLinkLabel));
        add(createLabel(getProperty("swing.persons.count.label")));
        add(createTeamMembersCountList());
        add(createLabel(getProperty("swing.day.number.label")));
        add(createDayNumberInput());

        SpringUtilities.makeCompactGrid(this,
                7, 2,
                5, 5,
                10, 5);
    }

    public String getTeamName() {
        return teamNameInput.getText();
    }

    @Override
    public String getVkProfileLink() {
        return profileLinkInput.getText();
    }

    @Override
    public int getTeamMembersCount() {
        return (int) teamMembersCountBox.getSelectedItem();
    }

    public int getDayNumber() {
        return (Integer) dayNumberInput.getSelectedItem();
    }

    @Override
    public void showErrors(Map<String, String> errors) throws ServiceException {
        if (errors.isEmpty())
            return;
        StringBuilder errorString = new StringBuilder();
        for(String key : errors.keySet()) {
            switch (key) {
                case "teamName":
                    highligthErrorLabel(teamNameLabel);
                    break;
                case "groupId":
                    highligthErrorLabel(groupIdLabel);
                    break;
                case "vkProfile":
                    highligthErrorLabel(profileLinkLabel);
                    break;
                case "runTime":
                    highligthErrorLabel(runTimeLabel);
                    break;
                default:
                    // Do nothing
            }
            if (errorString.length() > 0)
                errorString.append("\n");
            errorString.append(errors.get(key));
        }
        JOptionPane.showMessageDialog(parentWindow, errorString.toString(), getProperty("err.dialog.title"), JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void modeChanged() throws ServiceException {
        String regModeTitle = (String) registrationMethodBox.getSelectedItem();
        logger.info(String.format("Registration method changed to %s", regModeTitle));
        switch (RegistrationMethod.getRegistrationMethod(Integer.valueOf(groupIdInput.getText()), regModeTitle)) {
            case MZGB:
                for (Component component : getComponents()) {
                    if (component instanceof JTextField
                          || component instanceof JComboBox) {
                        component.setEnabled(true);
                    }
                }
                break;
            case KRUGOZOR:
                teamNameInput.setEnabled(true);
                teamMembersCountBox.setEnabled(true);
                groupIdInput.setEnabled(true);
                runTimeInput.setEnabled(true);
                registrationMethodBox.setEnabled(true);
                profileLinkInput.setEnabled(false);
                dayNumberInput.setEnabled(false);
                break;
            default:
                throw new ServiceException("Unknown registration method " + regModeTitle);
        }
    }

    public RegistrationMethod getRegistrationMethod() throws ServiceException {
        String title = (String) registrationMethodBox.getSelectedItem();
        return RegistrationMethod.getRegistrationMethod(Integer.valueOf(groupIdInput.getText()), title);
    }

    @Override
    public String getGroupId() {
        return groupIdInput.getText();
    }

    @Override
    public String getScanWallTime() {
        return runTimeInput.getText();
    }

    void setDisabled() {
        for (Component component : getComponents()) {
            if (component instanceof JTextField
                  || component instanceof JComboBox) {
                component.setEnabled(false);
            }
        }
    }

    void makeEnable() {
        for (Component component : getComponents()) {
            if (component instanceof JTextField
                  || component instanceof JComboBox) {
                component.setEnabled(true);
            }
        }
    }

    private void highligthErrorLabel(ChangeColorLabel label) {
        if (label.getPrevColor() == null) {
            label.setPrevColor(label.getForeground());
        }
        label.setForeground(Color.RED);
    }

    private String getProperty(String key) throws ServiceException {
        return getProperties().getProperty(key);
    }

    private AppProperties getProperties() throws ServiceException {
        if (properties == null)
            properties = AppProperties.getInstance();
        return properties;
    }

    private JTextField createTeamNameInput(ChangeColorLabel label) {
        teamNameInput = new TextInputField(label);
        return teamNameInput;
    }

    private TextInputField createProfileLinkInput(ChangeColorLabel label) {
        profileLinkInput = new TextInputField(label);
        return profileLinkInput;
    }

    private ChangeColorLabel createLabel(String text) {
        return new ChangeColorLabel(text);
    }

    private JComboBox<Integer> createTeamMembersCountList() {
        Integer[] teamMembersCountValues = {4, 5, 6, 7, 8, 9, 10};
        teamMembersCountBox = new JComboBox(teamMembersCountValues);
        return teamMembersCountBox;
    }

    private JComboBox<String> createRegistrationMethodBox() throws ServiceException {
        List<String> titles = RegistrationMethod.getRegistrationMethodTitles();
        registrationMethodBox = new JComboBox(titles.toArray());
        registrationMethodBox.setSize(500, registrationMethodBox.getHeight());
        DataInputPanel that = this;
        registrationMethodBox.addActionListener((ActionEvent e) -> {
                try {
                    that.modeChanged();
                }
                catch (ServiceException ex) {
                    logger.error(ex.getLocalizedMessage());
                }
            }
        );
        return registrationMethodBox;
    }

    private TextInputField createGroupIdInput(ChangeColorLabel label) {
        groupIdInput = new TextInputField(label, TextInputField.INT_TYPE);
        return groupIdInput;
    }

    private TextInputField createRunTimeInput(ChangeColorLabel label) throws ServiceException {
        runTimeInput = new TextInputField(
              DateTimeUtil.formatDate(new Date()),
              label,
              TextInputField.DATE_TYPE
        );
        return runTimeInput;
    }

    private JComboBox<Integer> createDayNumberInput() {
        Integer[] days = {1, 2, 3};
        dayNumberInput = new JComboBox(days);
        return dayNumberInput;
    }

}
