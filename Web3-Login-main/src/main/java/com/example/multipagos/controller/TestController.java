package com.example.multipagos.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.multipagos.model.User;
import java.util.*;

@RestController
@RequestMapping("/me")
public class TestController {

    @GetMapping("/context")
    public Map<String,Object> context() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        Map<String,Object> m = new HashMap<>();
        if (a != null && a.getPrincipal() instanceof User) {
            User u = (User) a.getPrincipal();
            m.put("username", u.getUsername());
            m.put("companyIds", ((Map)a.getDetails()).get("companyIds"));
            m.put("roles", a.getAuthorities());
        } else {
            m.put("anonymous", true);
        }
        return m;
    }
}
