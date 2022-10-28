from time import sleep
import boto3
import threading

client = boto3.client('kinesis')

def consume(stream_name:str='hubert.test.dbo.customers'):
    shards = client.list_shards(StreamName=stream_name)
    # print(f'shards {shards}========{dir(shards)}')
    for shard in shards['Shards']:
        # print(f'shard {shard["ShardId"]}')
        si = client.get_shard_iterator(
            StreamName=stream_name,
            ShardId=shard["ShardId"],
            ShardIteratorType='LATEST'
        )
        threading.Thread(target=lambda: show(si)).start()
        

def show(si):
    i = si['ShardIterator']
    while i is not None:
        response = client.get_records(ShardIterator=si['ShardIterator'])
        for rec in response['Records']: 
            print(rec)
            i = response['NextShardIterator']
        sleep(10)

if __name__ == "__main__":
    while True:
        consume()
