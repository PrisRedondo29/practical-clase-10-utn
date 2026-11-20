export type CrearPedidoRequest = {
  clienteId: number;
  descripcion: string;
  total: number;
};

export type PedidoResponse = {
  id: number;
};
