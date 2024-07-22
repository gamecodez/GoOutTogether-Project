package dev.nncode.gooutbackend.user;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.nncode.gooutbackend.common.enumeration.RoleEnum;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Test
    void shouldReturnRoles() {
        var mockRole = List.of(
                new Role(RoleEnum.CONSUMER.getId(), RoleEnum.CONSUMER.name()),
                new Role(RoleEnum.ADMIN.getId(), RoleEnum.ADMIN.name()),
                new Role(RoleEnum.COMPANY.getId(), RoleEnum.COMPANY.name()));
        when(roleRepository.findAll())
                .thenReturn(mockRole);

        var actual = roleService.getAllRoles();
        List<Role> result = new ArrayList<>();
        actual.iterator().forEachRemaining(result::add);

        assertEquals(3, result.size());


    }
}
