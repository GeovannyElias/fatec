-- Criar banco
CREATE DATABASE consumo_api;


-- Criar tabela Empresa
CREATE TABLE empresa (
                         id SERIAL PRIMARY KEY,
                         cnpj VARCHAR(18) NOT NULL UNIQUE,
                         razao_social VARCHAR(255) NOT NULL,
                         nome_fantasia VARCHAR(255),
                         logradouro VARCHAR(255)
);


-- Criar tabela Socio
CREATE TABLE socio (
                       id SERIAL PRIMARY KEY,
                       nome_socio VARCHAR(255) NOT NULL,
                       cnpj_cpf_do_socio VARCHAR(18) NOT NULL,
                       qualificacao_socio VARCHAR(255),
                       empresa_id INT NOT NULL,


                       CONSTRAINT fk_empresa
                           FOREIGN KEY (empresa_id)
                               REFERENCES empresa(id)
                               ON DELETE CASCADE,


                       CONSTRAINT unique_socio_empresa
                           UNIQUE (cnpj_cpf_do_socio, empresa_id)
);



