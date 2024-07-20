package dev.nncode.gooutbackend.tour.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    private final RoleService roleService;

    public TestController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping("/role")
    public Iterable<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
   
}
