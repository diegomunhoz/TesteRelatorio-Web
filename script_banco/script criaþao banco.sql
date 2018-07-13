SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `academico` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci ;
USE `academico`;

-- -----------------------------------------------------
-- Table `academico`.`aluno`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `academico`.`aluno` (
  `id_aluno` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(120) NOT NULL ,
  `matricula` VARCHAR(20) NOT NULL COMMENT 'unique key' ,
  `situacao_frequencia` CHAR(1) NOT NULL COMMENT 'A - ATIVO\nI - INATIVO ' ,
  `situacao_pagamento` CHAR(1) NOT NULL COMMENT 'A - ADIMPLENTE\nI - INADIMPLENTE' ,
  PRIMARY KEY (`id_aluno`) ,
  UNIQUE INDEX `UNIQUE` (`matricula` ASC) )
ENGINE = InnoDB
COMMENT = 'Tabela de alunos da escola';


-- -----------------------------------------------------
-- Table `academico`.`professor`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `academico`.`professor` (
  `id_professor` INT NOT NULL AUTO_INCREMENT ,
  `nome` VARCHAR(120) NOT NULL ,
  `matricula` VARCHAR(20) NOT NULL ,
  `titulacao` VARCHAR(20) NOT NULL ,
  `situacao` CHAR(1) NOT NULL COMMENT 'A - ATIVO\nP - APOSENTADO\nL - LICENCIADO\nC - CEDIDO' ,
  PRIMARY KEY (`id_professor`) ,
  UNIQUE INDEX `UNIQUE` (`matricula` ASC) )
ENGINE = InnoDB
COMMENT = 'Tabela de professores dos alunos';


-- -----------------------------------------------------
-- Table `academico`.`professores_alunos`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `academico`.`professores_alunos` (
  `id_professor` INT NOT NULL ,
  `id_aluno` INT NOT NULL ,
  PRIMARY KEY (`id_aluno`, `id_professor`) ,
  INDEX `fk_aluno` (`id_aluno` ASC) ,
  INDEX `fk_professor` (`id_professor` ASC) ,
  CONSTRAINT `fk_aluno`
    FOREIGN KEY (`id_aluno` )
    REFERENCES `academico`.`aluno` (`id_aluno` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_professor`
    FOREIGN KEY (`id_professor` )
    REFERENCES `academico`.`professor` (`id_professor` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
