package com.trodix.clipystream.security.mapping;

import com.trodix.clipystream.security.entity.User;
import com.trodix.clipystream.security.model.request.UserPublicRequest;
import com.trodix.clipystream.security.model.response.UserPublicResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    // out
    UserPublicResponse entityToModel(User user);
    Iterable<UserPublicResponse> entityListToModel(Iterable<User> users);

    // in
    User modelToEntity(UserPublicRequest userRequest);

}
