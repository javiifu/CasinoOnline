use casino;
go

CREATE TABLE dbo.tipo_juego (
  tipo_juego_id tinyint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE,   -- SLOT, ROULETTE, BLACKJACK
  name nvarchar(64) NOT NULL
);
CREATE TABLE dbo.tipo_movimientos_contables (
  id_tipo_movimiento smallint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE,   -- Depositar, Mantener, apostar, ganar, Sotar, Sacar, Tasa, Devolución
  direccion tinyint NOT NULL,          -- 1=credito	, 2=Debito, 3=neutral
  affects_balance bit NOT NULL
);

CREATE TABLE dbo.estado_pagos (
  id_estado_pago tinyint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE    -- Pendiente, autorizado, capturado, fallado, Devuelto, reembolso
);


CREATE TABLE dbo.kyc_estado (
  id_status_kyc tinyint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE    -- pendiente, verificado, rechazado
);


--Autenticación de usuarios. 
CREATE TABLE dbo.usuarios (
  id_usuario uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  email nvarchar(255) NOT NULL UNIQUE,
  telefono nvarchar(32) NULL,
  activo bit NOT NULL DEFAULT 1,
  baja bit NOT NULL DEFAULT 0,
  creado datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  modificado datetime2 NOT NULL DEFAULT SYSUTCDATETIME()
);


CREATE TABLE dbo.autenticacion_usuario (
  user_id uniqueidentifier NOT NULL PRIMARY KEY,
  password_hash varbinary(256) NOT NULL,
  password_algo nvarchar(32) NOT NULL,
  last_login_at datetime2 NULL,
  failed_login_count int NOT NULL DEFAULT 0,
  locked_until datetime2 NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);
CREATE TABLE dbo.sesiones_usuarios (
  session_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  created_at datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  expires_at datetime2 NOT NULL,
  ip nvarchar(64) NULL,
  user_agent nvarchar(512) NULL,
  device_id nvarchar(128) NULL,
  revoked_at datetime2 NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);
CREATE INDEX IX_user_sessions_user_created ON dbo.sesiones_usuarios(user_id, created_at DESC);

--perfil con KYC y consentimientos. 

CREATE TABLE dbo.user_profile (
  user_id uniqueidentifier NOT NULL PRIMARY KEY,
  nombre nvarchar(80) NULL,
  apellidos nvarchar(120) NULL,
  fecha_nacimiento date NULL,
  nif nvarchar(32) NULL,       -- DNI/NIE
  codigo_pais char(2) NOT NULL DEFAULT 'ES',
  provincia nvarchar(64) NULL,
  ciudad nvarchar(64) NULL,
  direccion nvarchar(255) NULL,
  codigo_postal nvarchar(16) NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);
CREATE INDEX IX_user_profile_national_id ON dbo.user_profile(nif);

CREATE TABLE dbo.kyc_usuario (
  user_id uniqueidentifier NOT NULL PRIMARY KEY,
  kyc_status_id tinyint NOT NULL,      -- FK to kyc_statuses
  proveedor nvarchar(64) NULL,
  codigo_proveedor nvarchar(128) NULL,
  fecha_entregado datetime2 NULL,
  fecha_verificacion datetime2 NULL,
  fecha_rechazo datetime2 NULL,
  razon_rechazo nvarchar(255) NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (kyc_status_id) REFERENCES dbo.kyc_estado(id_status_kyc)
);

CREATE TABLE dbo.consentimientos_usuarios (
  user_id uniqueidentifier NOT NULL,
  codigo_consentimiento nvarchar(64) NOT NULL,  -- TERMS, PRIVACY, MARKETING
  confirmados bit NOT NULL,
  fecha_modificacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  PRIMARY KEY (user_id, codigo_consentimiento),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);


CREATE TABLE dbo.limites_responsabilidad (
  user_id uniqueidentifier NOT NULL PRIMARY KEY,
  deposito_limite_diario_cent bigint NULL,
  perdidas_limite_diario_cent bigint NULL,
  limite_tiempo_sesion_minutos int NULL,
  fecha_modificacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);
