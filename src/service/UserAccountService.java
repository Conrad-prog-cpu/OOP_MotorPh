package service;

import java.util.List;

public interface UserAccountService {

    List<UserAccountDto> findAll();

    UserAccountDto findByUsername(String username);

    boolean add(UserAccountCreateRequest request);

    boolean update(UserAccountUpdateRequest request);

    boolean deleteByUsername(String username);
}