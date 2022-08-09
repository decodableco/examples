insert into wikipedia_flatten
select 
    id,
    title,
    link,
    guidislink,
    published,
    summary,
    author,
    comments,
    diff_id,
    oldid,
    url_title,
    CASE
        WHEN LOCATE(':', url_title) > 0 THEN SPLIT_INDEX(url_title, ':', 0) 
        ELSE null
    END AS `type`
from (
    select 
        id,
        title,
        link,
        guidislink,
        summary,
        TO_TIMESTAMP(`published`, 'E, dd MMM yyyy HH:mm:ss z') as `published`,
        author,
        comments,
        PARSE_URL(id,'QUERY','diff') as diff_id,
        PARSE_URL(id,'QUERY','oldid') as oldid,
        PARSE_URL(id,'QUERY','title') as url_title
    from wikipedia_raw
    where lower(author) NOT LIKE '%bot%'
)