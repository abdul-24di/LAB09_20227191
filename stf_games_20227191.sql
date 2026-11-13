DROP DATABASE IF EXISTS stf_games_20227191;
CREATE DATABASE stf_games_20227191 DEFAULT CHARACTER SET utf8mb4;
USE stf_games_20227191;

CREATE TABLE stf_puzzle_sv (
    -- Posicion de cada ficha leyendo el tablero fila por fila; X = casilla vacia.
    -- 3x3: "13652847X" (sin separador, como el ejemplo). 4x4 en adelante: "1-2-...-X"
    stf_game_board_structure VARCHAR(255) NOT NULL,
    -- Imagen del personaje como binario (clase 5.3, metodo 2). NULL = imagen por defecto
    image LONGBLOB NULL
);

-- Unica fila: tablero ordenado = no hay juego en curso
INSERT INTO stf_puzzle_sv (stf_game_board_structure, image) VALUES ('12345678X', NULL);