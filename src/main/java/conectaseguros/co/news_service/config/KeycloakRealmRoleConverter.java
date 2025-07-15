package conectaseguros.co.news_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {

        Map<String, Object> claims = jwt.getClaims();
        if (claims == null || claims.isEmpty()) {
            return Collections.emptyList();
        }

        Object realmAccessObj = claims.get(REALM_ACCESS);
        if (!(realmAccessObj instanceof Map)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;

        Object rolesObj = realmAccess.get(ROLES);
        if (!(rolesObj instanceof List)) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<Object> rolesList = (List<Object>) rolesObj;

        return rolesList.stream()
                .filter(Objects::nonNull)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(roleName -> !roleName.trim().isEmpty())
                .map(roleName -> ROLE_PREFIX + roleName.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());


    }

}
