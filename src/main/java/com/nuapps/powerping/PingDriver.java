package com.nuapps.powerping;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PingDriver {
    public static void ping(TableView<Hosts> theTable, String strParameters) throws IOException {
        if (theTable.getSelectionModel().getSelectedIndex() >= 0) {
            ObservableList<Hosts> data = theTable.getSelectionModel().getSelectedItems();
            for (Hosts hosts1 : data) {
                String strCommand;
                String hostName = hosts1.getHostName();
                strCommand = strParameters + hosts1.getIpAddress();
                String ip_address = hosts1.getIpAddress();
                int lineNumber = data.indexOf(hosts1);

                String env_temp = System.getenv("TEMP");
                FileWriter bat = new FileWriter(env_temp + "/powerping/ping" + lineNumber + ".bat");
                try (BufferedWriter bf = new BufferedWriter(bat)) {
                    bf.write("@echo off");
                    bf.newLine();
                    bf.write("@cls");
                    bf.newLine();
                    bf.write("@color 17");
                    bf.newLine();
                    bf.write("@title Ping  " + hostName + "  [" + ip_address + "]");
                    bf.newLine();
                    bf.write(strCommand);
                    bf.newLine();
                    bf.write("@pause");
                }
                String strCommand2 = env_temp + "/powerping/ping" + lineNumber + ".bat";
                Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + strCommand2);
            }
        }
    }
}
// parei aqui