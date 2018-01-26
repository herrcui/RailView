from py4j.java_collections import SetConverter, MapConverter, ListConverter
from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
import logging
import random

class RandomDispatcher():
    is_initialized = False
    
    def __init__(self):
        self.is_initialized = True
    
    def isInitialized(self):
        return self.is_initialized
        
    def determineAction(self, actions):
        # add your own logic here
        length = len(actions)
        return random.randint(0, length-1)

python_log=True

if python_log:
    logging.basicConfig(filename='error.log', level=logging.ERROR)
    logger = logging.getLogger("py4j")
    logger.addHandler(logging.StreamHandler())
    
dispatching_server = RandomDispatcher()

clientserver = ClientServer(
    java_parameters=JavaParameters(),
    python_parameters=PythonParameters(),
    python_server_entry_point=dispatching_server)
    
dispatching_server.clientserver = clientserver
