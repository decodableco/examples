-- MySQL
CREATE DATABASE vector_demo;
USE vector_demo;

    create table book (
        id bigint not null,
        title varchar(255),
        link varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table book_seq (
        next_val bigint
    ) engine=InnoDB;

    insert into book(id,title,link) VALUES 
        (1,'Frankenstein; Or, The Modern Prometheus','https://www.gutenberg.org/cache/epub/84/pg84.txt'),
        (2,'Moby Dick; Or, The Whale','https://www.gutenberg.org/cache/epub/2701/pg2701.txt'),
        (3,'Romeo and Juliet','https://www.gutenberg.org/cache/epub/1513/pg1513.txt'),
        (4,'Simple Sabotage Field Manual','https://www.gutenberg.org/cache/epub/26184/pg26184.txt'),
        (5,'Middlemarch','https://www.gutenberg.org/cache/epub/145/pg145.txt')
        ;
        -- more books to load ...
        -- (6,'A Room with a View','https://www.gutenberg.org/cache/epub/2641/pg2641.txt'),
        -- (7,'The Complete Works of William Shakespeare','https://www.gutenberg.org/cache/epub/100/pg100.txt'),
        -- (8,'Little Women; Or, Meg, Jo, Beth, and Amy','https://www.gutenberg.org/cache/epub/37106/pg37106.txt'),
        -- (9,'Pride and Prejudice','https://www.gutenberg.org/cache/epub/1342/pg1342.txt'),
        -- (10,'Alice''s Adventures in Wonderland','https://www.gutenberg.org/cache/epub/11/pg11.txt');
    
    insert into book_seq values (6);


-- Flink CDC user
CREATE USER 'flinkcdc'@'%' IDENTIFIED BY '$e(reT';
GRANT SELECT, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'flinkcdc';
FLUSH PRIVILEGES;
