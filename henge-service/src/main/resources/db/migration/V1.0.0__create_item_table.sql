create table `item` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(100) not null,
    `desc` varchar(100) not null,
    `createdBy` varChar(100) not null,
    `createdDate` datetime not null,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;