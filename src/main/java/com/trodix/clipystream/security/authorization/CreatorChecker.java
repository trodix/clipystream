package com.trodix.clipystream.security.authorization;

import java.text.MessageFormat;
import java.util.UUID;
import com.trodix.clipystream.core.exception.ResourceNotFoundException;
import com.trodix.clipystream.core.interfaces.Ownable;
import com.trodix.clipystream.security.entity.User;
import com.trodix.clipystream.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CreatorChecker {

    @Autowired
    ApplicationContext appContext;

    @Autowired
    private final UserDetailsServiceImpl userDetailsService;

    public CreatorChecker(final UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public <T extends Ownable> boolean check(final Class<T> c, final UUID id) {
        final String[] beanNamesForType = appContext.getBeanNamesForType(ResolvableType.forClassWithGenerics(CrudRepository.class, c, UUID.class));
        final CrudRepository<T, UUID> repository = (CrudRepository<T, UUID>) appContext.getBean(beanNamesForType[0]);

        final T entity = repository.findById(id).orElse(null);
        if (entity != null) {
            return entity.getUser().isEnabled() && entity.getUser().equals(getAuthenticatedUser());
        }
        throw new ResourceNotFoundException(MessageFormat.format("Entity {0} not found for id {1}", c.getName(), id));
    }

    private User getAuthenticatedUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String currentPrincipalName = authentication.getName();

        return userDetailsService.loadUserEntityByUsername(currentPrincipalName);
    }

}
