CREATE TABLE IF NOT EXISTS FUNKOS(
 id INT PRIMARY KEY AUTO_INCREMENT,
 cod UUID NOT NULL DEFAULT RANDOM_UUID(),
 nombre VARCHAR(255) NOT NULL,
 modelo VARCHAR(20) CHECK(modelo IN ('MARVEL','DISNEY','ANIME','OTROS')),
 precio DOUBLE NOT NULL,
 fecha_lanzamiento DATE NOT NULL
);
