CREATE INDEX idx_eventos_pedido_event_id
ON eventos_pedido_recibidos (event_id);

ALTER TABLE eventos_pedido_recibidos
ADD CONSTRAINT uq_eventos_pedido_event_id
UNIQUE (event_id)
USING INDEX idx_eventos_pedido_event_id
ENABLE NOVALIDATE;