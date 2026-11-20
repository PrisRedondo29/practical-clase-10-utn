import { useState, type FormEvent } from 'react';
import { crearPedido } from './api/pedidosApi';
import type { PedidoResponse } from './types/pedido';

export function App() {
  const [clienteId, setClienteId] = useState<string>('');
  const [descripcion, setDescripcion] = useState<string>('');
  const [total, setTotal] = useState<string>('');

  const [cargando, setCargando] = useState<boolean>(false);
  const [pedidoCreado, setPedidoCreado] = useState<PedidoResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPedidoCreado(null);
    setError(null);

    const clienteIdNumero = Number(clienteId);
    const descripcionLimpia = descripcion.trim();
    const totalNumero = Number(total);

    if (!Number.isInteger(clienteIdNumero) || clienteIdNumero <= 0) {
      setError('El identificador de cliente es obligatorio y debe ser un número entero positivo.');
      return;
    }

    if (descripcionLimpia === '') {
      setError('La descripción del pedido es obligatoria.');
      return;
    }

    if (Number.isNaN(totalNumero) || totalNumero < 0.01) {
      setError('El total del pedido debe ser mayor a cero.');
      return;
    }

    setCargando(true);
    try {
      const respuesta = await crearPedido({
        clienteId: clienteIdNumero,
        descripcion: descripcionLimpia,
        total: totalNumero,
      });

      setPedidoCreado(respuesta);
      setClienteId('');
      setDescripcion('');
      setTotal('');
    } catch (err) {
      const mensaje =
        err instanceof Error ? err.message : 'Ocurrió un error inesperado al crear el pedido.';
      setError(mensaje);
    } finally {
      setCargando(false);
    }
  }

  return (
    <main>
      <h1>Nuevo pedido</h1>

      <form onSubmit={handleSubmit} aria-busy={cargando} noValidate>
        <div>
          <label htmlFor="clienteId">ID de cliente</label>
          <input
            id="clienteId"
            name="clienteId"
            type="number"
            min="1"
            step="1"
            value={clienteId}
            onChange={(e) => setClienteId(e.target.value)}
            disabled={cargando}
            required
          />
        </div>

        <div>
          <label htmlFor="descripcion">Descripción</label>
          <input
            id="descripcion"
            name="descripcion"
            type="text"
            value={descripcion}
            onChange={(e) => setDescripcion(e.target.value)}
            disabled={cargando}
            required
          />
        </div>

        <div>
          <label htmlFor="total">Total</label>
          <input
            id="total"
            name="total"
            type="number"
            min="0.01"
            step="0.01"
            value={total}
            onChange={(e) => setTotal(e.target.value)}
            disabled={cargando}
            required
          />
        </div>

        <button type="submit" disabled={cargando}>
          {cargando ? 'Creando pedido...' : 'Crear pedido'}
        </button>
      </form>

      {cargando && <p role="status">Enviando solicitud de pedido...</p>}

      {pedidoCreado && (
        <p role="status">Pedido creado correctamente (ID: {pedidoCreado.id}).</p>
      )}

      {error && <p role="alert">{error}</p>}
    </main>
  );
}

export default App;
