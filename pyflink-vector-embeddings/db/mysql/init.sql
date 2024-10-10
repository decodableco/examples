-- MySQL
CREATE DATABASE vector_demo;
USE vector_demo;

    create table Review (
        id bigint not null,
        itemId varchar(255),
        reviewText mediumtext,
        primary key (id)
    ) engine=InnoDB;

    create table Review_SEQ (
        next_val bigint
    ) engine=InnoDB;

    insert into Review_SEQ values ( 26 );

    insert into Review(id,itemId,reviewText) VALUES 
        (1,'B01N0TQ0OH','work great. use a new one every month'),
        (2,'B07DD2DMXB','Little on the thin side'),
        (3,'B082W3Z9YK','Quick delivery, fixed the issue!'),
        (4,'B078W2BJY8','I wasn''t sure whether these were worth it or not, given the cost compared to the original branded filters.<br /><br />I can happily report that these are a great value and work every bit as good as the original. If you are on the fence worrying whether these are worth it- I can assure you they are.'),
        (5,'B08C9LPCQV','Easy to install got the product expected to receive'),
        (6,'B08D6RFV6D','After buying this ice machine just 15 months ago and using it 5 times per month it’s now leaking so bad I can’t use it anymore. The company has refused to replace it!'),
        (7,'B001TH7GZA','Not the best quality'),
        (8,'B00AF7WZTM','Part came quickly and fit my LG dryer.  Thanks!'),
        (9,'B001H05AXY','Always arrive in a fast manner.  Descriptions on line are accurate and helpful'),
        (10,'B085C6C7WH','The company responded very quickly. Refunded purchase price right away. No problems at all. The unit did not work. I tried ev'),
        (11,'B01AML6LT0','Love little kitchen gadgets.  Would recommend this'),
        (12,'B09B21HWFM','but i havent had it long  a year down the road  i may change my mind and i love the blue trim  i  didnt realize it matches my  shower curtain so well as its not solid  but pretty swirls of  purples and blues and this matches it nicely  i got the first one and it was broken so i did a return and replace but  im impressed and its quiet and i like tht it has no agitator  for things to get stuck under !!'),
        (13,'B07BFGZQ65','i have a K15 and was concerned it wouldnt fit  but it was PERFECT !!!'),
        (14,'B01HIT0VMW','im excited to see how the coffee comes out  i got 2 mason jars too  so i ordered a 2nd one of these so i''ll always have 2 in the fridge  for the summer<br /><br />UPDATE:  my first cup was a bit strong  i added way too much coffee i guess but i watered it down with plenty of ice and  half and half - a little cinnamon and vanilla -  i ordered the 2nd filter and it will be here soon and i plan to have iced coffee all summer ...  definitely cutting down on the amount of coffee though !!!!!'),
        (15,'B072JXR3MW','what a great deal and it even had a little coffee scooper !!!!!!!!!  i paid this for 1 in Target  when i first got my Keurig last year !!!'),
        (16,'B00009W3HD','worked great'),
        (17,'B00EORXEUS','these are OK  a little tooo big  but they work  i had ordered Melita discs and they were so flimsy  !!'),
        (18,'B00A7ZJNHO','thin and chinsy i could even bother returning it i threw them out  - the second they touched the pot it disintegrated'),
        (19,'B016TZKU54','The filter did not fit in my Keurig K50. When closing the keurig, there was still a small gap that left the keurig slightly open. I had to hold down the keurig in order to get it to register that it was shut and allow me to select my cup size, and still had to hold it down while it was brewing. Then, the water barely flowed through the filter. Tried with a finer grind as described on the instructions. After a minute of holding it there, I had only less than half a cup of coffee. I tried again with a medium grind and still had the same issue. Decided it was not worth my hassle and to return it. Return was easy and was provided with a free return label.'),
        (20,'B08R9C3F83','This was hard to find out how to put it into the office maker. I think better instructions would have been nice. I ended up poking a hold in the bottom when I put the lid down. Now coffee grounds come out into my coffee. I would not buy this product again.'),
        (21,'B076ZRN68C','Everything is great, easy to trim to stove and counter design. Thanks'),
        (22,'B07CKYZRXH','Used in one cup pod machine.  It did the job very well coffee was good.'),
        (23,'B0745N964K','You can’t beat the price! Awesome stove and looks great!!!'),
        (24,'B003AXVADA','Good price for coffee filters.'),
        (25,'B001TJ5380','These water filters do the same job as the name brand from Samsung, but they are less expensive.');

-- Flink CDC user
CREATE USER 'flinkcdc'@'%' IDENTIFIED BY '$e(reT';
GRANT SELECT, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'flinkcdc';
FLUSH PRIVILEGES;
