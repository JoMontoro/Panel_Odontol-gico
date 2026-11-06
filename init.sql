-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: odontologico_db
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `caso_clinico`
--

DROP TABLE IF EXISTS `caso_clinico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caso_clinico` (
  `id` int NOT NULL AUTO_INCREMENT,
  `diagnostico` varchar(255) DEFAULT NULL,
  `estado` enum('CANCELADO','EN_PROCESO','FINALIZADO','SUSPENDIDO') DEFAULT NULL,
  `fecha_fin` date DEFAULT NULL,
  `fecha_inicio` date DEFAULT NULL,
  `motivo_consulta` varchar(255) DEFAULT NULL,
  `id_odontologo_responsable` int NOT NULL,
  `id_paciente` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7ketk6si7v2yq2gjv7dumlgo9` (`id_odontologo_responsable`),
  KEY `FKe57xgfilu7ckaj7phkuossy2n` (`id_paciente`),
  CONSTRAINT `FK7ketk6si7v2yq2gjv7dumlgo9` FOREIGN KEY (`id_odontologo_responsable`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKe57xgfilu7ckaj7phkuossy2n` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caso_clinico`
--

LOCK TABLES `caso_clinico` WRITE;
/*!40000 ALTER TABLE `caso_clinico` DISABLE KEYS */;
INSERT INTO `caso_clinico` VALUES (1,NULL,'FINALIZADO','2026-10-12','2026-10-12','mucho dolor de muela',1,1),(2,NULL,'EN_PROCESO',NULL,'2026-10-25','dolor de muela',1,2),(3,NULL,'EN_PROCESO',NULL,'2026-10-25','dolor molar',1,3);
/*!40000 ALTER TABLE `caso_clinico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cerrada_por_pago` bit(1) DEFAULT NULL,
  `estado` enum('CANCELADA','CONFIRMADA','EN_ATENCION','FINALIZADA','NO_ASISTIO','PROGRAMADA') DEFAULT NULL,
  `fecha` date DEFAULT NULL,
  `hora_fin` time(6) DEFAULT NULL,
  `hora_inicio` time(6) DEFAULT NULL,
  `observaciones` text,
  `tipo` enum('CONSULTA','CONTROL','DIAGNOSTICO','EMERGENCIA','TRATAMIENTO') DEFAULT NULL,
  `id_caso_clinico` int NOT NULL,
  `id_odontologo` int NOT NULL,
  `id_paciente` int NOT NULL,
  `id_plan_detalle` int DEFAULT NULL,
  `id_tratamiento` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7wvh0abi52k22v8omcmvepk7f` (`id_caso_clinico`),
  KEY `FK563cw5pe0d2w7d6j4bin9w24u` (`id_odontologo`),
  KEY `FK7fljkhue1c7r80b4li70f6fh3` (`id_paciente`),
  KEY `FKmbsi56xux3qcvvs3psw6rbt8a` (`id_plan_detalle`),
  KEY `FK7kfjohya9lfc8ul3vyn8jsbas` (`id_tratamiento`),
  CONSTRAINT `FK563cw5pe0d2w7d6j4bin9w24u` FOREIGN KEY (`id_odontologo`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FK7fljkhue1c7r80b4li70f6fh3` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`),
  CONSTRAINT `FK7kfjohya9lfc8ul3vyn8jsbas` FOREIGN KEY (`id_tratamiento`) REFERENCES `tratamiento` (`id`),
  CONSTRAINT `FK7wvh0abi52k22v8omcmvepk7f` FOREIGN KEY (`id_caso_clinico`) REFERENCES `caso_clinico` (`id`),
  CONSTRAINT `FKmbsi56xux3qcvvs3psw6rbt8a` FOREIGN KEY (`id_plan_detalle`) REFERENCES `plan_detalle` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `consentimiento_informado`
--

DROP TABLE IF EXISTS `consentimiento_informado`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `consentimiento_informado` (
  `id` int NOT NULL AUTO_INCREMENT,
  `aceptado` bit(1) NOT NULL,
  `contenido` text,
  `fecha_aceptacion` datetime(6) DEFAULT NULL,
  `firma_odontologo_base64` text,
  `firma_paciente_base64` text,
  `ip_address` varchar(255) DEFAULT NULL,
  `pdf_ruta` text,
  `tipo_procedimiento` enum('BLANQUEAMIENTO','CIRUGIA_ORAL','ENDODONCIA','EXTRACION_DENTAL','GENERAL','IMPLANTES','ORTONCIA','PERIODONCIA','PROTESIS') NOT NULL,
  `titulo` varchar(200) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `version` varchar(20) NOT NULL,
  `id_historial_clinico` int NOT NULL,
  `id_paciente` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1ceat8rbyo6dc36igj886u7eu` (`id_historial_clinico`),
  KEY `FK95ol0vn1ndt5370cbfhkpdiri` (`id_paciente`),
  CONSTRAINT `FK1ceat8rbyo6dc36igj886u7eu` FOREIGN KEY (`id_historial_clinico`) REFERENCES `historial_clinico` (`id`),
  CONSTRAINT `FK95ol0vn1ndt5370cbfhkpdiri` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `consentimiento_informado`
--

LOCK TABLES `consentimiento_informado` WRITE;
/*!40000 ALTER TABLE `consentimiento_informado` DISABLE KEYS */;
INSERT INTO `consentimiento_informado` VALUES (1,_binary '','Yo, paciente / representante legal, declaro haber sido informado(a) sobre el diagnóstico, el plan de tratamiento odontológico, sus beneficios, riesgos, alternativas y costos. Autorizo al profesional a realizar los procedimientos necesarios para mi atención y acepto las indicaciones post-tratamiento.','2026-10-25 22:24:53.208179',NULL,'data:image/png;base64,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','0:0:0:0:0:0:0:1',NULL,'CIRUGIA_ORAL','Consentimiento informado - Cirugía oral','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36','1.0',3,3),(2,_binary '','Yo, paciente / representante legal, declaro haber sido informado(a) sobre el diagnóstico, el plan de tratamiento odontológico, sus beneficios, riesgos, alternativas y costos. Autorizo al profesional a realizar los procedimientos necesarios para mi atención y acepto las indicaciones post-tratamiento.','2026-10-25 22:25:29.489558',NULL,'data:image/png;base64,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','0:0:0:0:0:0:0:1',NULL,'BLANQUEAMIENTO','Consentimiento informado - Blanqueamiento dental','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36','1.0',1,1);
/*!40000 ALTER TABLE `consentimiento_informado` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `control_evolucion`
--

DROP TABLE IF EXISTS `control_evolucion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `control_evolucion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `monto_pagado` double DEFAULT NULL,
  `observaciones` text,
  `procedimiento_realizado` text,
  `id_caso_clinico` int NOT NULL,
  `id_cita` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjwnt6gqioooyyv6x1puupxdks` (`id_caso_clinico`),
  KEY `FKjmtids6xylmo8od35vc23sk20` (`id_cita`),
  CONSTRAINT `FKjmtids6xylmo8od35vc23sk20` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id`),
  CONSTRAINT `FKjwnt6gqioooyyv6x1puupxdks` FOREIGN KEY (`id_caso_clinico`) REFERENCES `caso_clinico` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `control_evolucion`
--

LOCK TABLES `control_evolucion` WRITE;
/*!40000 ALTER TABLE `control_evolucion` DISABLE KEYS */;
/*!40000 ALTER TABLE `control_evolucion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnostico`
--

DROP TABLE IF EXISTS `diagnostico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diagnostico` (
  `id` int NOT NULL AUTO_INCREMENT,
  `descripcion` text,
  `fecha` date DEFAULT NULL,
  `id_caso_clinico` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtgcpmkfkoyhw3fq5vqexqicmt` (`id_caso_clinico`),
  CONSTRAINT `FKtgcpmkfkoyhw3fq5vqexqicmt` FOREIGN KEY (`id_caso_clinico`) REFERENCES `caso_clinico` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnostico`
--

LOCK TABLES `diagnostico` WRITE;
/*!40000 ALTER TABLE `diagnostico` DISABLE KEYS */;
/*!40000 ALTER TABLE `diagnostico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historial_clinico`
--

DROP TABLE IF EXISTS `historial_clinico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historial_clinico` (
  `id` int NOT NULL AUTO_INCREMENT,
  `alergias` text,
  `antecedentes_medicos` text,
  `enfermedades` text,
  `observaciones_generales` text,
  `updated_at` datetime(6) DEFAULT NULL,
  `id_paciente` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlr7j686ecp6ib3p8ik64dkm2u` (`id_paciente`),
  CONSTRAINT `FK4qp51fvm3o14hmrimqv8jd79p` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historial_clinico`
--

LOCK TABLES `historial_clinico` WRITE;
/*!40000 ALTER TABLE `historial_clinico` DISABLE KEYS */;
INSERT INTO `historial_clinico` VALUES (1,'penincilina','diabetes','','','2026-10-12 19:28:39.026754',1),(2,NULL,NULL,NULL,NULL,'2026-10-25 21:57:59.823678',2),(3,NULL,NULL,NULL,NULL,'2026-10-25 22:04:54.774125',3);
/*!40000 ALTER TABLE `historial_clinico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odontograma_diente`
--

DROP TABLE IF EXISTS `odontograma_diente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odontograma_diente` (
  `id` int NOT NULL AUTO_INCREMENT,
  `numero_diente` int DEFAULT NULL,
  `id_odontograma` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKacq2xcrxtpqo7o6rp9k588td6` (`id_odontograma`),
  CONSTRAINT `FKacq2xcrxtpqo7o6rp9k588td6` FOREIGN KEY (`id_odontograma`) REFERENCES `odontograma_evaluacion` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odontograma_diente`
--

LOCK TABLES `odontograma_diente` WRITE;
/*!40000 ALTER TABLE `odontograma_diente` DISABLE KEYS */;
INSERT INTO `odontograma_diente` VALUES (1,51,1),(2,65,1),(3,71,1),(4,73,1),(5,83,1);
/*!40000 ALTER TABLE `odontograma_diente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odontograma_diente_condiciones`
--

DROP TABLE IF EXISTS `odontograma_diente_condiciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odontograma_diente_condiciones` (
  `id_odontograma_diente` int NOT NULL,
  `condicion` enum('AUSENTE','CARIES','CORONA','ENDODONCIA','ERUPCIONADO','EXTRACCION_INDICADA','FRACTURA','IMPACTADO','IMPLANTE','MOBILIDAD','OBTURADO','PROTESIS','SENSIBILIDAD','SUPERNUMERARIO') DEFAULT NULL,
  KEY `FK7j7isv0fbjcy4yviwqiqt4sdw` (`id_odontograma_diente`),
  CONSTRAINT `FK7j7isv0fbjcy4yviwqiqt4sdw` FOREIGN KEY (`id_odontograma_diente`) REFERENCES `odontograma_diente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odontograma_diente_condiciones`
--

LOCK TABLES `odontograma_diente_condiciones` WRITE;
/*!40000 ALTER TABLE `odontograma_diente_condiciones` DISABLE KEYS */;
INSERT INTO `odontograma_diente_condiciones` VALUES (1,'ENDODONCIA'),(2,'SENSIBILIDAD'),(3,'CORONA'),(4,'FRACTURA'),(5,'AUSENTE');
/*!40000 ALTER TABLE `odontograma_diente_condiciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odontograma_evaluacion`
--

DROP TABLE IF EXISTS `odontograma_evaluacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odontograma_evaluacion` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `observaciones` text,
  `tipo_dentadura` enum('ADULTO','NINO') DEFAULT NULL,
  `id_odontologo` int DEFAULT NULL,
  `id_paciente` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl40gw5v91nm6n48ocapv1d665` (`id_odontologo`),
  KEY `FKoh324xoxuh9r4hraam2sduy6t` (`id_paciente`),
  CONSTRAINT `FKl40gw5v91nm6n48ocapv1d665` FOREIGN KEY (`id_odontologo`) REFERENCES `usuario` (`id`),
  CONSTRAINT `FKoh324xoxuh9r4hraam2sduy6t` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odontograma_evaluacion`
--

LOCK TABLES `odontograma_evaluacion` WRITE;
/*!40000 ALTER TABLE `odontograma_evaluacion` DISABLE KEYS */;
INSERT INTO `odontograma_evaluacion` VALUES (1,'2026-10-12 19:26:35.434601',NULL,'NINO',1,1);
/*!40000 ALTER TABLE `odontograma_evaluacion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `odontograma_tejido_blando`
--

DROP TABLE IF EXISTS `odontograma_tejido_blando`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `odontograma_tejido_blando` (
  `id` int NOT NULL AUTO_INCREMENT,
  `estado` enum('ATROFIA','INFLAMACION','LESION','NEOPLASIA','NORMAL','NO_EVALUADO','SANGRADO','TRAUMA','ULCERA') DEFAULT NULL,
  `observacion` varchar(255) DEFAULT NULL,
  `zona` varchar(255) DEFAULT NULL,
  `id_odontograma` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi5od170n0mh0evq7tol4awq56` (`id_odontograma`),
  CONSTRAINT `FKi5od170n0mh0evq7tol4awq56` FOREIGN KEY (`id_odontograma`) REFERENCES `odontograma_evaluacion` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `odontograma_tejido_blando`
--

LOCK TABLES `odontograma_tejido_blando` WRITE;
/*!40000 ALTER TABLE `odontograma_tejido_blando` DISABLE KEYS */;
INSERT INTO `odontograma_tejido_blando` VALUES (1,'INFLAMACION',NULL,'Lengua',1),(2,'SANGRADO',NULL,'Ganglios linfáticos',1);
/*!40000 ALTER TABLE `odontograma_tejido_blando` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paciente`
--

DROP TABLE IF EXISTS `paciente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paciente` (
  `id` int NOT NULL AUTO_INCREMENT,
  `apellido` varchar(255) DEFAULT NULL,
  `apoderado` varchar(255) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `dni` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `estado` bit(1) DEFAULT NULL,
  `estado_atencion` enum('ATENDIDO','EN_ATENCION') DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `id_odontologo` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqdgfhcpe13l3ski7rkx3lv7hs` (`id_odontologo`),
  CONSTRAINT `FKqdgfhcpe13l3ski7rkx3lv7hs` FOREIGN KEY (`id_odontologo`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paciente`
--

LOCK TABLES `paciente` WRITE;
/*!40000 ALTER TABLE `paciente` DISABLE KEYS */;
INSERT INTO `paciente` VALUES (1,'MONTORO',NULL,'Av. Holanda (antes avenida D)','54874734','jmg050299@gmail.com',_binary '','ATENDIDO','2005-02-05','JOSE Antonio','973573743',1),(2,'Montoro','Si','Av. Holanda (antes avenida D)','78277492','fabio@gmail.com',_binary '','EN_ATENCION','2007-01-17','Fabio','982173887',1),(3,'Garcia','Si','Av. Holanda (antes avenida D)','87167836','Maria@gmail.com',_binary '','EN_ATENCION','2007-07-17','Maria','987384372',1);
/*!40000 ALTER TABLE `paciente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pago`
--

DROP TABLE IF EXISTS `pago`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pago` (
  `id` int NOT NULL AUTO_INCREMENT,
  `fecha` date DEFAULT NULL,
  `monto` double DEFAULT NULL,
  `id_presupuesto` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk0g205qfrjd1jga89pun15vj1` (`id_presupuesto`),
  CONSTRAINT `FKk0g205qfrjd1jga89pun15vj1` FOREIGN KEY (`id_presupuesto`) REFERENCES `presupuesto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pago`
--

LOCK TABLES `pago` WRITE;
/*!40000 ALTER TABLE `pago` DISABLE KEYS */;
INSERT INTO `pago` VALUES (1,'2026-10-12',630,1),(2,'2026-10-13',450,2);
/*!40000 ALTER TABLE `pago` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_detalle`
--

DROP TABLE IF EXISTS `plan_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_detalle` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cara` varchar(255) DEFAULT NULL,
  `diente` int DEFAULT NULL,
  `estado` enum('CANCELADO','PENDIENTE','REALIZADO') DEFAULT NULL,
  `orden` int DEFAULT NULL,
  `precio` double DEFAULT NULL,
  `procedimiento` varchar(255) DEFAULT NULL,
  `id_plan` int DEFAULT NULL,
  `id_tratamiento` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2m7nq54u5r51ycqkfdh2wmjqv` (`id_plan`),
  KEY `FKf236c4teytpcob8fqs5ci81id` (`id_tratamiento`),
  CONSTRAINT `FK2m7nq54u5r51ycqkfdh2wmjqv` FOREIGN KEY (`id_plan`) REFERENCES `plan_tratamiento` (`id`),
  CONSTRAINT `FKf236c4teytpcob8fqs5ci81id` FOREIGN KEY (`id_tratamiento`) REFERENCES `tratamiento` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_detalle`
--

LOCK TABLES `plan_detalle` WRITE;
/*!40000 ALTER TABLE `plan_detalle` DISABLE KEYS */;
INSERT INTO `plan_detalle` VALUES (1,NULL,NULL,'REALIZADO',1,100,'Alveoloplastia',1,19),(2,NULL,NULL,'REALIZADO',2,400,'Carillas cerámicas',1,34),(3,NULL,NULL,'REALIZADO',3,30,'Consulta inicial / diagnóstico',1,36),(4,NULL,NULL,'REALIZADO',4,120,'Exodoncia compleja / quirúrgica',1,17),(8,NULL,NULL,'REALIZADO',1,400,'Carillas cerámicas',2,34),(9,NULL,NULL,'REALIZADO',2,30,'Consulta inicial / diagnóstico',2,36),(10,NULL,NULL,'REALIZADO',3,20,'Aplicación de flúor',2,41);
/*!40000 ALTER TABLE `plan_detalle` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plan_tratamiento`
--

DROP TABLE IF EXISTS `plan_tratamiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plan_tratamiento` (
  `id` int NOT NULL AUTO_INCREMENT,
  `costo_estimado` double DEFAULT NULL,
  `estado` enum('CANCELADO','COMPLETADO','EN_PROCESO','PENDIENTE') DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `sesiones_estimadas` int DEFAULT NULL,
  `id_caso_clinico` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKorjwpp3lb1esoebiesn33nu5w` (`id_caso_clinico`),
  CONSTRAINT `FKorjwpp3lb1esoebiesn33nu5w` FOREIGN KEY (`id_caso_clinico`) REFERENCES `caso_clinico` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plan_tratamiento`
--

LOCK TABLES `plan_tratamiento` WRITE;
/*!40000 ALTER TABLE `plan_tratamiento` DISABLE KEYS */;
INSERT INTO `plan_tratamiento` VALUES (1,650,'COMPLETADO','Plan de tratamiento',1,1),(2,450,'COMPLETADO','Plan de tratamiento',1,1),(3,150,'PENDIENTE','Plan sugerido',1,1);
/*!40000 ALTER TABLE `plan_tratamiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presupuesto`
--

DROP TABLE IF EXISTS `presupuesto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presupuesto` (
  `id` int NOT NULL AUTO_INCREMENT,
  `adelanto` double DEFAULT NULL,
  `descuento` double DEFAULT NULL,
  `estado` enum('APROBADO','PAGADO','PENDIENTE','RECHAZADO') DEFAULT NULL,
  `monto_personalizado` double DEFAULT NULL,
  `saldo_pendiente` double DEFAULT NULL,
  `subtotal` double DEFAULT NULL,
  `total` double DEFAULT NULL,
  `id_caso_clinico` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_presupuesto_caso` (`id_caso_clinico`),
  CONSTRAINT `FKdbkcs7enhm5wwq9f4pkxofctd` FOREIGN KEY (`id_caso_clinico`) REFERENCES `caso_clinico` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presupuesto`
--

LOCK TABLES `presupuesto` WRITE;
/*!40000 ALTER TABLE `presupuesto` DISABLE KEYS */;
INSERT INTO `presupuesto` VALUES (1,630,20,'PAGADO',NULL,0,650,630,1),(2,450,0,'PAGADO',NULL,0,450,450,1);
/*!40000 ALTER TABLE `presupuesto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recuperacion_password`
--

DROP TABLE IF EXISTS `recuperacion_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recuperacion_password` (
  `id` int NOT NULL AUTO_INCREMENT,
  `codigo` varchar(6) NOT NULL,
  `correo` varchar(255) NOT NULL,
  `creado_en` datetime(6) DEFAULT NULL,
  `expira_en` datetime(6) DEFAULT NULL,
  `intentos` int DEFAULT NULL,
  `usado` bit(1) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recuperacion_password`
--

LOCK TABLES `recuperacion_password` WRITE;
/*!40000 ALTER TABLE `recuperacion_password` DISABLE KEYS */;
/*!40000 ALTER TABLE `recuperacion_password` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tratamiento`
--

DROP TABLE IF EXISTS `tratamiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tratamiento` (
  `id` int NOT NULL AUTO_INCREMENT,
  `activo` bit(1) DEFAULT NULL,
  `categoria` varchar(255) DEFAULT NULL,
  `codigo` varchar(255) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `duracion_minutos` int DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `precio_referencia` double DEFAULT NULL,
  `id_odontologo` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm5g73v24tg28v42wodpxw5cwa` (`id_odontologo`),
  CONSTRAINT `FKm5g73v24tg28v42wodpxw5cwa` FOREIGN KEY (`id_odontologo`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tratamiento`
--

LOCK TABLES `tratamiento` WRITE;
/*!40000 ALTER TABLE `tratamiento` DISABLE KEYS */;
INSERT INTO `tratamiento` VALUES (17,_binary '','CIRUGIA','CIR-02','Extracción con ostectomía / odontosección',60,'Exodoncia compleja / quirúrgica',120,1),(19,_binary '','CIRUGIA','CIR-04','Regularización de reborde alveolar',45,'Alveoloplastia',100,1),(34,_binary '','ESTETICA','EST-03','Por unidad',120,'Carillas cerámicas',400,1),(36,_binary '','DIAGNOSTICO','DIA-01','Examen clínico + radiografías',30,'Consulta inicial / diagnóstico',30,1),(41,_binary '','PREVENTIVO','DIA-06','Tópico profesional',15,'Aplicación de flúor',20,1);
/*!40000 ALTER TABLE `tratamiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `apellido` varchar(255) DEFAULT NULL,
  `correo` varchar(255) DEFAULT NULL,
  `especialidad` varchar(255) DEFAULT NULL,
  `estado` bit(1) DEFAULT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `rol` enum('ADMIN','ODONTOLOGO') DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'Reupo','gina.odontologia@gmail.com','Odontología General',_binary '',NULL,'Gina','$2a$10$0uSfz.BYRiP2QySmHVJsXe9geFGizckFLY6tOMRc.UbVq9tD3cNVq','ODONTOLOGO','Dr.Gina'),(2,'Montalvo','ruben.odontologia@gmail.com','Ortodoncia',_binary '',NULL,'Ruben','$2a$10$iEMBnx8iB7/SQTOtWWhGnueKb01nFi5GcNe2xpKyqm/.YrNHc.zAu','ODONTOLOGO','Dr.Ruben');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-02 15:41:19
