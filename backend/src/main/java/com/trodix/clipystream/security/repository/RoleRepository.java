package com.trodix.clipystream.security.repository;

import java.util.Optional;
import com.trodix.clipystream.security.entity.ERole;
import com.trodix.clipystream.security.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(ERole name);
}
