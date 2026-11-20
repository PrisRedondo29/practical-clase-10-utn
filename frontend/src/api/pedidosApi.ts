import type { CrearPedidoRequest, PedidoResponse } from '../types/pedido';

const baseUrl = import.meta.env.VITE_API_URL ?? '';

type ErrorPayload = {
  message?: string;
  detail?: string;
  error?: string;
};

async function extraerMensajeError(response: Response): Promise<string | null> {
  try {
    const data = (await response.json()) as ErrorPayload;
    if (typeof data?.message === 'string' && data.message.trim() !== '') {
      return data.message.trim();
    }
    if (typeof data?.detail === 'string' && data.detail.trim() !== '') {
      return data.detail.trim();
    }
  } catch {
    // Cuerpo vacío o no JSON
  }
  return null;
}

export async function crearPedido(datos: CrearPedidoRequest): Promise<PedidoResponse> {
  let response: Response;

  try {
    response = await fetch(`${baseUrl}/api/pedidos`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(datos),
    });
  } catch {
    throw new Error('Servicio no disponible. No se pudo conectar con el servidor de pedidos.');
  }

  if (!response.ok) {
    const detalle = await extraerMensajeError(response);

    if (response.status === 503 || response.status >= 500) {
      throw new Error(
        detalle ?? 'Servicio no disponible. No se pudo verificar el cliente en este momento (HTTP 503).'
      );
    }

    if (response.status === 400) {
      throw new Error(
        detalle ?? 'No se pudo crear el pedido: el cliente no existe, está inactivo o los datos son inválidos (HTTP 400).'
      );
    }

    throw new Error(detalle ?? `No se pudo crear el pedido (HTTP ${response.status}).`);
  }

  return (await response.json()) as PedidoResponse;
}
