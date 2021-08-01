package localkube.app;

import com.google.cloud.tools.jib.api.*;
import com.google.cloud.tools.jib.api.buildplan.AbsoluteUnixPath;
import localkube.lib.LocalKubeApi;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class App{
    private final int id;
    private final String name;
    private final int port;
    private final int servicePort;
    private final String dockerInstance;
    private long elapsedTime;

    public App(int id, String name, int port, int servicePort, String dockerInstance) {
        this.id = id;
        this.name = name;
        this.port = port;
        this.servicePort = servicePort;
        this.dockerInstance = dockerInstance;
        this.elapsedTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "{" + "id: " + id + ""
                + ", name: " + name + ""
                + ", port: " + port + ""
                + ", servicePort: " + servicePort + ""
                + ", dockerInstance: " + dockerInstance + ""
                + ", elapsedTime: " + TimeUnit.MILLISECONDS.toMinutes(elapsedTime - this.elapsedTime) + "min"
                + "}";
    }

    public int getId() {
        return id;
    }

    public String getName() { return name; }

    public int getPort() { return port; }

    public int getServicePort() { return servicePort; }

    public String getDockerInstance() { return dockerInstance; }

    static void displayErrorCode(int exitCode){
        if (exitCode != 0 && exitCode != 143)
            System.err.println("\n" + LocalKubeApi.formatTime() +"Exited with error code : " + exitCode);
    }

    static void displayCommandExecution(Process process){
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                System.out.println(LocalKubeApi.formatTime()+line);
            }
        } catch (IOException e) {
            System.err.println(LocalKubeApi.formatTime()+"File not exist or invalid name");
        }
    }


    boolean isAppExist(String name) {
        try {
            FileReader r = new FileReader("apps/" + name + ".jar");
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    static App removeAppInList(ArrayList<App> appList, int id){
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).getId() == id){
                return appList.remove(i);
            }
        }
        return null;
    }

    static void loadImageIntoDocker(String appName){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "docker load < docker-images/"+appName+"-image:latest.tar");
        try {
            Process process = processBuilder.start();
            displayCommandExecution(process);
            int exitCode = process.waitFor();
            displayErrorCode(exitCode);
        } catch (IOException e) {
            System.err.println("File not exist or invalid name");
            return;
        } catch (InterruptedException e) {
            return;
        }
    }

    static void buildLocalImage(App app) throws InvalidImageReferenceException, IOException, InterruptedException, ExecutionException, RegistryException, CacheDirectoryCreationException {
        Jib.from("openjdk:15").addLayer(Arrays.asList(Paths.get("apps/" + app.getName() + ".jar")), AbsoluteUnixPath.get("/")).addLayer(Arrays.asList(Paths.get("lib/local-kube-api.jar")), AbsoluteUnixPath.get("/")).setEntrypoint("java", "--enable-preview","-jar","local-kube-api.jar",String.valueOf(app.getId()), app.getName(), String.valueOf(app.getPort()), String.valueOf(app.getServicePort()), app.getDockerInstance()).containerize(Containerizer.to(TarImage.at(Paths.get("docker-images/" + app.getName() + "-image:latest.tar")).named(app.getName() + "-image:latest")).setOfflineMode(true));
        loadImageIntoDocker(app.getName());

    }

    static void runDockerContainer(App app, ArrayList<Integer> usedPorts){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "docker run --rm --network host --name AppContainer"+app.getId()+" -p "+app.getPort()+":"+app.getServicePort()+" "+app.getName()+"-image:latest");
        try {
            Process process = processBuilder.start();
            displayCommandExecution(process);
            int exitCode = process.waitFor();
            displayErrorCode(exitCode);
        } catch (IOException e) {
            System.err.println(LocalKubeApi.formatTime()+"File not exist or invalid name");
            return;
        } catch (InterruptedException e) {
            System.err.println(LocalKubeApi.formatTime()+"The application encountered a problem while launching");
        }
        usedPorts.add(app.getPort());
    }

    static void stopDockerContainer(App app){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "docker stop "+"AppContainer"+app.getId());
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            displayErrorCode(exitCode);
        } catch (IOException e) {
            System.err.println(LocalKubeApi.formatTime()+"File not exist or invalid name");
            return;
        } catch (InterruptedException e) {
            return;
        }
    }


    static ArrayList<String> displayAppsWithTime(ArrayList<App> appList){
        ArrayList<String> result = new ArrayList<>();
        for (App app : appList) {
            result.add(app.toString());
        }
        return result;
    }


    static void runApplication(App app, ArrayList<Integer> usedPorts) throws IOException, InterruptedException, ExecutionException, RegistryException, CacheDirectoryCreationException, InvalidImageReferenceException {

        buildLocalImage(app);
        loadImageIntoDocker(app.getName());
        runDockerContainer(app, usedPorts);
    }

    static ArrayList<String> stopApplication(App app, ArrayList<App> appList, ArrayList<Integer> usedPort){
        var stopedApp = App.removeAppInList(appList,app.getId());
        var displayResult = new ArrayList<String>();
        if(stopedApp == null){
            displayResult.add(LocalKubeApi.formatTime()+"Error : Please verify the id entered, or if the application is still alive by visiting /app/list}");
            System.out.println(displayResult);
            return displayResult;
        }
        else{
            App.stopDockerContainer(stopedApp);
            var index = usedPort.indexOf(app.getPort());
            usedPort.remove(index);
            displayResult.add(LocalKubeApi.formatTime()+stopedApp.toString());
            System.out.println(displayResult);
            return displayResult;
        }
    }

    static void startAppValidation(App app){
        Class str = String.class;
        Class it = Integer.class;
        if(!str.isInstance(app.getName()) || !it.isInstance(app.getPort()))
            throw new IllegalArgumentException();
        else if (app.getPort() > 65535 || app.getPort() < 0)
            throw new IllegalArgumentException();
    }

    static void stopAppValidation(App app){
        Class it = Integer.class;
        if(!it.isInstance(app.getId()))
            throw new IllegalArgumentException("Invalid Id.");
    }

}
