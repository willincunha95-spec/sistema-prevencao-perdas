-- Índices para otimização de busca na tabela registers

-- Índice para busca rápida por protocolo (usado em findByProtocol)
CREATE INDEX IF NOT EXISTS idx_registers_protocol ON registers(protocol);

-- Índice para filtragem eficiente por status (usado em findByStatus e countByStatus)
CREATE INDEX IF NOT EXISTS idx_registers_status ON registers(status);

-- Índice para SKU, comumente usado em buscas de inventário
CREATE INDEX IF NOT EXISTS idx_registers_sku ON registers(sku);

-- Índice para timestamp para relatórios temporais rápidos
CREATE INDEX IF NOT EXISTS idx_registers_timestamp ON registers(timestamp);
