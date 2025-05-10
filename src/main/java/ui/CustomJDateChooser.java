package ui;

import javax.swing.*;
import java.util.Date;

public class CustomJDateChooser extends JPanel {
    
    private com.toedter.calendar.JDateChooser dateChooser;
    
    public CustomJDateChooser() {
        dateChooser = new com.toedter.calendar.JDateChooser();
        setLayout(new java.awt.BorderLayout());
        add(dateChooser, java.awt.BorderLayout.CENTER);
    }
    
    public Date getDate() {
        return dateChooser.getDate();
    }
    
    public void setDate(Date date) {
        dateChooser.setDate(date);
    }
} 