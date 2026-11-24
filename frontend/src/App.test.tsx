import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';

import App from './App';
import { server } from './test/server';

async function completarPedido() {
  const usuario = userEvent.setup();

  await usuario.type(
    screen.getByLabelText(/id de cliente/i),
    '1'
  );

  await usuario.type(
    screen.getByLabelText(/descripci[oó]n/i),
    'Pedido de prueba'
  );

  await usuario.type(
    screen.getByLabelText(/total/i),
    '1500'
  );

  await usuario.click(
    screen.getByRole('button', {
      name: /crear pedido/i
    })
  );
}

describe('Formulario de pedidos', () => {
  it('confirma el pedido cuando el servidor responde 201', async () => {
    server.use(
      http.post('/api/pedidos', async ({ request }) => {
        const datos = await request.json() as {
          clienteId: number;
          descripcion: string;
          total: number;
        };

        expect(datos.clienteId).toBe(1);
        expect(datos.descripcion).toBe('Pedido de prueba');
        expect(datos.total).toBe(1500);

        return HttpResponse.json(
          { id: 110 },
          { status: 201 }
        );
      })
    );

    render(<App />);
    await completarPedido();

    expect(
      await screen.findByRole('status')
    ).toHaveTextContent(/pedido creado correctamente/i);
  });

  it('informa el rechazo del pedido sin mostrar éxito', async () => {
    server.use(
        http.post('/api/pedidos', () =>
        HttpResponse.json(
            { message: 'El cliente está inactivo' },
            { status: 400 }
        )
        )
    );

    render(<App />);
    await completarPedido();

    expect(
        await screen.findByRole('alert')
    ).toHaveTextContent(/inactivo/i);

    expect(
        screen.queryByText(/pedido creado correctamente/i)
    ).not.toBeInTheDocument();
    });

    it('informa que el servicio no está disponible', async () => {
    server.use(
        http.post('/api/pedidos', () =>
        HttpResponse.json(
            { message: 'Servicio no disponible' },
            { status: 503 }
        )
        )
    );

    render(<App />);
    await completarPedido();

    expect(
        await screen.findByRole('alert')
    ).toHaveTextContent(/no disponible/i);

    expect(
        screen.queryByText(/pedido creado correctamente/i)
    ).not.toBeInTheDocument();
    });

});

