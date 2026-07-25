insert into public.categorias_menu (nombre, orden)
values
  ('Hamburguesas', 1),
  ('Pizzas', 2),
  ('Bebidas', 3),
  ('Postres', 4)
on conflict (nombre) do nothing;

insert into public.items_menu (nombre, descripcion, precio, categoria_id, imagen_ref, disponible, activo)
select * from (
  values
    ('Hamburguesa Clásica', 'Carne angus con queso', 12.50, 1, 'Hamburguesa Clásica', true, true),
    ('Papas Fritas Grandes', 'Papas crujientes', 4.50, 1, 'Papas Fritas Grandes', true, true),
    ('Pizza Pepperoni', 'Pizza familiar de pepperoni', 15.00, 2, 'Pizza Pepperoni', true, true),
    ('Tacos al Pastor', 'Tacos con piña y cilantro', 9.00, 1, 'Tacos al Pastor', true, true),
    ('Ensalada César', 'Lechuga, crutones y parmesano', 8.50, 1, 'Ensalada César', true, true),
    ('Refresco de Cola', 'Lata 355ml', 2.50, 3, 'Refresco de Cola', true, true),
    ('Agua Natural', 'Botella 500ml', 1.50, 3, 'Agua Natural', true, true),
    ('Flan Napolitano', 'Postre de caramelo', 5.00, 4, 'Flan Napolitano', true, true),
    ('Quesadillas', 'Tortilla con queso derretido', 6.00, 1, 'Quesadillas', true, true),
    ('Burrito Supreme', 'Burrito de carne y frijoles', 10.00, 1, 'Burrito Supreme', true, true)
) as v(nombre, descripcion, precio, categoria_id, imagen_ref, disponible, activo)
where not exists (
  select 1 from public.items_menu m where m.nombre = v.nombre
);
