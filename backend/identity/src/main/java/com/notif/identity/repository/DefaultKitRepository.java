package com.notif.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notif.common.entity.identity.DefaultKit;

public interface DefaultKitRepository extends JpaRepository<DefaultKit, Short> {}
