package ar.edu.utn.ba.capacitacion.monolito.pedido.repository;

import ar.edu.utn.ba.capacitacion.monolito.pedido.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<PedidoEntity, Long> {
}

