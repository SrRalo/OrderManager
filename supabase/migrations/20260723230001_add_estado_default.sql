alter table public.pedidos
  alter column estado set default 'pendiente';

alter table public.pedidos
  alter column origen set default 'app_mesero';
