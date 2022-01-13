package com.trodix.clipystream.security.service;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import com.trodix.clipystream.security.entity.ERole;
import com.trodix.clipystream.security.entity.Role;
import com.trodix.clipystream.security.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initDefaultRoles() {
        final Set<Role> roleSet = new HashSet<>();

        final Role adminRole = new Role(ERole.ROLE_ADMIN);
        final Role moderatorRole = new Role(ERole.ROLE_MODERATOR);
        final Role userRole = new Role(ERole.ROLE_USER);

        roleSet.add(adminRole);
        roleSet.add(moderatorRole);
        roleSet.add(userRole);

        for (final Role role : roleSet) {
            if (!this.roleRepository.findByName(role.getName()).isPresent()) {
                this.roleRepository.save(role);
            }
        }
    }

}
