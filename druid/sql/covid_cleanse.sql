insert into covid_cleansed
select
    ID,
    Country,
    CountryCode,
    Slug,
    NewConfirmed,
    TotalConfirmed,
    NewDeaths,
    TotalDeaths,
    NewRecovered,
    TotalRecovered,
    TO_TIMESTAMP(`Date`, 'yyyy-MM-dd''T''HH:mm:ss.SSS''Z''') as `Date`
from covid_raw