--Me la ha recomendado chatGPT
CREATE TABLE dbo.self_exclusions (
  exclusion_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  starts_at datetime2 NOT NULL,
  ends_at datetime2 NULL,              -- NULL = indefinida
  reason nvarchar(255) NULL,
  created_at datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);
CREATE INDEX IX_self_exclusions_user_active ON dbo.self_exclusions(user_id, starts_at DESC);

--Carteras (wallet) y contabilidad. 
CREATE TABLE dbo.carteras (
  wallet_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL UNIQUE,
  codigo_moneda char(3) NOT NULL DEFAULT 'EUR',
  balance_cent bigint NOT NULL DEFAULT 0,   -- cache (opcional, pero útil)
  holds_cent bigint NOT NULL DEFAULT 0,    -- fondos reservados (holds)
  fecha_creacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  fecha_modificacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id)
);


CREATE TABLE dbo.contabilidad_cartera (
  id_contabilidad uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  wallet_id uniqueidentifier NOT NULL,
  id_tipo_movimiento smallint NOT NULL,
  monto_cent bigint NOT NULL,              -- siempre positivo; dirección la marca ledger_type
  tipo_referencia nvarchar(32) NOT NULL,      -- PAYMENT, BET, ROUND, PAYOUT, ADJUSTMENT
  id_referencia uniqueidentifier NOT NULL,    -- apunta a payments/bets/game_rounds/payouts...
  fecha_creacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  description nvarchar(255) NULL,
  FOREIGN KEY (wallet_id) REFERENCES dbo.carteras(wallet_id),
  FOREIGN KEY (id_tipo_movimiento) REFERENCES dbo.tipo_movimientos_contables(id_tipo_movimiento)
);
CREATE INDEX IX_wallet_ledger_wallet_created ON dbo.contabilidad_cartera(wallet_id, fecha_creacion DESC);
CREATE INDEX IX_wallet_ledger_reference ON dbo.contabilidad_cartera(tipo_referencia, id_referencia);        

-- Evitar duplicados por idempotencia en movimientos críticos:
-- (por ejemplo, un SETTLE repetido por retry)

CREATE TABLE dbo.juegos (
  id_juego uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  tipo_juego_id tinyint NOT NULL,
  codigo nvarchar(64) NOT NULL UNIQUE,     -- OBSIDIAN_VAULT, ROULETTE_EU, BJ_CLASSIC
  name nvarchar(128) NOT NULL,
  activo bit NOT NULL DEFAULT 1,
  config_json nvarchar(max) NULL,
  fecha_creacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (tipo_juego_id) REFERENCES dbo.tipo_juego(tipo_juego_id)
);

CREATE TABLE dbo.sesiones_juego (
  sesiones_juego_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  juego_id uniqueidentifier NOT NULL,
  empezada_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  acabada_en datetime2 NULL,
  ip nvarchar(64) NULL,
  dispositivo_id nvarchar(128) NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (juego_id) REFERENCES dbo.juegos(id_juego)
);
CREATE INDEX IX_sesion_juego_usuario_empezeada ON dbo.sesiones_juego(user_id, empezada_en DESC);

CREATE TABLE dbo.rondas_juego (
  ronda_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  juego_sesion_id uniqueidentifier NOT NULL,
  ronda_seq int NOT NULL,               -- 1..n dentro de la sesión
  empezada_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  acabada_en datetime2 NULL,
  status nvarchar(32) NOT NULL,         -- OPEN, SETTLED, CANCELED
  FOREIGN KEY (juego_sesion_id) REFERENCES dbo.sesiones_juego(sesiones_juego_id)
);
CREATE UNIQUE INDEX UX_sesion_rondas_juego_seq ON dbo.rondas_juego(juego_sesion_id, ronda_seq);

CREATE TABLE dbo.apuestas (
  apuesta_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  wallet_id uniqueidentifier NOT NULL,
  ronda_id uniqueidentifier NOT NULL,
  apuesta_total_cent bigint NOT NULL,
  paga_total_cent bigint NOT NULL DEFAULT 0,
  status nvarchar(32) NOT NULL,         -- PLACED, SETTLED, CANCELED
  colocada_en datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  establecida_en datetime2 NULL,
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (wallet_id) REFERENCES dbo.carteras(wallet_id),
  FOREIGN KEY (ronda_id) REFERENCES dbo.rondas_juego(ronda_id)
);

