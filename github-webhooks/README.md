# Processing GitHub Webhooks With Decodable

This repository shows how to process GitHub Webhooks with Decodable.
It accompanies this [blog post](https://www.decodable.co/blog/processing-github-webhooks-with-decodable).

## Prerequisites

* A Decodable account
* A Cloudflare account
* Java 17

## Set Up

1. Create a Decodable Stream

Go to the Decodable web UI -> "Streams" -> "New Stream".
Select "Import Schema" and past the contents of the _schema.json_ file from this directory.
Click "Next", specify a name for the stream and click "Create Stream".

2. Create a Decodable Connection

Go to the Decodable web UI -> "Connections" -> "New Connection".
Select the "REST" connector, click "Next", and choose the previously created stream as the destination for the connection's data.
Click "Next", specify a name for the connection and click "Create Connection".

3. Create a Decodable Pipeline

Go to the Decodable web UI -> "Pipelines" -> "New Pipeline".
Select the previously created stream as input stream.
Put in the following SQL:

```sql
INSERT INTO commit_count_by_author
SELECT c.author.name AS author, count(*) AS commits
FROM webhook_events e, UNNEST(e.commits) AS c
GROUP BY c.author.name
```

Click "Next" and "Create Stream".
Click "Next", specify a name for the pipeline and click "Create Pipeline".


4. Create a Cloudflare Worker

Log into the Cloudflare console and create a new worker.
Specify the following code for it, adjusting the account and connection ids as per your specific values:

```javascript
export default {
 async fetch(request) {
   const url = "https://<account>.api.decodable.co/v1alpha2/connections/<id>/events";


   // wrap the event into an array
   const json = await request.json();
   const payload = { events: [json] };


   // prepare a new request with the updated body and the bearer token header
   const newRequestInit = {
     method: "POST",
     body: JSON.stringify(payload),
     headers: {
       "Authorization": "Bearer <your token>"
     }
   };


   // execute that new request
   const newRequest = new Request(url, newRequestInit);
   const response = await fetch(newRequest);


   return response;
 }
};
```

Refer to the REST [connector guide](https://docs.decodable.co/docs/how-to-configure-and-use-the-rest-connector#authentication) for details on how to obtain a bearer token for the connection.

5. Create a GitHub Webhook

Go to "Settings" -> "Webhooks" of your GitHub repository of interest and click "Add webhook".
Speficy the Cloudflare worker’s URL as payload URL, “application/json” as the content type, and "Just the push event." as the event type to send.
Click "Add webhook".

## Running the Data Flow

If not done yet, activate the connection and pipeline in the Decodable web UI.
Push some commits to the GitHub repository with the webhook,
go to the "commit_count_by_author" stream and observe in the stream preview how the commit count per author is updated as events come in.
Optionally, run the following to create some random commits (this pushes commits to your repository, so make sure to not run this against a repository where you don't want to have these commits!):

```bash
#!/bin/bash
x=1
while [ $x -le 100 ]
do
  touch "some-file-$x.txt"
  echo `date` > "some-file-$x.txt"
  git add "some-file-$x.txt"
  git commit -m "Some commit" --author "`java NameGen.java`"
  sleep .5 &&
  x=$(( $x + 1 ))

  if [[ $(( x % 5 )) == 0 ]]; then
    git push upstream main
  fi
done
```
