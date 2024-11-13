SET FOREIGN_KEY_CHECKS=0;

-- location definition

CREATE TABLE `location` (
                            `locationid` bigint(20) NOT NULL AUTO_INCREMENT,
                            `direction` varchar(255) DEFAULT NULL,
                            `height` float NOT NULL,
                            `is_outside` bit(1) NOT NULL,
                            `latitude` float NOT NULL,
                            `longitude` float NOT NULL,
                            PRIMARY KEY (`locationid`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- mail definition

CREATE TABLE `mail` (
                        `mail_id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `body` varchar(255) DEFAULT NULL,
                        `footer` varchar(255) DEFAULT NULL,
                        `header` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`mail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- region definition

CREATE TABLE `region` (
                          `id` bigint(20) NOT NULL,
                          `name` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- sensor_type definition

CREATE TABLE `sensor_type` (
                               `typeid` bigint(20) NOT NULL AUTO_INCREMENT,
                               `type_name` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`typeid`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- `user` definition

CREATE TABLE `user` (
                        `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `is_admin` bit(1) NOT NULL,
                        `first_name` varchar(255) DEFAULT NULL,
                        `last_name` varchar(255) DEFAULT NULL,
                        `email` varchar(255) DEFAULT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- workshopcode definition

CREATE TABLE `workshopcode` (
                                `code` bigint(20) NOT NULL,
                                `expiration_date` datetime DEFAULT NULL,
                                `creation_date` datetime DEFAULT NULL,
                                PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- region_cords definition

CREATE TABLE `region_cords` (
                                `id` bigint(20) NOT NULL,
                                `region_id` bigint(20) DEFAULT NULL,
                                `latitude` float NOT NULL,
                                `longitude` float NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `FKiu776j92u2nvrsrdikg2stjvi` (`region_id`),
                                CONSTRAINT `FKiu776j92u2nvrsrdikg2stjvi` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- station definition

CREATE TABLE `station` (
                           `stationid` bigint(20) NOT NULL AUTO_INCREMENT,
                           `name` varchar(255) DEFAULT NULL,
                           `database_tag` varchar(255) DEFAULT NULL,
                           `is_public` bit(1) NOT NULL,
                           `registration_code` bigint(20) NOT NULL,
                           `location_locationid` bigint(20) DEFAULT NULL,
                           `user_id` bigint(20) DEFAULT NULL,
                           `is_active` bit(1) DEFAULT NULL,
                           PRIMARY KEY (`stationid`),
                           KEY `FKguexbm09vbcok067wdao85dw0` (`location_locationid`),
                           KEY `FKjpfw49q2hwosivlesvqxjy6qo` (`user_id`),
                           CONSTRAINT `FKguexbm09vbcok067wdao85dw0` FOREIGN KEY (`location_locationid`) REFERENCES `location` (`locationid`),
                           CONSTRAINT `FKjpfw49q2hwosivlesvqxjy6qo` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- token definition

CREATE TABLE `token` (
                         `id` bigint(20) NOT NULL,
                         `creation_time` datetime(6) DEFAULT NULL,
                         `link_hash` varchar(255) DEFAULT NULL,
                         `user_id` bigint(20) DEFAULT NULL,
                         `numeric_code` varchar(255) DEFAULT NULL,
                         PRIMARY KEY (`id`),
                         KEY `FKp2lrydh553poqd4jq5f2gxh5u` (`user_id`),
                         CONSTRAINT `FKp2lrydh553poqd4jq5f2gxh5u` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;


-- sensor definition

CREATE TABLE `sensor` (
                          `sensorid` bigint(20) NOT NULL AUTO_INCREMENT,
                          `active_data` bit(1) NOT NULL,
                          `sensor_data` int(11) NOT NULL,
                          `sensor_typeid` bigint(20) DEFAULT NULL,
                          `stationid` bigint(20) DEFAULT NULL,
                          PRIMARY KEY (`sensorid`),
                          KEY `FKokblxlilng14k37vbnax547yi` (`sensor_typeid`),
                          KEY `FK73l0iai9750tbfx3p3i1jtty6` (`stationid`),
                          CONSTRAINT `FK73l0iai9750tbfx3p3i1jtty6` FOREIGN KEY (`stationid`) REFERENCES `station` (`stationid`),
                          CONSTRAINT `FKokblxlilng14k37vbnax547yi` FOREIGN KEY (`sensor_typeid`) REFERENCES `sensor_type` (`typeid`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- Seed locations

INSERT INTO location (direction,height,is_outside,latitude,longitude) VALUES
                                                                                               ('E',10.0,0,51.5474,5.04709),
                                                                                               ('E',10.0,0,51.5612,5.06723),
                                                                                               ('E',10.0,0,51.5403,5.08047),
                                                                                               ('S',50.0,1,51.577,5.09702),
                                                                                               ('E',10.0,1,51.5589,5.0665),
                                                                                               ('N',10.0,1,51.5599,5.07736),
                                                                                               ('N',10.0,1,51.557,5.10663),
                                                                                               ('S',10.0,1,51.549,5.05069),
                                                                                               ('S',10.0,1,51.5405,5.06897),
                                                                                               ('W',10.0,1,51.5843,5.0162);
INSERT INTO location (direction,height,is_outside,latitude,longitude) VALUES
                                                                                               ('E',10.0,1,51.5823,5.00256),
                                                                                               ('W',10.0,1,51.5781,4.99295),
                                                                                               ('S',10.0,1,51.587,4.97537),
                                                                                               ('S',10.0,1,51.5714,4.98474),
                                                                                               ('N',10.0,1,51.5795,5.07733),
                                                                                               ('N',10.0,1,51.5802,5.08682),
                                                                                               ('N',10.0,1,51.5821,5.10071),
                                                                                               ('E',10.0,1,51.6121,5.14792),
                                                                                               ('S',10.0,1,51.577,5.13892);

-- Seed regions

INSERT INTO region (id,name) VALUES
                                                      (1,'Tilburg Noord'),
                                                      (2,'Tilburg Oud-Noord'),
                                                      (3,'Tilburg West'),
                                                      (4,'Tilburg Reeshof'),
                                                      (5,'Tilburg Centrum'),
                                                      (6,'Tilburg Zuid'),
                                                      (7,'Trouwlaan'),
                                                      (8,'Moerenburg'),
                                                      (9,'Het Zand'),
                                                      (10,'Industrieterrein-oost');
INSERT INTO region (id,name) VALUES
                                                      (11,'Groenewoud'),
                                                      (12,'Tuindorp De Kievit'),
                                                      (13,'Armhoef'),
                                                      (14,'De Lijnse Hoek'),
                                                      (15,'Korvel'),
                                                      (16,'Zorgvlied'),
                                                      (17,'Jeruzalem'),
                                                      (18,'De Blaak'),
                                                      (19,'Hoefstraat'),
                                                      (20,'Koolhoven');
INSERT INTO region (id,name) VALUES
                                                      (21,'Witbrant'),
                                                      (22,'Groeseind'),
                                                      (23,'Stokhasselt-zuid'),
                                                      (24,'Rooi Harten'),
                                                      (25,'De Oude Warande'),
                                                      (26,'Wandelbos-zuid'),
                                                      (27,'Koningshoeven'),
                                                      (28,'Hasselt'),
                                                      (29,'Mariaziekenhuis-Vredeburcht'),
                                                      (30,'Heikant');
INSERT INTO region (id,name) VALUES
                                                      (31,'Dalem'),
                                                      (32,'Het Laar'),
                                                      (33,'Vlashof'),
                                                      (34,'Wandelbos-noord'),
                                                      (35,'Stokhasselt-noord'),
                                                      (36,'Huibeven'),
                                                      (37,'Heerevelden'),
                                                      (38,'Industrieterrein Loven'),
                                                      (39,'Campenhoef'),
                                                      (40,'Loven');
INSERT INTO region (id,name) VALUES
                                                      (41,'Dongewijk'),
                                                      (42,'Gesworen Hoek'),
                                                      (43,'Industriestrook Goirke-Kanaaldijk'),
                                                      (44,'Heyhoef'),
                                                      (45,'De Quirijnstok'),
                                                      (46,'Industriestrook Lovense-Kanaaldijk'),
                                                      (47,'Bouwmeester'),
                                                      (48,'Noordhoek'),
                                                      (49,'Binnenstad'),
                                                      (50,'Fatima');

-- Seed region_cords

INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (6,2,51.56,5.0918),
                                                                                    (7,2,51.5618,5.09315),
                                                                                    (8,2,51.5619,5.09183),
                                                                                    (9,2,51.5675,5.08828),
                                                                                    (10,2,51.5672,5.08181),
                                                                                    (11,2,51.561,5.08002),
                                                                                    (12,3,51.5436,5.08324),
                                                                                    (13,3,51.5438,5.09306),
                                                                                    (14,3,51.5488,5.1041),
                                                                                    (15,3,51.5526,5.10024);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (16,3,51.5512,5.09584),
                                                                                    (17,3,51.5517,5.09549),
                                                                                    (18,3,51.5512,5.08897),
                                                                                    (19,3,51.5493,5.08822),
                                                                                    (20,3,51.5478,5.08509),
                                                                                    (21,4,51.5681,5.07501),
                                                                                    (22,4,51.5672,5.07676),
                                                                                    (23,4,51.567,5.07946),
                                                                                    (24,4,51.567,5.08114),
                                                                                    (25,4,51.5676,5.08367);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (26,4,51.5678,5.08708),
                                                                                    (27,4,51.573,5.08462),
                                                                                    (28,4,51.5769,5.08425),
                                                                                    (29,4,51.5766,5.07736),
                                                                                    (30,4,51.5716,5.07918),
                                                                                    (31,4,51.57,5.0765),
                                                                                    (32,5,51.5586,5.03868),
                                                                                    (33,5,51.558,5.0413),
                                                                                    (34,5,51.5562,5.06391),
                                                                                    (35,5,51.563,5.06527);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (36,5,51.5664,5.0426),
                                                                                    (37,6,51.5512,5.0887),
                                                                                    (38,6,51.5517,5.09555),
                                                                                    (39,6,51.5513,5.09593),
                                                                                    (40,6,51.5553,5.10635),
                                                                                    (41,6,51.5612,5.10372),
                                                                                    (42,6,51.56,5.09696),
                                                                                    (43,6,51.56,5.09174),
                                                                                    (44,6,51.5572,5.09305),
                                                                                    (45,6,51.5526,5.09055);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (47,7,51.544,5.07753),
                                                                                    (48,7,51.5436,5.08337),
                                                                                    (49,7,51.5479,5.08507),
                                                                                    (50,7,51.5492,5.08819),
                                                                                    (51,7,51.5511,5.08872),
                                                                                    (52,7,51.5524,5.08831),
                                                                                    (53,7,51.5512,5.08464),
                                                                                    (54,7,51.5508,5.07816),
                                                                                    (55,7,51.549,5.07684),
                                                                                    (56,8,51.5621,5.10936);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (57,8,51.5454,5.11925),
                                                                                    (58,8,51.5471,5.11996),
                                                                                    (59,8,51.5506,5.12487),
                                                                                    (60,8,51.5544,5.12173),
                                                                                    (61,8,51.5574,5.12407),
                                                                                    (62,8,51.5596,5.12856),
                                                                                    (63,8,51.5615,5.12719),
                                                                                    (64,8,51.5666,5.12717),
                                                                                    (65,9,51.5664,5.04248),
                                                                                    (66,9,51.5636,5.0618);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (67,9,51.5686,5.06257),
                                                                                    (68,9,51.5721,5.06151),
                                                                                    (69,9,51.5756,5.06306),
                                                                                    (70,9,51.5773,5.04794),
                                                                                    (71,9,51.5768,5.04672),
                                                                                    (72,10,51.5742,5.09682),
                                                                                    (73,10,51.5708,5.10091),
                                                                                    (74,10,51.5701,5.11456),
                                                                                    (75,10,51.5707,5.11514),
                                                                                    (76,10,51.57,5.11778);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (77,10,51.5773,5.12402),
                                                                                    (78,10,51.5834,5.11096),
                                                                                    (79,10,51.5839,5.10863),
                                                                                    (80,10,51.5811,5.10625),
                                                                                    (81,10,51.5806,5.10415),
                                                                                    (82,10,51.5794,5.10287),
                                                                                    (83,10,51.5768,5.10439),
                                                                                    (84,10,51.574,5.10052),
                                                                                    (85,11,51.5369,5.07617),
                                                                                    (86,11,51.5353,5.09436);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (87,11,51.5314,5.10014),
                                                                                    (88,11,51.5366,5.10808),
                                                                                    (89,11,51.5409,5.11351),
                                                                                    (90,11,51.5427,5.1134),
                                                                                    (91,11,51.5451,5.11177),
                                                                                    (92,11,51.5516,5.11002),
                                                                                    (93,11,51.5439,5.09302),
                                                                                    (94,11,51.5436,5.08341),
                                                                                    (95,11,51.544,5.07749),
                                                                                    (96,12,51.584,4.98584);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (97,12,51.5826,4.99638),
                                                                                    (98,12,51.5859,4.9975),
                                                                                    (99,12,51.5844,5.00715),
                                                                                    (100,12,51.5859,5.00934),
                                                                                    (101,12,51.5882,5.00541),
                                                                                    (102,12,51.5911,5.00599),
                                                                                    (103,12,51.6123,4.94963),
                                                                                    (104,12,51.6114,4.94879),
                                                                                    (105,12,51.6031,4.96782),
                                                                                    (106,12,51.6018,4.96881);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (107,12,51.5981,4.96758),
                                                                                    (108,12,51.5972,4.97568),
                                                                                    (109,12,51.5962,4.97764),
                                                                                    (110,12,51.594,4.97794),
                                                                                    (111,13,51.5612,5.10376),
                                                                                    (112,13,51.5552,5.10627),
                                                                                    (113,13,51.5571,5.11166),
                                                                                    (114,13,51.5623,5.10873),
                                                                                    (115,14,51.578,5.07364),
                                                                                    (116,14,51.579,5.083);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (117,14,51.5788,5.08891),
                                                                                    (118,14,51.5782,5.09093),
                                                                                    (119,14,51.5812,5.09381),
                                                                                    (120,14,51.5816,5.08231),
                                                                                    (121,14,51.5828,5.07639),
                                                                                    (122,15,51.5456,5.05408),
                                                                                    (123,15,51.5444,5.0547),
                                                                                    (124,15,51.544,5.05624),
                                                                                    (125,15,51.544,5.07757),
                                                                                    (126,15,51.549,5.07686);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (127,15,51.5507,5.07818),
                                                                                    (128,15,51.5531,5.07918),
                                                                                    (129,15,51.5551,5.07775),
                                                                                    (130,15,51.5537,5.07206),
                                                                                    (131,15,51.5531,5.06469),
                                                                                    (132,15,51.5547,5.06366),
                                                                                    (133,15,51.5505,5.06079),
                                                                                    (134,15,51.5473,5.0569),
                                                                                    (135,16,51.558,5.04131),
                                                                                    (136,16,51.5473,5.05681);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (137,16,51.5506,5.06073),
                                                                                    (138,16,51.5506,5.06073),
                                                                                    (139,16,51.5547,5.06356),
                                                                                    (140,17,51.5549,5.10648),
                                                                                    (141,17,51.5517,5.11006),
                                                                                    (142,17,51.5528,5.11434),
                                                                                    (143,17,51.5559,5.11255),
                                                                                    (144,17,51.5566,5.11141),
                                                                                    (145,18,51.5402,5.03354),
                                                                                    (146,18,51.5397,5.04823);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (147,18,51.5421,5.04956),
                                                                                    (148,18,51.5472,5.0569),
                                                                                    (149,18,51.558,5.04136),
                                                                                    (150,18,51.5585,5.0396),
                                                                                    (151,18,51.5536,5.03792),
                                                                                    (152,18,51.5536,5.03653),
                                                                                    (153,18,51.5497,5.03693),
                                                                                    (154,18,51.5462,5.03609),
                                                                                    (155,18,51.5444,5.02953),
                                                                                    (156,19,51.5678,5.08709);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (157,19,51.5672,5.09012),
                                                                                    (158,19,51.5674,5.09446),
                                                                                    (159,19,51.5676,5.09518),
                                                                                    (160,19,51.5684,5.09667),
                                                                                    (161,19,51.5685,5.09821),
                                                                                    (162,19,51.5689,5.0982),
                                                                                    (163,19,51.5755,5.09015),
                                                                                    (164,19,51.5745,5.08607),
                                                                                    (165,19,51.5736,5.086),
                                                                                    (166,19,51.5729,5.08454);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (167,20,51.5695,4.96784),
                                                                                    (168,20,51.565,4.99326),
                                                                                    (169,20,51.5738,4.9934),
                                                                                    (170,20,51.5773,4.97047),
                                                                                    (171,20,51.5773,4.96926),
                                                                                    (172,20,51.5736,4.96701),
                                                                                    (173,20,51.5713,4.96892),
                                                                                    (174,21,51.5651,4.99317),
                                                                                    (175,21,51.5615,5.01838),
                                                                                    (176,21,51.5699,5.01804);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (177,21,51.5738,4.99342),
                                                                                    (178,22,51.573,5.08463),
                                                                                    (179,22,51.5735,5.08583),
                                                                                    (180,22,51.5745,5.08617),
                                                                                    (181,22,51.5756,5.08998),
                                                                                    (182,22,51.5769,5.08421),
                                                                                    (183,23,51.5788,5.06649),
                                                                                    (184,23,51.5781,5.07341),
                                                                                    (185,23,51.5828,5.07627),
                                                                                    (186,23,51.5838,5.06798);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (187,24,51.5531,5.06463),
                                                                                    (188,24,51.5538,5.07212),
                                                                                    (189,24,51.555,5.07764),
                                                                                    (190,24,51.5553,5.07779),
                                                                                    (191,24,51.5563,5.06403),
                                                                                    (192,24,51.5548,5.06354),
                                                                                    (193,25,51.5616,5.01827),
                                                                                    (194,25,51.5587,5.03878),
                                                                                    (195,25,51.5664,5.04262),
                                                                                    (196,25,51.5701,5.01797);
INSERT INTO region_cords (id,region_id,latitude,longitude) VALUES
                                                                                    (197,26,51.5701,5.01806),
                                                                                    (198,26,51.5664,5.04247),
                                                                                    (199,26,51.5725,5.04511),
                                                                                    (200,26,51.575,5.03746),
                                                                                    (201,26,51.5763,5.02924),
                                                                                    (202,26,51.5788,5.02278),
                                                                                    (203,27,51.5427,5.11367),
                                                                                    (204,27,51.5457,5.11823),
                                                                                    (205,27,51.5527,5.11434),
                                                                                    (206,27,51.5517,5.10991);

-- Seed workshop codes

INSERT INTO workshopcode (code,expiration_date,creation_date) VALUES
                                                                                       (124516,'2024-11-14 14:51:43.000','2024-11-12 15:51:43.000'),
                                                                                       (269628,'2024-11-19 14:53:02.000','2024-11-12 15:53:02.000'),
                                                                                       (463784,'2024-11-12 16:55:06.000','2024-11-12 15:55:06.000'),
                                                                                       (480590,'2024-11-14 14:47:53.000','2024-11-12 15:47:53.000'),
                                                                                       (538424,'2025-12-31 13:44:22.000',NULL),
                                                                                       (667025,'2024-11-05 16:00:00.000',NULL),
                                                                                       (861059,'2024-11-12 15:03:21.000','2024-11-12 15:48:21.000'),
                                                                                       (889308,'2024-11-12 15:01:36.000','2024-11-12 15:46:36.000'),
                                                                                       (898017,'2024-11-12 15:24:13.000','2024-11-12 15:54:13.000'),
                                                                                       (123456789,'2025-12-31 13:44:22.000',NULL);

-- Seed users

INSERT INTO `user` (is_admin,first_name,last_name,email,password) VALUES
                                                                                           (0,'Pieter','Peter','Pieter@mail.com',NULL),
                                                                                           (1,'Jan','Joep','Jan@mail.com',NULL),
                                                                                           (0,'Joeri','waterman','Joeri@mail.com',NULL),
                                                                                           (0,'Benny','Bener','Benny@mail.com',NULL),
                                                                                           (1,'Arjan','Gosens','a.gosens@student.fontys.nl','$argon2id$v=19$m=65536,t=4,p=8$RteROzfT4b1Xp3gid1DU8A$y6N9yBVkDnw+uWc5zZkRpcxzud/wJAsJ3Sf/Pvsl6f4');

-- Seed stations

INSERT INTO station (name,database_tag,is_public,registration_code,location_locationid,user_id,is_active) VALUES
                                                                                                                                   ('Fontys Campus','MJS',1,378,10,12,NULL),
                                                                                                                                   ('Blaak','MJS',1,580,7,12,NULL),
                                                                                                                                   ('Spoorpark','MJS',1,674,8,1,NULL),
                                                                                                                                   ('Meetstation','MJS',1,402,12,1,NULL),
                                                                                                                                   ('Iepenpad','MJS',1,562,13,1,NULL),
                                                                                                                                   ('Noordhoek','MJS',1,980,14,1,NULL),
                                                                                                                                   ('Oisterwijksebaan','MJS',1,687,15,1,NULL),
                                                                                                                                   ('Berkelvijver','MJS',1,563,16,1,NULL),
                                                                                                                                   ('Ateletiekbaan','MJS',1,604,17,1,NULL),
                                                                                                                                   ('Borselestraat','MJS',1,964,18,1,NULL);
INSERT INTO station (name,database_tag,is_public,registration_code,location_locationid,user_id,is_active) VALUES
                                                                                                                                   ('Heerenveld','MJS',1,939,19,1,NULL),
                                                                                                                                   ('Dongewijk','MJS',1,972,20,1,NULL),
                                                                                                                                   ('Rijperkerkpark','MJS',1,958,21,1,NULL),
                                                                                                                                   ('Koolhoven','MJS',1,967,22,1,NULL),
                                                                                                                                   ('Bendastraat','MJS',1,569,23,1,NULL),
                                                                                                                                   ('Bachlaan','MJS',1,708,24,1,NULL),
                                                                                                                                   ('Verhulstlaan','MJS',1,475,25,1,NULL),
                                                                                                                                   ('Udenhout','MJS',1,575,26,1,NULL),
                                                                                                                                   ('Berkel-Enschot','MJS',1,943,27,1,NULL);

-- Seed sensor type

INSERT INTO sensor_type (type_name) VALUES
                                                             ('Temperatuur'),
                                                             ('Stikstof'),
                                                             ('Koolstofdioxide'),
                                                             ('Fijnstof'),
                                                             ('Luchtvochtigheid'),
                                                             ('Windsnelheid'),
                                                             ('Batterij');
-- Seed sensor

INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (0,5,4,11),
                                                                                              (0,25,1,12),
                                                                                              (0,55,5,12),
                                                                                              (0,3,7,12),
                                                                                              (0,26,1,11),
                                                                                              (0,51,5,11),
                                                                                              (0,3,7,11),
                                                                                              (0,25,1,10),
                                                                                              (0,51,5,10),
                                                                                              (0,3,7,10);
INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (0,25,1,12),
                                                                                              (0,55,5,12),
                                                                                              (0,3,7,12),
                                                                                              (0,26,1,11),
                                                                                              (0,51,5,11),
                                                                                              (0,3,7,11),
                                                                                              (1,30,1,20),
                                                                                              (1,33,5,20),
                                                                                              (1,3,7,20),
                                                                                              (1,31,1,17);
INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (1,39,5,17),
                                                                                              (1,3,7,17),
                                                                                              (1,31,1,11),
                                                                                              (1,40,5,11),
                                                                                              (1,3,7,11),
                                                                                              (1,29,1,18),
                                                                                              (1,43,5,18),
                                                                                              (1,3,7,18),
                                                                                              (1,29,1,25),
                                                                                              (1,39,5,25);
INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (1,3,7,25),
                                                                                              (1,30,1,29),
                                                                                              (1,36,5,29),
                                                                                              (1,3,7,29),
                                                                                              (1,41,1,15),
                                                                                              (1,24,5,15),
                                                                                              (1,3,7,15),
                                                                                              (1,30,1,12),
                                                                                              (1,42,5,12),
                                                                                              (1,3,7,12);
INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (1,27,1,16),
                                                                                              (1,34,5,16),
                                                                                              (1,3,7,16),
                                                                                              (1,30,1,26),
                                                                                              (1,40,5,26),
                                                                                              (1,3,7,26),
                                                                                              (1,30,1,27),
                                                                                              (1,50,5,27),
                                                                                              (1,3,7,27),
                                                                                              (1,30,1,28);
INSERT INTO sensor (active_data,sensor_data,sensor_typeid,stationid) VALUES
                                                                                              (1,42,5,28),
                                                                                              (1,3,7,28);
