package com.ifpe.techifolio.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ProtectedController {

    @GetMapping("/protected")
    public String protectedPage(Model model, Authentication authentication) {
        if (authentication != null) {
            String username;
            
            // Extrair o nome do usuário com base no tipo de autenticação
            if (authentication.getPrincipal() instanceof User) {
                User userDetails = (User) authentication.getPrincipal();
                username = userDetails.getUsername();
                
                // Se tivermos detalhes adicionais, tente obter o nome real
                if (authentication.getDetails() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
                    if (details.containsKey("nome")) {
                        username = (String) details.get("nome");
                    }
                }
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                username = oauth2User.getAttribute("name");
                if (username == null) {
                    username = oauth2User.getAttribute("email");
                }
            } else {
                username = authentication.getName();
            }
            
            model.addAttribute("username", username);
        } else {
            model.addAttribute("username", "Unknown");
        }
        
        return "protected";
    }
}