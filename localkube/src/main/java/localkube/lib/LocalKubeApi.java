package localkube.lib;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
public class LocalKubeApi {

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(LocalKubeApi.class).profiles("client").web(WebApplicationType.NONE).bannerMode(Banner.Mode.OFF).run(args);
        if (args.length != 5) {
            throw new IllegalStateException("You must provide 5 arguments to initialize information's application");
        }
        InitialisationMessage(args[1]);
        var logClient = new LogClient(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4]);
        logClient.sendInitLog(0);
        try (var logParser = new LogParser(args[1])) {
            LocalKubeApiTreatment(logClient, logParser);
        } catch (IOException e) {
            ErrorDuringApplicationProcess(logClient);
        }
        System.exit(0);
    }

    private static void ErrorDuringApplicationProcess(LogClient logClient){
        logClient.sendLog(LogClient.LogType.ERROR.write("[Error during the application process, please verify your app."));
        System.err.println("Error during the application process, please verify your app." );
        System.exit(-1);
    }

    private static void LocalKubeApiTreatment(LogClient logClient, LogParser logParser) throws IOException {
        logClient.sendAddAppList();
        outPutTreatment(logClient, logParser);
        errTreatment(logClient, logParser);
        StopLocalKubeApi(logParser, logClient);
    }

    static void InitialisationMessage(String appName){
        System.out.println(appName +"  app start" );
    }

    public static void printBanner(){
        System.out.println("\n" +
                "  _                    _ _  __     _          \n" +
                " | |    ___   ___ __ _| | |/ /   _| |__   ___ \n" +
                " | |   / _ \\ / __/ _` | | ' / | | | '_ \\ / _ \\\n" +
                " | |__| (_) | (_| (_| | | . \\ |_| | |_) |  __/\n" +
                " |_____\\___/ \\___\\__,_|_|_|\\_\\__,_|_.__/ \\___|\n" +
                "                                              \n");
    }

    public static String formatTime(){
        return "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) +"]  ";
    }

    public static void initialisationMessage(){
        System.out.println(formatTime()+"Please go to http://localhost:8080/app/start to start your app");
    }

    static void StopLocalKubeApi(LogParser logParser, LogClient logClient){
        var exitCode = logParser.exitCode();
        logClient.sendStopLog(exitCode,0);
        logClient.sendStopApplication();
    }

    static void outPutTreatment(LogClient logClient, LogParser logParser) throws IOException {
        String out;
        while ((out = logParser.nextStandardOutput()) != null) {
            System.out.println(out);
            logClient.sendLog(LogClient.LogType.STANDARDOUTPUT.write(out));
        }
    }

    static void errTreatment(LogClient logClient, LogParser logParser) throws IOException {
        String out;
        while ((out = logParser.nextStandardErrOutput()) != null) {
            System.err.println(out);
            logClient.sendLog(LogClient.LogType.ERROR.write(out));
        }
    }
}
