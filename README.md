# Node-RED docker

This is a docker image building setup, to be used for Node-RED wireless experimentation along with Node-RED modules and flow files (explained in a separate [repository](https://github.com/mmehari/Node-RED_Framework)).


First, build a docker image using the Dockerfile (i.e. [Dockerfile_Node-RED](Dockerfile_Node-RED)) provided.
```bash
docker build -t node-red -f Dockerfile_Node-RED .
```

Creating the image takes some time and after it is done, you can login to execute multiple demonstrators.

Issue the follow command to start a new docker container, with guest port 1880 forwarded to host port 1880. node-RED by default uses port 1880.
```bash
docker run -it -p 1880:1880 --name node-red --hostname=node-red node-red
```

Afterwards, start a demonstrator flow and give it some time, specially when executed the first time.
```bash
node-red /root/.node-red/flows_sensys2017.json
```

Later on, navigate to [localhost:1880](http://localhost:1880) to have a look at the node-red flow. For the example above, it will open the wireless sensor network demonstrator that was used in [sensys 2017 demo/poster session](https://doi.org/10.1145/3131672.3136971).


## Contact

michael.mehari@ugent.be
