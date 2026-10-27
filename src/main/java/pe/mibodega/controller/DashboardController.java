package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.DashboardService;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(defaultValue = "mes") String periodo,
            @RequestParam(required = false) Integer sede, Model model) {
        model.addAttribute("k", dashboardService.kpis(s.getIdEmpresa()));
        model.addAttribute("graficos", dashboardService.graficos(s.getIdEmpresa(), periodo, sede));
        model.addAttribute("periodo", periodo);
        model.addAttribute("sedeFiltro", sede);
        return "dashboard";
    }
}
