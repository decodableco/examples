#!/bin/bash

mkdir -p model/all-mpnet-base-v2
curl -L "https://drive.usercontent.google.com/download?id=1SYT4p4OXvcHzWHJgFSkDDUzraP30ZsFG&export=download&confirm=y" -o ./model/all-mpnet-base-v2/tokenizer.json
curl -L "https://drive.usercontent.google.com/download?id=1-_g--u_Tn72nkYbhb0VUeG-mBUPSoeZQ&export=download&confirm=y" -o ./model/all-mpnet-base-v2/model.onnx
