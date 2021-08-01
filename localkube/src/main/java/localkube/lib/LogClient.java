package localkube.lib;


import localkube.app.App;
import localkube.logs.Log;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.function.Function;

public class LogClient {
    private final WebClient webClient;
    private final App app;


    enum LogType {
        ERROR((String message) -> {
            return "[ERROR]  " + message;
        }),
        STATUS((message) -> {
            return "[STATUS]  " + message;
        }),
        STANDARDOUTPUT((message) -> {
            return "[STANDARD OUTPUT]  " + message;
        });
        private final Function<String,String> action;
        LogType(Function<String, String> action) { this.action = action; }
        public String write(String message) { return action.apply(message); }
    }


    LogClient(int id, String name, int port, int servicePort, String dockerInstance){
        webClient = WebClient.builder()
                             .baseUrl("http://localhost:8080/")
                             .build();
        app = new App(id, name, port, servicePort, dockerInstance);
    }

    void sendLog(String message){
        var log = new Log(app.getId(), app.getName(), app.getPort(), app.getServicePort(), app.getDockerInstance(), LogType.STANDARDOUTPUT.write(message), LocalTime.now().toString());
        webClient.post().uri("/log").body(Mono.just(log), Log.class)
                        .retrieve().bodyToMono(Log.class).block();
    }



   void sendInitLog(long time){

        sendLog(LogType.STATUS.write("Initialisation of " + app.getName()));
    }

    void sendStopLog(int exitCode,long time){
        sendLog(LogType.STATUS.write("End of " + app.getName() + " Exit with code : " + exitCode));
    }

    void sendAddAppList(){
        webClient.post().uri("/app/list/add")
                .body(Mono.just(app), App.class)
                .retrieve().bodyToMono(String.class).block();
        sendLog(LogType.STATUS.write(app.getName() + " correctly added on localKube"));
    }

    void sendStopApplication(){
        System.out.println(app.getName() + "  app shutdown.");
        webClient.post().uri("/app/stop")
                        .body(Mono.just(app), App.class)
                .retrieve().bodyToMono(String.class).block();

    }


}
