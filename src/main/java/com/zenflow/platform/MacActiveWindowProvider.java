package com.zenflow.platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;


public class MacActiveWindowProvider {


    public Optional<String> getActiveWindowInfo() {
        try {
            String[] cmdApp = {"osascript", "-e", "tell application \"System Events\" to get name of first process whose frontmost is true"};
            Process p1 = new ProcessBuilder(cmdApp).start();
            String processName = readAll(p1).trim();
            p1.waitFor();

            if (processName.isEmpty()) return Optional.empty();


            String script = "tell application \"System Events\"\n" +
                    "  set proc to first process whose frontmost is true\n" +
                    "  try\n" +
                    "    set wNames to name of every window of proc\n" +
                    "    if (count of wNames) > 0 then\n" +
                    "      return item 1 of wNames\n" +
                    "    else\n" +
                    "      return \"\"\n" +
                    "    end if\n" +
                    "  on error\n" +
                    "    return \"\"\n" +
                    "  end try\n" +
                    "end tell";
            String[] cmdWin = {"osascript", "-e", script};
            Process p2 = new ProcessBuilder(cmdWin).start();
            String windowTitle = readAll(p2).trim();
            p2.waitFor();

            return Optional.of(processName + "|" + windowTitle);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private String readAll(Process p) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String ln;
            while ((ln = br.readLine()) != null) {
                sb.append(ln).append("\n");
            }
            return sb.toString();
        }
    }


    public static void main(String[] args) throws Exception {
        MacActiveWindowProvider m = new MacActiveWindowProvider();
        System.out.println(m.getActiveWindowInfo().orElse("n/a"));
    }
}
