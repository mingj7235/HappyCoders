package com.happycoders.account;

import com.happycoders.domain.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true) //성능을 조금이나마 향상
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickName);

    @EntityGraph(value = "Account.withTags", type = EntityGraph.EntityGraphType.FETCH)
    Account findAccountWithTagsById (Long id);

    @EntityGraph(value = "Account.withZones", type = EntityGraph.EntityGraphType.FETCH)
    Account findAccountWithZonesById (Long id);

}
