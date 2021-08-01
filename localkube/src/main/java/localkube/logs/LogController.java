package localkube.logs;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;


@RestController
public class LogController {

    LogSqlHandler logSqlHandler = new LogSqlHandler();


    /**
     *
     * @param log
     */
    @PostMapping(path = "/log")
    public void createLog(@RequestBody Log log){
        logSqlHandler.insert(log);
    }

    /**
     *
     * @param time
     * @return
     */
    @GetMapping(path = "/log/{time}")
    public ArrayList<Log> findLog(@PathVariable("time") String time){
        return logSqlHandler.logsByTime(time);
    }


    /**
     *
     * @return
     */
    @GetMapping(path = "/log/logs")
    public ArrayList<Log> logs(){
        var l = logSqlHandler.allLogs();
        System.out.println(l);
        return l;
    }


    /**
     *
     * @param time
     * @param id
     * @return
     */
    @GetMapping(path = "/log/{time}/byId/{id}")
    public ArrayList<Log> findLogById(@PathVariable("time") String time, @PathVariable("id") int id){
        return logSqlHandler.logsById(time, id);
    }

    /**
     *
     * @param time
     * @param appName
     * @return
     */
    @GetMapping(path = "log/{time}/byApp/{appName}")
    public ArrayList<Log> findLogByName(@PathVariable("time") String time, @PathVariable("appName") String appName){

        return logSqlHandler.logsByDockerName(time, appName);
    }


    /**
     *
     * @param time
     * @param dockerInstance
     * @return
     */
    @GetMapping(path = "/log/{time}/byInstance/{dockerInstance}")
    public ArrayList<Log> findLogByInstance(@PathVariable("time") String time, @PathVariable("dockerInstance") String dockerInstance){
        return logSqlHandler.logsByInstance(time, dockerInstance);
    }
}
