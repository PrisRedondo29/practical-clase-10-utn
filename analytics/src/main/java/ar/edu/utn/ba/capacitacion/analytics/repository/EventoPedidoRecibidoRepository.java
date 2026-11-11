package ar.edu.utn.ba.capacitacion.analytics.repository;

import ar.edu.utn.ba.capacitacion.analytics.entity.EventoPedidoRecibidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface EventoPedidoRecibidoRepository extends JpaRepository<EventoPedidoRecibidoEntity, Long> {

    @Query("SELECT COALESCE(SUM(e.total), 0) FROM EventoPedidoRecibidoEntity e")
    BigDecimal sumarTotal();
}
