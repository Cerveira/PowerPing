package com.nuapps.powerping;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PingDriver {
    public void ping(TableView<Hosts> theTable, String theEco) throws IOException {
        if (theTable.getSelectionModel().getSelectedIndex() >= 0) {
            ObservableList<Hosts> data = theTable.getSelectionModel().getSelectedItems();
            for (Hosts host1: data) {
                String hostname = host1.getHostname();
                String ip_address = host1.getIp();
                int numLinha = data.indexOf(host1);

                String env_temp = System.getenv("TEMP");
                FileWriter bat = new FileWriter(env_temp + "/powerping/ping" + numLinha + ".bat");
                try (BufferedWriter bf = new BufferedWriter(bat)) {
                    bf.write("@echo off");
                    bf.newLine();
                    bf.write("@cls");
                    bf.newLine();
                    bf.write("@color 17");
                    bf.newLine();
                    bf.write("@title Ping  " + hostname + "  [" + ip_address + "]");
                    bf.newLine();
                    bf.write(theEco + ip_address);
                    bf.newLine();
                    bf.write("@pause");
                }
                String comando = env_temp + "/powerping/ping" + numLinha + ".bat";
                Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + comando);
            }
        }
    }
}
