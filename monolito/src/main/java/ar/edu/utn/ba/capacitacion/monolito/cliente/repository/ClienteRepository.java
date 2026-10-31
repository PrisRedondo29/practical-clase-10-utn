package ar.edu.utn.ba.capacitacion.monolito.cliente.repository;

import ar.edu.utn.ba.capacitacion.monolito.cliente.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {
}

