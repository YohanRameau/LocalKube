package localkube.app;

import com.google.cloud.tools.jib.api.*;
import localkube.lib.LocalKubeApi;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
public class AppController {
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final AtomicInteger servicePortCounter = new AtomicInteger(15000);
    private final ArrayList<App> appList = new ArrayList<>();
    private final ArrayList<Integer> usedPorts = new ArrayList<>();

    @RequestMapping(method=RequestMethod.POST, value="/app/start", consumes = "application/json", produces = "application/json")
    private App startApp(@RequestBody App app) throws IOException, InterruptedException, ExecutionException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {
        System.out.println(LocalKubeApi.formatTime()+"Initializing the Docker Image {"+app.getName()+"}");
        App.startAppValidation(app);
        if (app.isAppExist(app.getName()) && !usedPorts.contains(app.getPort())) {
            var startedapp = new App(idCounter.addAndGet(1), app.getName(), app.getPort(), servicePortCounter.addAndGet(1), app.getName() + appList.size()+1);
            App.runApplication(startedapp, usedPorts);
            return startedapp;
        }
        System.out.println(LocalKubeApi.formatTime()+"Initialization failure {"+app.getName()+"}");
        return new App(0, "Error", 0, 0, "Please verify the name entered, or if the application is in the right folder");
    }

    @RequestMapping(value = "/app/list", method = RequestMethod.GET, produces = "application/json")
    private ArrayList<String> listApp() {
        return App.displayAppsWithTime(appList);
    }

    @RequestMapping(value = "/app/list/add", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    private ArrayList<App> addListApp(@RequestBody App app) {
        appList.add(app);
        return appList;
    }

    @RequestMapping(value = "/app/stop", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    private ArrayList<String> stopApp(@RequestBody App app){
        App.stopAppValidation(app);
        return App.stopApplication(app, appList, usedPorts);
    }

}