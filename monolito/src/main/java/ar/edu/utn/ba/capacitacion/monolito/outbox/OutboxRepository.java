package ar.edu.utn.ba.capacitacion.monolito.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepository {

    private final JdbcTemplate jdbc;

    public OutboxRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void guardar(
            String eventId,
            String topic,
            String clave,
            String payload) {

        jdbc.update("""
            INSERT INTO outbox_eventos
                (event_id, topic, clave, payload, estado)
            VALUES (?, ?, ?, ?, 'PENDIENTE')
            """,
            eventId, topic, clave, payload);
    }

    public List<OutboxPendiente> obtenerPendientes() {
        return jdbc.query("""
            SELECT event_id, topic, clave, payload
            FROM outbox_eventos
            WHERE estado = 'PENDIENTE'
            ORDER BY creado_en, event_id
            FETCH FIRST 10 ROWS ONLY
            """,
            (rs, rowNum) -> new OutboxPendiente(
                    rs.getString("event_id"),
                    rs.getString("topic"),
                    rs.getString("clave"),
                    rs.getString("payload")
            ));
    }

    public void marcarPublicado(String eventId) {
        jdbc.update("""
            UPDATE outbox_eventos
            SET estado = 'PUBLICADO',
                publicado_en = SYSTIMESTAMP
            WHERE event_id = ?
              AND estado = 'PENDIENTE'
            """,
            eventId);
    }
}