package ar.edu.utn.ba.capacitacion.monolito.outbox;

public record OutboxPendiente(
        String eventId,
        String topic,
        String clave,
        String payload
) {
}