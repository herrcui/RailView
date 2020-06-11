from py4j.java_collections import SetConverter, MapConverter, ListConverter
from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
import logging
import gc
import random
import sys

python_log = True
logger = None

if python_log:
    logging.basicConfig(filename='py4j_disp.log', level=logging.ERROR)
    logger = logging.getLogger("py4j")
    logger.addHandler(logging.StreamHandler())


# implementation of Java interface "railapp.simulation.listeners.IDispatchingListener"
class RuleBasedDispatcher:    
    def __init__(self, logger=None):
        self.initialized = False
        self.logger = logger
    
    # Java call through Py4j
    def has_initialized(self):
        return self.initialized
        
    # Java call through Py4j
    # state: a java state object
    # actions: a list of actions
    # return the index of the action
    def determine_action(self, state, actions):
        gc.disable()
        action_index = 0
    
        # add your own logic here
        # example for with FCFS (probability 90%) or give up the chance of occupancy
        if random.uniform(0, 1) < 0.1: 
            action_index = 1

        gc.collect()
        
        return action_index

    # experience: Java-Object
    def save_action(self, action, current_pending):
        return

    def training(self):
        gc.disable()
        result = random.random()
        gc.collect()

        return


print("Create DispatchingServer...")

dispatching_server = RuleBasedDispatcher(logger)

client_server = ClientServer(
    java_parameters=JavaParameters(),
    python_parameters=PythonParameters(),
    python_server_entry_point=dispatching_server)

dispatching_server.initialized = True

print("Initialization of ClientServer accomplished")

