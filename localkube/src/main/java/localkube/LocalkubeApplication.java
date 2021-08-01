package localkube;

import localkube.lib.LocalKubeApi;
import localkube.logs.LogController;
import localkube.logs.LogSqlHandler;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class LocalkubeApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(LocalkubeApplication.class).profiles("server").bannerMode(Banner.Mode.OFF).run(args);
		LocalKubeApi.printBanner();
		LogSqlHandler.createDatabase();
		LocalKubeApi.initialisationMessage();

	}
}