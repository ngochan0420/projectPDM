package com.pdm.loan;

import com.pdm.loan.ui.LoginFrame;
import com.pdm.loan.ui.kit.Theme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Theme.install();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
