package com.example.multipagos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.multipagos.model.Company;
import com.example.multipagos.model.User;
import com.example.multipagos.model.UserCompany;
import com.example.multipagos.model.Role;
import com.example.multipagos.repository.CompanyRepository;
import com.example.multipagos.repository.UserRepository;
import com.example.multipagos.repository.UserCompanyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application {
    public static void main(String[] args){ SpringApplication.run(Application.class, args); }

 

    // Seed simple data
    @Bean
    public CommandLineRunner seed(CompanyRepository companyRepo, UserRepository userRepo, UserCompanyRepository ucRepo, PasswordEncoder encoder) {
        return args -> {
            if(companyRepo.count() == 0) {
                Company c1 = new Company();
                c1.setName("Empresa A");
                c1.setNit("1001");
                companyRepo.save(c1);

                Company c2 = new Company();
                c2.setName("Empresa B");
                c2.setNit("2002");
                companyRepo.save(c2);

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(encoder.encode("admin123"));
                userRepo.save(admin);

                UserCompany uca = new UserCompany();
                uca.setUser(admin);
                uca.setCompany(c1);
                uca.setRole(Role.ADMIN);
                ucRepo.save(uca);

                User provider = new User();
                provider.setUsername("provider");
                provider.setEmail("prov@example.com");
                provider.setPasswordHash(encoder.encode("prov123"));
                userRepo.save(provider);

                UserCompany ucp = new UserCompany();
                ucp.setUser(provider);
                ucp.setCompany(c2);
                ucp.setRole(Role.PROVIDER);
                ucRepo.save(ucp);
            }
        };
    }
}
