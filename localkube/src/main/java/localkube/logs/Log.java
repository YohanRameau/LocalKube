package localkube.logs;


import localkube.app.App;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.LocalTime;

public record Log(@ColumnName("appId") long appId, @ColumnName("name") String dockerName, @ColumnName("dockerPort") int dockerPort,@ColumnName("servicePort") int servicePort,
                  @ColumnName("dockerInstance") String dockerInstance, @ColumnName("message") String message, @ColumnName("time") String time) {


    public long getAppId() {
        return appId;
    }

    public String getDockerName() {
        return dockerName;
    }

    public int getDockerPort() {
        return dockerPort;
    }

    public int getServicePort() {
        return servicePort;
    }

    public String getDockerInstance() {
        return dockerInstance;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