CREATE INDEX IX_apuestas_usuario ON dbo.apuestas(user_id, colocada_en DESC);
CREATE INDEX IX_apuestas_ronda ON dbo.apuestas(ronda_id);

CREATE TABLE dbo.items_apuesta (
  id_item_apuesta uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  apuesta_id uniqueidentifier NOT NULL,
  tipo_item nvarchar(32) NOT NULL,        -- POSICION_RULETA, SIDE_BLACKJACK, LINEA_SLOT (si aplica)
  payload_json nvarchar(max) NULL,
  stake_cent bigint NOT NULL,
  payout_cent bigint NOT NULL DEFAULT 0,
  FOREIGN KEY (apuesta_id) REFERENCES dbo.apuestas(apuesta_id)
);
CREATE INDEX IX_items_apuesta_apuesta ON dbo.items_apuesta(apuesta_id);


--Tablas para la tragaperras

CREATE TABLE dbo.spins_slot (
  ronda_id uniqueidentifier NOT NULL PRIMARY KEY,   -- 1 spin = 1 ronda
  apuesta_id uniqueidentifier NOT NULL UNIQUE,
  grid_rodillos_json nvarchar(max) NULL,
  lineas_json nvarchar(max) NULL,
  semilla_rng nvarchar(128) NULL,
  nonce_rng nvarchar(128) NULL,
  es_bonus bit NOT NULL DEFAULT 0,
  multiplicador int NOT NULL DEFAULT 1,
  FOREIGN KEY (ronda_id) REFERENCES dbo.rondas_juego(ronda_id),
  FOREIGN KEY (apuesta_id) REFERENCES dbo.apuestas(apuesta_id)
);
--Tablas para la ruleta. 
CREATE TABLE dbo.rondas_ruleta (
  ronda_id uniqueidentifier NOT NULL PRIMARY KEY,
  numero_ganador tinyint NULL,
  color_ganador nvarchar(8) NULL,        -- ROJO/NEGRO/VERDE (o RED/BLACK/GREEN si prefieres)
  docena tinyint NULL,                   -- 1/2/3
  columna tinyint NULL,                  -- 1/2/3
  FOREIGN KEY (ronda_id) REFERENCES dbo.rondas_juego(ronda_id)
);

CREATE TABLE dbo.apuestas_ruleta (
  id_item_apuesta uniqueidentifier NOT NULL PRIMARY KEY,
  apuesta_id uniqueidentifier NOT NULL,
  tipo_apuesta nvarchar(32) NOT NULL,    -- PLENO, CABALLO, ROJO, NEGRO, PAR, IMPAR, DOCENA, COLUMNA...
  seleccion nvarchar(64) NULL,           -- "17" o "17-20" o "ROJO"
  cuota_x int NOT NULL,                  -- 35, 17, 1, 2...
  FOREIGN KEY (id_item_apuesta) REFERENCES dbo.items_apuesta(id_item_apuesta),
  FOREIGN KEY (apuesta_id) REFERENCES dbo.apuestas(apuesta_id)
);

--Tablas para el blackjack. 
CREATE TABLE dbo.mesas_blackjack (
  mesa_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  nombre nvarchar(64) NOT NULL,
  apuesta_minima_cent bigint NOT NULL,
  apuesta_maxima_cent bigint NOT NULL,
  reglas_json nvarchar(max) NULL,
  activa bit NOT NULL DEFAULT 1
);

