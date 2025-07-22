package br.com.ecommerce.meninadourada;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MdecommerceApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.configure()
				.directory("src/main/resources")
				.load();

		// Exporta as variáveis para o sistema
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});



		SpringApplication.run(MdecommerceApplication.class, args);
	}

}