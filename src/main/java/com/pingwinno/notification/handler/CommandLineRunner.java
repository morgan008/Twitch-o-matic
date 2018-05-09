package com.pingwinno.notification.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandLineRunner {


    public void executeCommand(String name, String time) {
        //command line for run streamlink
        String fileName = name +"_"+ time + ".mp4";
        String command = String.join(" ","streamlink","https://www.twitch.tv/unknowndevice", "best",  "-o", fileName);
        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println(output.toString());

    }

}
