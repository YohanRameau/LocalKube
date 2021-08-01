package localkube.lib;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

class LogParser implements Closeable {
    private final Process process;
    private final BufferedReader outputBr;
    private final BufferedReader errBr;

    LogParser(String appName) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("bash", "-c", "java --enable-preview -jar " + appName + ".jar").inheritIO()
                                                                                                                                  .redirectOutput(ProcessBuilder.Redirect.PIPE)
                                                                                                                                  .redirectError(ProcessBuilder.Redirect.PIPE);
        process = builder.start();
        outputBr = new BufferedReader(new InputStreamReader(process.getInputStream()));
        errBr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }

    String nextStandardOutput() throws IOException {
        return outputBr.readLine() ;
    }

    String nextStandardErrOutput() throws IOException {
        return errBr.readLine() ;
    }

    int exitCode() {
        return process.exitValue();
    }


    @Override
    public void close() throws IOException {
        outputBr.close();
        errBr.close();
    }
}