CREATE TABLE dbo.manos_blackjack (
  mano_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  ronda_id uniqueidentifier NOT NULL,
  user_id uniqueidentifier NOT NULL,
  mesa_id uniqueidentifier NOT NULL,
  indice_mano tinyint NOT NULL DEFAULT 1,     -- para splits
  cartas_jugador_json nvarchar(max) NULL,
  cartas_crupier_json nvarchar(max) NULL,
  resultado nvarchar(32) NULL,               -- GANA/PIERDE/EMPATA/BLACKJACK/SE_PASA
  FOREIGN KEY (ronda_id) REFERENCES dbo.rondas_juego(ronda_id),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (mesa_id) REFERENCES dbo.mesas_blackjack(mesa_id)
);
CREATE INDEX IX_manos_blackjack_ronda ON dbo.manos_blackjack(ronda_id);

CREATE TABLE dbo.acciones_blackjack (
  accion_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  mano_id uniqueidentifier NOT NULL,
  secuencia int NOT NULL,
  accion nvarchar(16) NOT NULL,              -- PEDIR/PLANTARSE/DOBLAR/DIVIDIR (o HIT/STAND...)
  fecha_creacion datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  payload_json nvarchar(max) NULL,
  FOREIGN KEY (mano_id) REFERENCES dbo.manos_blackjack(mano_id)
);
CREATE UNIQUE INDEX UX_acciones_blackjack_mano_seq ON dbo.acciones_blackjack(mano_id, secuencia);

--Tablas pagos.

CREATE TABLE dbo.proveedores_pago (
  proveedor_id tinyint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE,      -- STRIPE, ADYEN, PAYPAL...
  nombre nvarchar(64) NOT NULL
);

CREATE TABLE dbo.metodos_pago (
  metodo_id tinyint NOT NULL PRIMARY KEY,
  codigo nvarchar(32) NOT NULL UNIQUE,      -- TARJETA, TRANSFERENCIA, APPLEPAY...
  nombre nvarchar(64) NOT NULL
);

CREATE TABLE dbo.pagos (
  pago_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  wallet_id uniqueidentifier NOT NULL,
  proveedor_id tinyint NOT NULL,
  metodo_id tinyint NOT NULL,
  id_estado_pago tinyint NOT NULL,
  monto_cent bigint NOT NULL,
  id_pago_proveedor nvarchar(128) NULL,     -- id externo del proveedor
  creado datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  modificado datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (wallet_id) REFERENCES dbo.carteras(wallet_id),
  FOREIGN KEY (proveedor_id) REFERENCES dbo.proveedores_pago(proveedor_id),
  FOREIGN KEY (metodo_id) REFERENCES dbo.metodos_pago(metodo_id),
  FOREIGN KEY (id_estado_pago) REFERENCES dbo.estado_pagos(id_estado_pago)
);
CREATE UNIQUE INDEX UX_pagos_proveedor_ext ON dbo.pagos(proveedor_id, id_pago_proveedor);

CREATE TABLE dbo.eventos_pago (
  evento_pago_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  proveedor_id tinyint NOT NULL,
  id_evento_externo nvarchar(128) NOT NULL,
  tipo_evento nvarchar(64) NOT NULL,
  recibido datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  payload_json nvarchar(max) NULL,
  FOREIGN KEY (proveedor_id) REFERENCES dbo.proveedores_pago(proveedor_id)
);
CREATE UNIQUE INDEX UX_eventos_pago_proveedor_evento ON dbo.eventos_pago(proveedor_id, id_evento_externo);

CREATE TABLE dbo.retiros (
  retiro_id uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  user_id uniqueidentifier NOT NULL,
  wallet_id uniqueidentifier NOT NULL,
  monto_cent bigint NOT NULL,
  estado nvarchar(32) NOT NULL,             -- SOLICITADO, PENDIENTE, PAGADO, FALLIDO, CANCELADO
  proveedor_id tinyint NULL,
  id_retiro_proveedor nvarchar(128) NULL,
  creado datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  modificado datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
  FOREIGN KEY (user_id) REFERENCES dbo.usuarios(user_id),
  FOREIGN KEY (wallet_id) REFERENCES dbo.carteras(wallet_id),
  FOREIGN KEY (proveedor_id) REFERENCES dbo.proveedores_pago(proveedor_id)
);
CREATE UNIQUE INDEX UX_retiros_proveedor_ext ON dbo.retiros(proveedor_id, id_retiro_proveedor);
