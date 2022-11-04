from time import sleep
import boto3
import threading

client = boto3.client('kinesis')

def consume(stream_name:str='ora.ADMIN.CUSTOMERS'):
    shards = client.list_shards(StreamName=stream_name)
    # print(f'shards {shards}========{dir(shards)}')
    for shard in shards['Shards']:
        # print(f'shard {shard["ShardId"]}')
        si = client.get_shard_iterator(
            StreamName=stream_name,
            ShardId=shard["ShardId"],
            ShardIteratorType='LATEST'
        )
        response = client.get_records(ShardIterator=si['ShardIterator'])
        for rec in response['Records']: 
            print(rec)
        # threading.Thread(target=lambda: show(si)).start()
        

def show(si):
    i = si['ShardIterator']
    while i is not None:
        response = client.get_records(ShardIterator=i)
        for rec in response['Records']: 
            print(rec)
            sleep(10)

        i = response['NextShardIterator']

if __name__ == "__main__":
    while True:
        consume()
        sleep(10)
